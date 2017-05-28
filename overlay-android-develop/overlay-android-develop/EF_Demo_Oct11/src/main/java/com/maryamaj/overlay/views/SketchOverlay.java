package com.maryamaj.overlay.views;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.SketchPoint;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmQuery;


public class SketchOverlay extends RelativeLayout implements ARListener, TutorialManagerListener {
    private static final String TAG = "SketchOverlay";
    private static final float TOUCH_TOLERANCE = 4;

    private Path sketchPath;
    Context context;
    private Paint sketchPaint;
    private float mX, mY;
    private Paint pointerPaint;
    private Path pointerPath;
    private Sketch sketch = null;
    private RealmList<SketchPoint> points;
    private boolean isDrawingEnabled;
    private Control control;
    private FloatingActionButton done;
    private GestureDetector clickDetector;
    private TutorialManager tutorialManager;


    public SketchOverlay(Context c) {
        this(c, null);
    }

    public SketchOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();

    }

    public void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.sketch_overlay);
        clickDetector = new GestureDetector(context, new ClickDetector());
        sketchPath = new Path();
        points = new RealmList<>();

        done = new FloatingActionButton(context);
        done.setId(View.generateViewId());
        done.setImageResource(R.drawable.ic_done_black_24dp);
//        done.setColorFilter(Color.BLACK);
        done.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        done.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams tParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        tParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tParams.setMargins(0, 0, 0, GeometryUtils.dpToPx(context, 15));
        done.setLayoutParams(tParams);

        done.setVisibility(View.GONE);
        done.setOnTouchListener((v, event) -> {
            if (clickDetector.onTouchEvent(event)) {
                sketchPath.reset();
                control.getRealm().executeTransaction(realm -> {
                    RealmList<SketchPoint> sketchPoints = GeometryUtils.translateSketchPoints(points, control.getMarkerCenter().opposite());
                    RealmList<SketchPoint> sketchPointsRealm = new RealmList<>();
                    for (SketchPoint sketchPoint : GeometryUtils.scaleSketchPoints(sketchPoints, control.getDepth() / tutorialManager.getActiveArea().getDrawDepth())) {
                        sketchPointsRealm.add(realm.copyToRealm(sketchPoint));
                    }
                    if (sketch == null) {
                        sketch = realm.createObject(Sketch.class, UUID.randomUUID().toString());
                        sketch.setArea(tutorialManager.getActiveArea());
                        sketch.setPoints(sketchPointsRealm);
                        RestClient.createSketch(sketch);
                    } else {
                        sketch.setPoints(sketchPointsRealm);
                        RestClient.updateSketch(sketch);
                    }
                });
                points.clear();
                isDrawingEnabled = false;
                control.resumeCamera();
                done.setVisibility(View.GONE);
            }
            return true;
        });
        addView(done);

        pointerPaint = new Paint();
        pointerPath = new Path();
        pointerPaint.setAntiAlias(true);
        pointerPaint.setColor(Color.BLUE);
        pointerPaint.setStyle(Paint.Style.STROKE);
        pointerPaint.setStrokeJoin(Paint.Join.MITER);
        pointerPaint.setStrokeWidth(4f);

        sketchPaint = new Paint();
        sketchPaint.setAntiAlias(true);
        sketchPaint.setDither(true);
        sketchPaint.setColor(Color.GREEN);
        sketchPaint.setStyle(Paint.Style.STROKE);
        sketchPaint.setStrokeJoin(Paint.Join.ROUND);
        sketchPaint.setStrokeCap(Paint.Cap.ROUND);
        sketchPaint.setStrokeWidth(12);

        setWillNotDraw(false);

        control.getRealm().where(Sketch.class).findAll().addChangeListener(element -> {
            if (sketch != null && !sketch.isValid()) {
                onSketchDeletion();
                Log.d(TAG, "Sketch deletion called due to update");
            }
        });
        setVisibility(View.GONE);
    }

    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isDrawingEnabled) {  // draw the existing points
            sketchPath.reset();
            int size = points.size();
            if (size == 0) {
                return;
            }
            SketchPoint start = points.get(0);
            sketchPath.moveTo(start.x, start.y);
            SketchPoint previous = start;
            int index = 1;
            while (index < size) {
                SketchPoint current = points.get(index);
                if (current.isInitial()) {
                    sketchPath.lineTo(previous.x, previous.y);
                    sketchPath.moveTo(current.x, current.y);
                } else {
                    sketchPath.quadTo(previous.x, previous.y, (current.x + previous.x) / 2, (current.y + previous.y) / 2);
                }
                previous = current;
                index++;
            }
            SketchPoint end = points.get(size - 1);
            sketchPath.lineTo(end.x, end.y);
        }
        canvas.drawPath(sketchPath, sketchPaint);
        canvas.drawPath(pointerPath, pointerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        if (!isDrawingEnabled) {
            return false;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                moveTouch(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                upTouch();
                invalidate();
                break;
        }
        return true;
    }

    private void startTouch(float x, float y) {
        sketchPath.moveTo(x, y);
        points.add(new SketchPoint(x, y, true));
        mX = x;
        mY = y;
    }

    private void moveTouch(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            points.add(new SketchPoint(x, y, false));
            sketchPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
            mX = x;
            mY = y;
            pointerPath.reset();
            pointerPath.addCircle(mX, mY, 30, Path.Direction.CW);
        }
    }

    private void upTouch() {
        sketchPath.lineTo(mX, mY);
        pointerPath.reset();
    }

    @Override
    public void onAreaChange(Area a) {
        if (a == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        RealmQuery<Sketch> sketchRealmQuery = control.getRealm().where(Sketch.class).equalTo("area.uuid", a.getUuid());
        if (sketchRealmQuery == null || sketchRealmQuery.count() == 0) {
            sketch = null;
            points.clear();
        } else {
            sketch = sketchRealmQuery.findFirst();
            if (control.getMarkerCenter() != null)
                points = GeometryUtils.translateSketchPoints(sketch.getPointsAt(control.getDepth()), control.getMarkerCenter());
        }
        invalidate();
    }

    @Override
    public void onModeChange(boolean buildMode) {

    }

    @Override
    public void editMode() {
        control.pauseCamera();
        isDrawingEnabled = true;
        sketchPath.reset();
        points.clear();
        done.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPauseCamera() {

    }

    @Override
    public void onResumeCamera() {

    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
        post(() -> {
            if (sketch != null && sketch.isValid()) {
                if(depth > 0) {
                    Log.e("sketch(depth)", "depthsketch.getPointsAt(depth)" + sketch.getPointsAt(depth));
                    Log.e(" markerCenter", " markerCenter" + markerCenter);
                    points = GeometryUtils.translateSketchPoints(sketch.getPointsAt(depth), markerCenter);
                    invalidate();
                }
            }
        });
    }

    private void onSketchDeletion() {
        sketch = null;
        points.clear();
        invalidate();
    }
}