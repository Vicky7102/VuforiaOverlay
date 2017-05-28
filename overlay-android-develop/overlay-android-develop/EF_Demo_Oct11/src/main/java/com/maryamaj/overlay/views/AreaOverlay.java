package com.maryamaj.overlay.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
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
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import java.util.UUID;


public class AreaOverlay extends FrameLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "AreaOverlay";
    private static final int MIN_AREA_RADIUS = 100;

    private float MARKER_RADIUS;
    private Context context;
    private Paint existingAreasPaint, newAreaPaint, markerPaint, selectedAreaInnerPaint, selectedAreaOuterPaint;
    private Point2D newAreaCenter, draggedAreaCenter;
    private Area touchedArea;
    private GestureDetector clickDetector;
    private Control control;
    private float newAreaRadius = 0.0f;
    private boolean isDrawing = false;
    private LongPressDragDrop longPressDragDrop;
    private TutorialManager tutorialManager;

    public AreaOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    private void init() {
        setWillNotDraw(false);
        clickDetector = new GestureDetector(context, new ClickDetector());
        MARKER_RADIUS = GeometryUtils.dpToPx(context, 5);
        longPressDragDrop = new LongPressDragDrop(this, new AreaDragDropListener());
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.area_overlay);

        existingAreasPaint = new Paint();
        existingAreasPaint.setAntiAlias(true);
        existingAreasPaint.setColor(Color.GRAY);
        existingAreasPaint.setStyle(Paint.Style.STROKE);
        existingAreasPaint.setStrokeWidth(15f);
        existingAreasPaint.setAlpha(100);

        newAreaPaint = new Paint();
        newAreaPaint.setAntiAlias(true);
        newAreaPaint.setColor(Color.RED);
        newAreaPaint.setStyle(Paint.Style.STROKE);
        newAreaPaint.setStrokeWidth(15f);
        newAreaPaint.setAlpha(100);

        selectedAreaInnerPaint = new Paint();
        selectedAreaInnerPaint.setAntiAlias(true);
        selectedAreaInnerPaint.setColor(Color.WHITE);
        selectedAreaInnerPaint.setStyle(Paint.Style.STROKE);
        selectedAreaInnerPaint.setStrokeWidth(10f);

        selectedAreaOuterPaint = new Paint();
        selectedAreaOuterPaint.setAntiAlias(true);
        selectedAreaOuterPaint.setStrokeWidth(25f);
        selectedAreaOuterPaint.setStyle(Paint.Style.STROKE);
        selectedAreaOuterPaint.setColor(Color.BLACK);

        markerPaint = new Paint();
        markerPaint.setAntiAlias(true);
        markerPaint.setColor(Color.WHITE);
        markerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        markerPaint.setStrokeJoin(Paint.Join.ROUND);
        markerPaint.setStrokeWidth(10f);
        markerPaint.setAlpha(100);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Point2D markerCenter = control.getMarkerCenter();
        Area activeArea = tutorialManager.getActiveArea();
        if (tutorialManager.isBuildMode() || activeArea == null) {
            for (Area area : tutorialManager.getAreas()) {
                Point2D translatedCenter = GeometryUtils.translate(area.getCenterAt(control.getDepth()), markerCenter);
                canvas.drawCircle(translatedCenter.x, translatedCenter.y, area.getRadiusAt(control.getDepth()), existingAreasPaint);
            }
        }
        if (activeArea != null) {
            Point2D translatedCenter = GeometryUtils.translate(activeArea.getCenterAt(control.getDepth()), markerCenter);
            canvas.drawCircle(translatedCenter.x, translatedCenter.y, activeArea.getRadiusAt(control.getDepth()), selectedAreaOuterPaint);
            canvas.drawCircle(translatedCenter.x, translatedCenter.y, activeArea.getRadiusAt(control.getDepth()), selectedAreaInnerPaint);
        }

        if (longPressDragDrop.isDragging() && touchedArea != null) {
            Point2D translatedCenter = GeometryUtils.translate(draggedAreaCenter, markerCenter);
            canvas.drawCircle(translatedCenter.x, translatedCenter.y, touchedArea.getRadiusAt(control.getDepth()), existingAreasPaint);
        }

        if (isDrawing && newAreaCenter != null)
            canvas.drawCircle(newAreaCenter.x, newAreaCenter.y, newAreaRadius, newAreaPaint);

        if (isTracking()) {
            canvas.drawCircle(markerCenter.x, markerCenter.y, MARKER_RADIUS, markerPaint);
        }
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getRawX();
        float y = event.getRawY();
        Point2D touch = new Point2D(x, y);
        if (isDrawing) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startTouch(touch);
                    break;
                case MotionEvent.ACTION_MOVE:
                    moveTouch(touch);
                    break;
                case MotionEvent.ACTION_UP:
                    isDrawing = false;
                    upTouch();
                    break;
            }
            return true;
        }
        if (longPressDragDrop.isDragging()) {
            longPressDragDrop.onMotionEvent(event);
            return true;
        } else {
            // this condition makes sure we don't update the touched area while dragging
            touchedArea = getTouchedArea(touch);
        }
        Area activeArea = tutorialManager.getActiveArea();
        if (touchedArea != null) {
            if (tutorialManager.isBuildMode()) {
                longPressDragDrop.onMotionEvent(event);
            }
            if (clickDetector.onTouchEvent(event)) {
                if (activeArea == null || activeArea.getSequence() != touchedArea.getSequence()) {
                    tutorialManager.setActiveArea(touchedArea);
                }
                return true;
            }
        }
        if (activeArea != null && touchedArea == null) {
            tutorialManager.setActiveArea(null);
        }
        return true;
    }

    private void startTouch(Point2D touch) {
        newAreaCenter = touch;
    }

    private void moveTouch(Point2D touch) {
        newAreaRadius = (float) GeometryUtils.distance(touch, newAreaCenter);
        invalidate();
    }

    private void upTouch() {
        if (newAreaRadius >= MIN_AREA_RADIUS) {
            Point2D translatedCenter = GeometryUtils.translate(newAreaCenter, control.getMarkerCenter().opposite());
            control.getRealm().executeTransaction(realm -> {
                Area area = realm.createObject(Area.class, UUID.randomUUID().toString());
                area.setCenter(realm.copyToRealm(translatedCenter));
                area.setRadius(newAreaRadius);
                area.setDrawDepth(control.getDepth());
                area.setSequence(tutorialManager.getAreas().size() + 1);
                tutorialManager.addArea(area);
            });
        }
        newAreaCenter = null;
        control.resumeCamera();
        invalidate();
    }

    private Area getTouchedArea(Point2D touch) {
        for (Area area : tutorialManager.getAreas()) {
            Point2D translatedCenter = GeometryUtils.translate(area.getCenterAt(control.getDepth()), control.getMarkerCenter());
            if (GeometryUtils.distance(translatedCenter, touch) <= area.getRadiusAt(control.getDepth()))
                return area;
        }
        return null;
    }

    private boolean isTracking() {
        return control.getMarkerCenter() != null && control.getMarkerCenter().x > 0;
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
        postInvalidate();
    }

    @Override
    public void onAreaChange(Area area) {
        invalidate();
    }

    @Override
    public void onModeChange(boolean buildMode) {
        invalidate();
    }

    @Override
    public void editMode() {
        control.pauseCamera();
        isDrawing = true;
    }

    @Override
    public void onPauseCamera() {
    }

    @Override
    public void onResumeCamera() {
    }


    private class AreaDragDropListener extends DragDropListener {
        @Override
        public void onStartDrag(Point2D start) {
            touchedArea = getTouchedArea(start);
            if (touchedArea != null) {
                draggedAreaCenter = touchedArea.getCenterAt(control.getDepth());
            }
//            Log.d(TAG, "Touched area is: " + touchedArea);
        }

        @Override
        public void onMove(Point2D delta) {
//            Log.d(TAG, "On move called");
            if (touchedArea != null) {
                draggedAreaCenter = GeometryUtils.translate(draggedAreaCenter, delta);
                invalidate();
            }
        }

        @Override
        public void onDrop() {
            if (touchedArea != null) {
                control.getRealm().executeTransaction(realm -> {
                    touchedArea.setCenter(realm.copyToRealm(GeometryUtils.scale(draggedAreaCenter, control.getDepth() / touchedArea.getDrawDepth())));
                    touchedArea.setDrawDepth(control.getDepth());
                    touchedArea.setRadius(touchedArea.getRadiusAt(control.getDepth()));
                });
                RestClient.updateArea(touchedArea);
                invalidate();
            }
        }

        @Override
        public void onDelete() {
            if (touchedArea != null) {
                tutorialManager.removeArea(touchedArea);
                invalidate();
            }
        }
    }
}