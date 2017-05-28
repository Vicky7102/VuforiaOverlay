package com.maryamaj.overlay.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

public class FollowArrowOverlay extends View implements ARListener, TutorialManagerListener{
    private static final String TAG = "FollowArrowOverlay";
    private static final int ARROW_BASE = 80, ARROW_HEIGHT = 100, ARROW_PADDING = 40;
    private Paint arrowInnerPaint, arrowBorderPaint;
    private Path arrowPath;
    private Context context;
    private Control control;
    private TutorialManager tutorialManager;

    public FollowArrowOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    private void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.follow_arrow_overlay);
        arrowPath = new Path();

        arrowInnerPaint = new Paint();
        arrowInnerPaint.setAntiAlias(true);
        arrowInnerPaint.setColor(Color.RED);
        arrowInnerPaint.setStyle(Paint.Style.FILL);
        arrowInnerPaint.setAlpha(60);

        arrowBorderPaint = new Paint();
        arrowBorderPaint.setAntiAlias(true);
        arrowBorderPaint.setStrokeWidth(5f);
        arrowBorderPaint.setStyle(Paint.Style.STROKE);
        arrowBorderPaint.setColor(Color.BLACK);
        arrowBorderPaint.setStrokeJoin(Paint.Join.ROUND);
        arrowInnerPaint.setAlpha(50);
    }

    @Override
    public void onDraw(Canvas canvas) {
        Area activeArea = tutorialManager.getActiveArea();
        if(activeArea == null)
            return;
        Point2D follow = GeometryUtils.translate(activeArea.getCenterAt(control.getDepth()), control.getMarkerCenter());
        if (follow.x < 0 || follow.x > canvas.getWidth() || follow.y < 0 || follow.y > canvas.getHeight()) {
            float centerX = canvas.getWidth() / 2.0f;
            float centerY = canvas.getHeight() / 2.0f;
            float intersectX, intersectY, rotation;
            float minX = ARROW_HEIGHT + ARROW_PADDING;
            float minY = ARROW_HEIGHT + ARROW_PADDING;
            float maxX = canvas.getWidth() - minX;
            float maxY = canvas.getHeight() - minY;
            if (follow.x == centerX) {
                intersectX = centerX;
                intersectY = follow.y < centerY ? minY : maxY;
                rotation = follow.y < centerY ? 0 : 180;
            } else {
                float m = (follow.y - centerY) / (follow.x - centerX);
                rotation = (float) Math.toDegrees(Math.atan(m));
                float c = centerY - m * centerX;
                if (follow.x >= centerX) {
                    rotation -= 90;
                    if (m == 0) {
                        intersectX = maxX;
                        intersectY = centerY;
                    } else if (follow.y <= centerY) {
                        // quadrant 1
                        intersectY = minY;
                        intersectX = (intersectY - c) / m;
                        if (intersectX > maxX) {
                            intersectX = maxX;
                            intersectY = m * intersectX + c;
                        }
                    } else {
                        // quadrant 2
                        intersectX = maxX;
                        intersectY = m * intersectX + c;
                        if (intersectY > maxY) {
                            intersectY = maxY;
                            intersectX = (intersectY - c) / m;
                        }
                    }
                } else {

                    rotation += 90;
                    if (m == 0) {
                        intersectX = minX;
                        intersectY = centerY;
                    } else if (follow.y >= centerY) {
                        // quadrant 3
                        intersectX = minX;
                        intersectY = m * intersectX + c;
                        if (intersectY > maxY) {
                            intersectY = maxY;
                            intersectX = (intersectY - c) / m;
                        }
                    } else {
                        // quadrant 4
                        intersectX = minX;
                        intersectY = m * intersectX + c;
                        if (intersectY < minY) {
                            intersectY = minY;
                            intersectX = (intersectY - c) / m;
                        }
                    }
                }
            }
            canvas.save();
            canvas.translate(intersectX, intersectY);
            canvas.rotate(rotation);
            arrowPath.reset();
            arrowPath.moveTo(-(ARROW_BASE / 2.0f), 0);
            arrowPath.lineTo(ARROW_BASE / 2.0f, 0);
            arrowPath.lineTo(0, ARROW_HEIGHT);
            arrowPath.lineTo(-(ARROW_BASE / 2.0f), 0);
            canvas.drawPath(arrowPath, arrowInnerPaint);
            canvas.drawPath(arrowPath, arrowBorderPaint);
            canvas.restore();
        }
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
        postInvalidate();
    }

    @Override
    public void onPauseCamera() {
        setVisibility(GONE);
    }

    @Override
    public void onResumeCamera() {
        setVisibility(VISIBLE);
    }

    @Override
    public void onAreaChange(Area area) {
        invalidate();
    }

    @Override
    public void onModeChange(boolean buildMode) {

    }

    @Override
    public void editMode() {

    }
}
