package com.maryamaj.overlay.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;


public class NavigationOverlay extends FrameLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "NavigationOverlay";
    private static final int WIDTH = 30, HEIGHT = 140, BEND = 60, BOTTOM_PADDING = 80, GAP = 160, SIDE_PADDING = 60;
    private int PADDING = 25, CIRCLE_PADDING = 20, LAYOUT_WIDTH = 200;
    private Context context;
    private Control control;
    private GestureDetector clickDetector;
    private TutorialManager tutorialManager;
    private Paint navigationInnerPaint, navigationBorderPaint;
    private Path navigateNext, navigatePrev;
    private TextPaint textPaint;
    private StaticLayout textLayout;
    private Paint circlePaint;

    public NavigationOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    private void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.navigation_overlay);
        setWillNotDraw(false);
        clickDetector = new GestureDetector(context, new ClickDetector());

        navigationInnerPaint = new Paint();
        navigationInnerPaint.setAntiAlias(true);
        navigationInnerPaint.setStyle(Paint.Style.FILL);
        navigationInnerPaint.setColor(Color.WHITE);
        navigationInnerPaint.setAlpha(50);

        navigationBorderPaint = new Paint();
        navigationBorderPaint.setAntiAlias(true);
        navigationBorderPaint.setStrokeWidth(5f);
        navigationBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        navigationBorderPaint.setStrokeCap(Paint.Cap.ROUND);
        navigationBorderPaint.setStyle(Paint.Style.STROKE);
        navigationBorderPaint.setColor(Color.BLACK);
        navigationBorderPaint.setAlpha(70);

        circlePaint = new Paint();
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.WHITE);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(65f);
        textPaint.setColor(Color.BLACK);

        PADDING = GeometryUtils.dpToPx(context, PADDING);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (tutorialManager.isBuildMode() || control.isCameraPaused())
            return;
        init_buttons();
        if (tutorialManager.hasNext()) {
            navigationInnerPaint.setColor(Color.WHITE);
        } else {
            navigationInnerPaint.setColor(Color.GRAY);
        }
        canvas.drawPath(navigateNext, navigationInnerPaint);
        canvas.drawPath(navigateNext, navigationBorderPaint);

        if (tutorialManager.hasPrevious()) {
            navigationInnerPaint.setColor(Color.WHITE);
        } else {
            navigationInnerPaint.setColor(Color.GRAY);
        }
        canvas.drawPath(navigatePrev, navigationInnerPaint);
        canvas.drawPath(navigatePrev, navigationBorderPaint);

        Area activeArea = tutorialManager.getActiveArea();
        if (activeArea == null)
            return;
        String seq = String.valueOf(textLayout.getText());
        float textWidth = textPaint.measureText(seq);
        float textHeight = textPaint.getTextSize();
        float x = getWidth() / 2.0f;
        float y = getHeight() - BOTTOM_PADDING - HEIGHT / 2.0f;
        float radius = Math.max(textWidth / 2.0f, textHeight / 2.0f) + CIRCLE_PADDING;
        canvas.drawCircle(x, y, radius, circlePaint);
        x-= textWidth / 2.0f;
        y-= textHeight / 2.0f;
        canvas.translate(x, y);
        textLayout.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (tutorialManager.isBuildMode() || control.isCameraPaused())
            return false;
        float x = event.getRawX();
        float y = event.getRawY();
        if (y < getHeight() - BOTTOM_PADDING - HEIGHT) {
            return false;
        }
        float minX = getWidth() / 2.0f - GAP / 2.0f - WIDTH - BEND - SIDE_PADDING;
        float maxX = getWidth() / 2.0f + GAP / 2.0f + WIDTH + BEND + SIDE_PADDING;
        if (x > maxX || x < minX) {
            return false;
        }
        if (clickDetector.onTouchEvent(event)) {
            if (x <= getWidth() / 2.0f) {
                tutorialManager.selectPrevious();
            } else {
                tutorialManager.selectNext();
            }
        }
        return true;
    }

    private void init_buttons() {
        if (navigatePrev == null || navigateNext == null) {
            float x = getWidth() / 2.0f;
            float y = getHeight() - BOTTOM_PADDING;
            navigateNext = new Path();
            navigatePrev = new Path();
            float gap = GAP / 2.0f;
            navigateNext.moveTo(x + gap, y);
            navigatePrev.moveTo(x - gap, y);
            navigateNext.lineTo(x + gap + WIDTH, y);
            navigatePrev.lineTo(x - gap - WIDTH, y);
            y -= HEIGHT / 2;
            navigateNext.lineTo(x + gap + WIDTH + BEND, y);
            navigatePrev.lineTo(x - gap - WIDTH - BEND, y);
            y -= HEIGHT / 2;
            navigateNext.lineTo(x + gap + WIDTH, y);
            navigatePrev.lineTo(x - gap - WIDTH, y);
            navigateNext.lineTo(x + gap, y);
            navigatePrev.lineTo(x - gap, y);
            y += HEIGHT / 2;
            navigateNext.lineTo(x + gap + BEND, y);
            navigatePrev.lineTo(x - gap - BEND, y);
            y += HEIGHT / 2;
            navigateNext.lineTo(x + gap, y);
            navigatePrev.lineTo(x - gap, y);
        }
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
    }

    @Override
    public void onAreaChange(Area area) {
        if (area != null) {
            textLayout = new StaticLayout(String.valueOf(area.getSequence()), textPaint, LAYOUT_WIDTH, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
        }
        invalidate();
    }

    @Override
    public void onModeChange(boolean buildMode) {
        invalidate();
    }

    @Override
    public void editMode() {
    }

    @Override
    public void onPauseCamera() {
        invalidate();
    }

    @Override
    public void onResumeCamera() {
        invalidate();
    }
}