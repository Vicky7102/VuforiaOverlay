package com.maryamaj.overlay.models;

import android.util.Log;

import com.google.gson.annotations.JsonAdapter;
import com.maryamaj.overlay.utils.GeometryUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Text extends RealmObject {
    public static final String TAG = "Text";

    @PrimaryKey
    private String uuid;

    private Point2D position;

    private String text;

    @JsonAdapter(AreaAdapter.class)
    private Area area;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public Point2D getPosition() {
        return position;
    }

    public Point2D getPositionAt(float depth) {
        //if(depth > 0) {
        Log.e("depth", "depth" + depth);
        Log.e("position", "position" + position);
        //  Log.e("area.getDrawDepth()","area.getDrawDepth()"+area.getDrawDepth());
        if (area != null) {
            if (area.getDrawDepth() > 0) {
                return GeometryUtils.scale(position, area.getDrawDepth() / depth);
            } else {
                return GeometryUtils.scale(position, depth);
            }
        }else {
            return GeometryUtils.scale(position, depth);
        }
       /* }else{
            return GeometryUtils.scale(position, area.getDrawDepth());
        }*/
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }
}