package com.maryamaj.overlay.api;

import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Point3D;

import io.realm.Realm;

public interface Control {
    public void pauseCamera();
    public void resumeCamera();
    public boolean isCameraPaused();
    public Point2D getMarkerCenter();
    public float getDepth();
    public Realm getRealm();
}

