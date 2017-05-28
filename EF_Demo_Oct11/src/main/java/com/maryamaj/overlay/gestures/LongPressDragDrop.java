package com.maryamaj.overlay.gestures;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

public class LongPressDragDrop {
    private static final String TAG = "LongPressDragDrop";
    private ViewGroup viewGroup;
    private GestureDetector longPressDetector;
    private Point2D curPoint;
    private boolean dragging = false;
    private static final int DELETION_DISTANCE = 300;
    private FloatingActionButton deleteBtn;
    private Control control;
    private DragDropListener dragDropListener;
    private Vibrator vibrator;

    public LongPressDragDrop(ViewGroup viewGroup, DragDropListener dragDropListener) {
        longPressDetector = new GestureDetector(viewGroup.getContext(), new LongPressDetector());
        vibrator = (Vibrator) viewGroup.getContext().getSystemService(Context.VIBRATOR_SERVICE);

        this.viewGroup = viewGroup;
        this.dragDropListener = dragDropListener;
        this.control = (Control) viewGroup.getContext();

        deleteBtn = new FloatingActionButton(viewGroup.getContext());
        deleteBtn.setId(View.generateViewId());
        deleteBtn.setImageResource(R.drawable.ic_delete_forever_black_24dp);
        deleteBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        deleteBtn.setSize(FloatingActionButton.SIZE_MINI);
        FrameLayout.LayoutParams deleteBtnParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        deleteBtnParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        deleteBtnParams.setMargins(0, 0, 0, GeometryUtils.dpToPx(viewGroup.getContext(), 15));
        deleteBtn.setLayoutParams(deleteBtnParams);
        deleteBtn.setVisibility(View.GONE);

        viewGroup.addView(deleteBtn);
    }

    public boolean onMotionEvent(MotionEvent event) {
        longPressDetector.onTouchEvent(event);
        if (dragging) {
            float x = event.getRawX();
            float y = event.getRawY();
            Point2D touch = new Point2D(x, y);
//            Log.d(TAG, "Touched at: " + x + ", " + y);
            switch (event.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    Point2D delta = GeometryUtils.translate(touch, curPoint.opposite());
//                    Log.d(TAG, "Delta: " + delta.x + ", " + delta.y);
                    dragDropListener.onMove(delta);
                    if(GeometryUtils.distance(new Point2D(deleteBtn.getX() + deleteBtn.getWidth() / 2, deleteBtn.getY() + deleteBtn.getHeight() / 2), touch) <= DELETION_DISTANCE) {
                        deleteBtn.setSize(FloatingActionButton.SIZE_NORMAL);
                    }
                    else {
                        deleteBtn.setSize(FloatingActionButton.SIZE_MINI);
                    }
                    curPoint = touch;
                    break;
                case MotionEvent.ACTION_UP:
                    if(deleteBtn.getSize() == FloatingActionButton.SIZE_MINI) {
                        dragDropListener.onDrop();
                    }
                    else {
                        dragDropListener.onDelete();
                    }
                    dragging = false;
                    deleteBtn.setVisibility(View.GONE);
                    control.resumeCamera();
                    break;
            }
        }
//        Log.d(TAG, "Dragging: " + dragging);
        return dragging;
    }

    public boolean isDragging() {
        return dragging;
    }

    private class LongPressDetector extends GestureDetector.SimpleOnGestureListener {
        @Override
        public void onLongPress(MotionEvent e) {
            float x = e.getRawX();
            float y = e.getRawY();
            curPoint = new Point2D(x, y);
            dragging = true;
            Log.d(TAG, "Started dragging at: " + x + ", " + y);
            deleteBtn.setVisibility(View.VISIBLE);
            vibrator.vibrate(100);
            dragDropListener.onStartDrag(curPoint);
            control.pauseCamera();
        }
    }

}

