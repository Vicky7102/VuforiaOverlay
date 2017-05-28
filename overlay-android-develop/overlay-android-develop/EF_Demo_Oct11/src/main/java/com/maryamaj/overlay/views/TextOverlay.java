package com.maryamaj.overlay.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.design.widget.FloatingActionButton;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.gestures.DragDropListener;
import com.maryamaj.overlay.gestures.LongPressDragDrop;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import java.util.UUID;

import io.realm.RealmQuery;


public class TextOverlay extends FrameLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "TextOverlay";
    private static final int MIN_CALLOUT_HEIGHT = 140;
    private static final int CALLOUT_DISTANCE = 100;
    private static final int MAX_CALLOUT_WIDTH = 700;
    private static final int CALLOUT_CORNER_RADIUS = 30;
    private static final int CALLOUT_GAP = 70;
    private static final int CALLOUT_BOTTOM_PADDING = 30;
    private static final int CALLOUT_TEXT_PADDING = 30;

    private Context context;
    private Control control;
    private AlertDialog alert;
    private Text text;
    private EditText textInput;
    private FloatingActionButton toggleTextBtn;
    private LongPressDragDrop longPressDragDrop;
    private GestureDetector clickDetector;
    private Paint callOutPaint, callOutBorderPaint;
    private TextPaint callOutTextPaint;
    private Path callOutPath;
    private StaticLayout textLayout;
    private boolean showTextCallout = false;
    private TutorialManager tutorialManager;

    public TextOverlay(Context c) {
        this(c, null);
    }

    public TextOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    public void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.text_overlay);
        clickDetector = new GestureDetector(context, new ClickDetector());
        longPressDragDrop = new LongPressDragDrop(this, new MyDrapDropListener());
        setWillNotDraw(false);

        textInput = new EditText(context);

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle("Enter text here:");
        dialog.setCancelable(false);
        dialog.setView(textInput);
        dialog.setPositiveButton("OK", (dialog1, which) -> {
            if (!textInput.getText().toString().isEmpty()) {
                String str = textInput.getText().toString();
                if (text == null) {
                    control.getRealm().executeTransaction(realm -> {
                        Area area = tutorialManager.getActiveArea();
                        text = realm.createObject(Text.class, UUID.randomUUID().toString());
                        text.setArea(area);
                        text.setText(str);
                        Point2D position = GeometryUtils.translate(area.getCenter(), new Point2D(area.getRadius() + 10, -area.getRadius() - 10));
                        text.setPosition(realm.copyToRealm(position));
                    });
                    RestClient.createText(text);
                } else {
                    control.getRealm().executeTransaction(realm -> text.setText(str));
                    RestClient.updateText(text);
                }
                toggleTextBtn.setVisibility(VISIBLE);
                textLayout = createLayout();
            }
            dialog1.dismiss();
            control.resumeCamera();
        });
        alert = dialog.create();

        toggleTextBtn = new FloatingActionButton(context);
        toggleTextBtn.setId(View.generateViewId());
        toggleTextBtn.setImageResource(R.drawable.ic_title_white_24dp);
        toggleTextBtn.setColorFilter(Color.BLACK);
        toggleTextBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        toggleTextBtn.setSize(FloatingActionButton.SIZE_MINI);
        toggleTextBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        toggleTextBtn.setVisibility(View.GONE);
        addView(toggleTextBtn);

        toggleTextBtn.setOnTouchListener((v, event) -> {
            if (tutorialManager.isBuildMode()) {
                longPressDragDrop.onMotionEvent(event);
            }
            if (clickDetector.onTouchEvent(event)) {
                showTextCallout = !showTextCallout;
                invalidate();
            }
            return true;
        });

        callOutPaint = new Paint();
        callOutPaint.setAntiAlias(true);
        callOutPaint.setColor(Color.WHITE);
        callOutPaint.setStyle(Paint.Style.FILL);

        callOutBorderPaint = new Paint();
        callOutBorderPaint.setAntiAlias(true);
        callOutBorderPaint.setColor(Color.BLACK);
        callOutBorderPaint.setStyle(Paint.Style.STROKE);
        callOutBorderPaint.setStrokeWidth(10f);

        callOutTextPaint = new TextPaint();
        callOutTextPaint.setAntiAlias(true);
        callOutTextPaint.setTextSize(65f);
        callOutTextPaint.setColor(Color.BLACK);

        callOutPath = new Path();

        control.getRealm().where(Text.class).findAll().addChangeListener(element -> {
            if(text != null && !text.isValid()) {
                onTextDeletion();
                Log.d(TAG, "Text deletion called due to update");
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!showTextCallout)
            return;

        callOutPath.reset();
        float startX = toggleTextBtn.getX() + toggleTextBtn.getWidth();
        float startY = toggleTextBtn.getY();
        float rect_height = Math.max(MIN_CALLOUT_HEIGHT, textLayout.getHeight() + 2 * CALLOUT_TEXT_PADDING);
        float rect_width = Math.min(MAX_CALLOUT_WIDTH, callOutTextPaint.measureText(text.getText()) + 2 * CALLOUT_TEXT_PADDING);
        float radius = CALLOUT_CORNER_RADIUS;
        float x = startX;
        float y = startY;
        callOutPath.moveTo(x, y);
        x += CALLOUT_DISTANCE;
        y -= CALLOUT_DISTANCE;
        callOutPath.lineTo(x, y);
        y = y + CALLOUT_GAP + CALLOUT_BOTTOM_PADDING - rect_height + radius;

        float textX = x + CALLOUT_TEXT_PADDING;
        float textY = (y - radius) + CALLOUT_TEXT_PADDING;

        callOutPath.lineTo(x, y);
        callOutPath.arcTo(x, y - radius, x + 2 * radius, y + radius, 180, 90, false);
        x += rect_width - radius;
        y -= radius;
        callOutPath.lineTo(x, y);
        callOutPath.arcTo(x - radius, y, x + radius, y + 2 * radius, -90, 90, false);
        x += radius;
        y += rect_height - radius;
        callOutPath.lineTo(x, y);
        callOutPath.arcTo(x - 2 * radius, y - radius, x, y + radius, 0, 90, false);
        x = (x - rect_width) + radius;
        y += radius;
        callOutPath.lineTo(x, y);
        callOutPath.arcTo(x - radius, y - 2 * radius, x + radius, y, 90, 90, false);
        x -= radius;
        y -= CALLOUT_BOTTOM_PADDING;
        callOutPath.lineTo(x, y);
        callOutPath.lineTo(startX, startY);

        canvas.drawPath(callOutPath, callOutPaint);
        canvas.drawPath(callOutPath, callOutBorderPaint);

        canvas.save();
        canvas.translate(textX, textY);
        textLayout.draw(canvas);
        canvas.restore();
    }

    @Override
    public void onAreaChange(Area area) {
        if (area == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        RealmQuery<Text> textRealmQuery = control.getRealm().where(Text.class).equalTo("area.uuid", area.getUuid());
        if (textRealmQuery == null || textRealmQuery.count() == 0) {
            text = null;
            textInput.setText("");
            toggleTextBtn.setVisibility(GONE);
        } else {
            text = textRealmQuery.findFirst();
            textInput.setText(text.getText());
            positionText();
            toggleTextBtn.setVisibility(VISIBLE);
            textLayout = createLayout();
        }
        showTextCallout = false;
    }

    @Override
    public void onModeChange(boolean buildMode) {

    }

    @Override
    public void editMode() {
        control.pauseCamera();
        alert.show();
    }

    @Override
    public void onPauseCamera() {
        if (!longPressDragDrop.isDragging()) {
            toggleTextBtn.setVisibility(GONE);
        }
        showTextCallout = false;
    }

    @Override
    public void onResumeCamera() {
        if (text != null) {
            toggleTextBtn.setVisibility(VISIBLE);
        }
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
        post(() -> {
            if (text != null) {
                positionText();
                invalidate();
            }
        });
    }

    private StaticLayout createLayout() {
        return new StaticLayout(text.getText(), callOutTextPaint, MAX_CALLOUT_WIDTH - 2 * CALLOUT_TEXT_PADDING, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
    }

    private void positionText() {
        if (text == null || !text.isValid())
            return;
        if (control.getMarkerCenter() != null) {
            if(text != null) {
                if (text.getPositionAt(control.getDepth()) != null) {
                    Point2D position = GeometryUtils.translate(text.getPositionAt(control.getDepth()), control.getMarkerCenter());
                    toggleTextBtn.setX(position.x);
                    toggleTextBtn.setY(position.y);
                }
            }
        }
    }

    private void onTextDeletion() {
        showTextCallout = false;
        text = null;
        textInput.setText("");
        toggleTextBtn.setVisibility(View.GONE);
        invalidate();
    }

    private class MyDrapDropListener extends DragDropListener {
        @Override
        public void onStartDrag(Point2D start) {

        }

        @Override
        public void onMove(Point2D delta) {
            toggleTextBtn.setX(toggleTextBtn.getX() + delta.x);
            toggleTextBtn.setY(toggleTextBtn.getY() + delta.y);
        }

        @Override
        public void onDrop() {
            Point2D curPosition = new Point2D(toggleTextBtn.getX(), toggleTextBtn.getY());
            Point2D position = GeometryUtils.translate(curPosition, control.getMarkerCenter().opposite());
            control.getRealm().executeTransaction(realm -> text.setPosition(realm.copyToRealm(GeometryUtils.scale(position, control.getDepth() / text.getArea().getDrawDepth()))));
            RestClient.updateText(text);
        }

        @Override
        public void onDelete() {
            RestClient.deleteText(text);
            control.getRealm().executeTransaction(realm -> text.deleteFromRealm());
            onTextDeletion();
        }
    }
}
