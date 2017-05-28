package com.maryamaj.overlay.api;

import com.maryamaj.overlay.models.Point2D;

public interface ARListener {
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId);
    public void onPauseCamera();
    public void onResumeCamera();
}
