package com.maryamaj.overlay.views;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Vibrator;
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
import com.maryamaj.ubitile_marker.R;

import java.util.ArrayList;
import java.util.List;


public class OverviewOverlay extends FrameLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "OverviewOverlay";
    private static final int THUMBNAIL_SIDE = 210, THUMBNAIL_RADIUS = 70, BOTTOM_PADDING = 60, LEFT_PADDING = 70, RIGHT_PADDING = 230, FLING_DECELERATION = 1, ANIMATION_FRAMES = 10, FLING_SNAP_THRESHOLD = 0, DRAG_TRANSLATE_PADDING = 100;
    private static final float MIN_DIFF = 0.00001f;
    private Context context;
    private Control control;
    private GestureDetector clickDetector, flingDetector, longPressDetector;
    private TutorialManager tutorialManager;
    private Paint selectedAreaInnerPaint, selectedAreaOuterPaint, existingAreasPaint;
    private float translateX = 0.0f, targetX = 0.0f, acceleration = 0.0f, flingVelocity = 0.0f, selectedTextHeight, textHeight, centerX, leftX, rightX, Y, touchX;
    private int curFrame = 0, startDragSeq, curDragSeq;
    private boolean flinging = false, dragging = false, swapping = false;
    private TextPaint selectedTextPaint, selectedTextBorderPaint, textPaint, textBorderPaint;
    private Vibrator vibrator;
    private List<Float> positions;

    public OverviewOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    private void init() {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.overview_overlay);
        setWillNotDraw(false);
        clickDetector = new GestureDetector(context, new ClickDetector());
        flingDetector = new GestureDetector(context, new FlingDetector());
        longPressDetector = new GestureDetector(context, new OverviewDragDropListener());
        positions = new ArrayList<>();

        existingAreasPaint = new Paint();
        existingAreasPaint.setAntiAlias(true);
        existingAreasPaint.setColor(Color.GRAY);
        existingAreasPaint.setStyle(Paint.Style.STROKE);
        existingAreasPaint.setStrokeWidth(10f);

        selectedAreaInnerPaint = new Paint();
        selectedAreaInnerPaint.setAntiAlias(true);
        selectedAreaInnerPaint.setColor(Color.WHITE);
        selectedAreaInnerPaint.setStyle(Paint.Style.STROKE);
        selectedAreaInnerPaint.setStrokeWidth(5f);

        selectedAreaOuterPaint = new Paint();
        selectedAreaOuterPaint.setAntiAlias(true);
        selectedAreaOuterPaint.setStrokeWidth(15f);
        selectedAreaOuterPaint.setStyle(Paint.Style.STROKE);
        selectedAreaOuterPaint.setColor(Color.BLACK);

        selectedTextBorderPaint = new TextPaint();
        selectedTextBorderPaint.setAntiAlias(true);
        selectedTextBorderPaint.setTextSize(85f);
        selectedTextBorderPaint.setColor(Color.BLACK);
        selectedTextBorderPaint.setStrokeWidth(3f);
        selectedTextBorderPaint.setStyle(Paint.Style.STROKE);
        selectedTextBorderPaint.setTextAlign(Paint.Align.CENTER);

        selectedTextPaint = new TextPaint();
        selectedTextPaint.setAntiAlias(true);
        selectedTextPaint.setTextSize(85f);
        selectedTextPaint.setColor(Color.WHITE);
        selectedTextPaint.setTextAlign(Paint.Align.CENTER);

        Rect rect = new Rect();
        selectedTextPaint.getTextBounds("1", 0, 1, rect);
        selectedTextHeight = rect.height();

        textBorderPaint = new TextPaint();
        textBorderPaint.setAntiAlias(true);
        textBorderPaint.setTextSize(65f);
        textBorderPaint.setColor(Color.BLACK);
        textBorderPaint.setStrokeWidth(2f);
        textBorderPaint.setStyle(Paint.Style.STROKE);
        textBorderPaint.setTextAlign(Paint.Align.CENTER);

        textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(65f);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        textPaint.getTextBounds("1", 0, 1, rect);
        textHeight = rect.height();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = right - left - LEFT_PADDING - RIGHT_PADDING;
        centerX = LEFT_PADDING + width / 2.0f;
        int numThumbnails = width / THUMBNAIL_SIDE;
        if (numThumbnails % 2 == 0) {
            numThumbnails--;
        }
        leftX = centerX - numThumbnails / 2.0f * THUMBNAIL_SIDE;
        rightX = centerX + numThumbnails / 2.0f * THUMBNAIL_SIDE;
        Y = bottom - top - BOTTOM_PADDING - THUMBNAIL_RADIUS;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!tutorialManager.isBuildMode() || control.isCameraPaused())
            return;
        List<Area> areas = tutorialManager.getAreas();
        if (areas.size() == 0)
            return;
        Area activeArea = tutorialManager.getActiveArea();
        canvas.clipRect(leftX, 0, rightX, canvas.getHeight());
        float X = centerX;
        canvas.save();
        canvas.translate(-translateX, 0);
        if(dragging) {
            for(int i = 0; i < positions.size(); i++) {
                int seq = i + 1;
                if(seq == startDragSeq)
                    continue;
                String strSeq = String.valueOf(seq);
                boolean active = activeArea != null && seq == activeArea.getSequence();
                float x = positions.get(i);
                if(seq > startDragSeq && seq <= curDragSeq) {
                    x -= THUMBNAIL_SIDE;
                }
                else if(seq >= curDragSeq && seq < startDragSeq) {
                    x += THUMBNAIL_SIDE;
                }
                drawThumbnail(canvas, x, strSeq, active);
            }
        }
        else {
            for (Area area : areas) {
                if(dragging && area.getSequence() == startDragSeq) {
                    X += THUMBNAIL_SIDE;
                    continue;
                }
                String seq = String.valueOf(area.getSequence());
                boolean active = activeArea != null && area.getSequence() == activeArea.getSequence();
                drawThumbnail(canvas, X, seq, active);
                X += THUMBNAIL_SIDE;
            }

        }
        canvas.restore();
        if (dragging) {
            boolean active = activeArea != null && startDragSeq == activeArea.getSequence();
            drawThumbnail(canvas, touchX, String.valueOf(startDragSeq), active);
        }
        if (flinging) {
            float minX = 0;
            float maxX = (areas.size() - 1) * THUMBNAIL_SIDE;
            translateX -= flingVelocity;
            if (translateX < minX) {
                translateX = minX;
                flinging = false;
                targetX = translateX;
            } else if (translateX > maxX) {
                translateX = maxX;
                flinging = false;
                targetX = translateX;
            } else {
                if (flingVelocity < 0) {
                    flingVelocity += FLING_DECELERATION;
                    flingVelocity = Math.min(0, flingVelocity);
                } else {
                    flingVelocity -= FLING_DECELERATION;
                    flingVelocity = Math.max(0, flingVelocity);
                }
                if (Math.abs(flingVelocity) <= FLING_SNAP_THRESHOLD) {
                    flinging = false;
                    setTargetSequence(sequenceAt(centerX));
                }
            }
            invalidate();
            return;
        }
        float diff = Math.abs(targetX - translateX);
        if (diff > MIN_DIFF) {
            float velocity = acceleration * curFrame;
            float move = Math.min(velocity, diff);
//                Log.d(TAG, "Diff: " + diff + " Velocity: " + velocity + " Cur Frame: " + curFrame);
            if (targetX > translateX) {
                translateX += move;
            } else {
                translateX -= move;
            }
            curFrame += 1;
            invalidate();
        }
    }

    private void drawThumbnail(Canvas canvas, float x, String seq, boolean active) {
        if (active) {
            canvas.drawCircle(x, Y, THUMBNAIL_RADIUS + 20, selectedAreaOuterPaint);
            canvas.drawCircle(x, Y, THUMBNAIL_RADIUS + 20, selectedAreaInnerPaint);
            canvas.drawText(seq, x, Y + selectedTextHeight / 2.0f, selectedTextPaint);
            canvas.drawText(seq, x, Y + selectedTextHeight / 2.0f, selectedTextBorderPaint);
        } else {
            canvas.drawCircle(x, Y, THUMBNAIL_RADIUS, existingAreasPaint);
            canvas.drawText(seq, x, Y + textHeight / 2.0f, textPaint);
            canvas.drawText(seq, x, Y + textHeight / 2.0f, textBorderPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!tutorialManager.isBuildMode() || control.isCameraPaused()) {
            return false;
        }
        float x = event.getRawX();
        float y = event.getRawY();
        float minY = getHeight() - BOTTOM_PADDING - THUMBNAIL_RADIUS - THUMBNAIL_SIDE / 2.0f;
        if(dragging) {
            if(event.getAction() == MotionEvent.ACTION_MOVE) {
                float curCenter = centerOf(curDragSeq);
                if(curDragSeq < tutorialManager.getAreas().size() && x > curCenter + THUMBNAIL_SIDE) {
                    curDragSeq += 1;
                }
                else if(curDragSeq > 1 && x < curCenter - THUMBNAIL_SIDE) {
                    curDragSeq -= 1;
                }
                touchX = x;
                float minX = 0;
                float maxX = (tutorialManager.getAreas().size() - 1) * THUMBNAIL_SIDE;
                if(translateX > minX && x < leftX + DRAG_TRANSLATE_PADDING) {
                    translateX -= (leftX + DRAG_TRANSLATE_PADDING - x);
                    translateX = Math.max(translateX, minX);
                }
                else if(translateX < maxX && x > rightX - DRAG_TRANSLATE_PADDING) {
                    translateX += (x - (rightX - DRAG_TRANSLATE_PADDING));
                    translateX = Math.min(translateX, maxX);
                }
            }
            else if(event.getAction() == MotionEvent.ACTION_UP) {
                setTargetSequence(sequenceAt(centerX));
                if(startDragSeq != curDragSeq) {
                    vibrator.vibrate(100);
                    tutorialManager.changeAreaSeq(startDragSeq, curDragSeq);
                }
                dragging = false;
            }
            invalidate();
            return true;
        }
        if (y < minY) {
            return false;
        }
        if (flinging) {
            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                flinging = false;
                setTargetSequence(sequenceAt(centerX));
            }
            return true;
        }
        if (Math.abs(targetX - translateX) > MIN_DIFF) {
            return true;
        }
        longPressDetector.onTouchEvent(event);
        if (clickDetector.onTouchEvent(event)) {
            int index = sequenceAt(x) - 1;
            if (index < tutorialManager.getAreas().size() && index >= 0) {
                tutorialManager.setActiveArea(tutorialManager.getAreas().get(index));
            }
            return true;
        }
        flinging = flingDetector.onTouchEvent(event);
        if (flinging) {
            invalidate();
            return true;
        }
        return true;
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
    }

    @Override
    public void onAreaChange(Area area) {
        if (area == null) {
            if (getTargetSequence() > tutorialManager.getAreas().size()) {
                setTargetSequence(tutorialManager.getAreas().size());
            }
        } else {
            setTargetSequence(area.getSequence());
        }
        invalidate();
    }

    @Override
    public void onModeChange(boolean buildMode) {
        invalidate();
    }

    private void setTargetSequence(int sequence) {
        targetX = (sequence - 1) * THUMBNAIL_SIDE;
        curFrame = 1;
        acceleration = 2 * (Math.abs(targetX - translateX)) / (ANIMATION_FRAMES * ANIMATION_FRAMES);
    }

    private int getTargetSequence() {
        int index = (int) ((targetX + THUMBNAIL_SIDE / 2.0f) / THUMBNAIL_SIDE);
        return index + 1;
    }

    private int sequenceAt(float touchX) {
        int index = (int) ((touchX + translateX - centerX + THUMBNAIL_SIDE / 2.0f) / THUMBNAIL_SIDE);
        return index + 1;
    }

    private float centerOf(int seq) {
        return (seq - 1) * THUMBNAIL_SIDE + centerX - translateX;
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


    private class OverviewDragDropListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            startDragSeq = sequenceAt(e.getRawX());
            if(startDragSeq < 1 || startDragSeq > tutorialManager.getAreas().size())
                return;
            dragging = true;
            curDragSeq = startDragSeq;
            float X = centerX;
            positions.clear();
            for (int i = 0; i < tutorialManager.getAreas().size(); i++) {
                positions.add(X);
                X += THUMBNAIL_SIDE;
            }
            vibrator.vibrate(100);
        }
    }

    private class FlingDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            flingVelocity = velocityX / 75;
            return true;
        }
    }
}