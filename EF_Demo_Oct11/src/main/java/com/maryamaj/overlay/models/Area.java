package com.maryamaj.overlay.models;

import com.google.gson.annotations.SerializedName;
import com.maryamaj.overlay.utils.GeometryUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Area extends RealmObject {
    public static final String TAG = "Area";

    @PrimaryKey
    private String uuid;
    private Point2D center;
    private float radius;
    private int sequence;

    @SerializedName("draw_depth")
    private float drawDepth;

    public Point2D getCenter() {
        return center;
    }

    public void setCenter(Point2D center) {
        this.center = center;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    public float getDrawDepth() {
        return drawDepth;
    }

    public void setDrawDepth(float drawDepth) {
        this.drawDepth = drawDepth;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getRadiusAt(float depth) {
        return this.radius * this.drawDepth / depth;
    }

    public Point2D getCenterAt(float depth) {
        return GeometryUtils.scale(center, drawDepth / depth);
    }
}
