package com.maryamaj.overlay.models;

import io.realm.RealmObject;

public class Point2D extends RealmObject{
    public float x;
    public float y;

    public Point2D(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Point2D(Point2D p) {
        this.x = p.x;
        this.y = p.y;
    }

    public Point2D() {

    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Point2D opposite() {
        return new Point2D(-x, -y);
    }
}
