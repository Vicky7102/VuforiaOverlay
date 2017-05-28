package com.maryamaj.overlay.models;

import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;

public class SketchPoint extends RealmObject{
    @SerializedName("is_initial")
    private boolean isInitial;

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float x;
    public float y;

    public SketchPoint(float x, float y, boolean isInitial) {
        this.x = x;
        this.y = y;
        this.isInitial = isInitial;
    }

    public SketchPoint(SketchPoint s) {
        this.x = s.x;
        this.y = s.y;
        this.isInitial = s.isInitial;
    }

    public SketchPoint() {

    }

    public boolean isInitial() {
        return isInitial;
    }

    public void setInitial(boolean initial) {
        isInitial = initial;
    }

}
