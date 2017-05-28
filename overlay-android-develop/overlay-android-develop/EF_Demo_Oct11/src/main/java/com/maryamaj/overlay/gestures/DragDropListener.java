package com.maryamaj.overlay.gestures;

import com.maryamaj.overlay.models.Point2D;

public abstract class DragDropListener {
    public abstract void onStartDrag(Point2D start);
    public abstract void onMove(Point2D delta);
    public abstract void onDrop();
    public abstract void onDelete();
}
