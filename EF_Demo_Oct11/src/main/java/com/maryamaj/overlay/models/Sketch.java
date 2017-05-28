package com.maryamaj.overlay.models;

import android.util.Log;

import com.google.gson.annotations.JsonAdapter;
import com.maryamaj.overlay.utils.GeometryUtils;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Sketch extends RealmObject {
    public static final String TAG = "Sketch";
    @PrimaryKey
    private String uuid;

    private RealmList<SketchPoint> points;

    @JsonAdapter(AreaAdapter.class)
    private Area area;

    public Sketch() {
        points = new RealmList<>();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public RealmList<SketchPoint> getPoints() {
        return points;
    }

    public RealmList<SketchPoint> getPointsAt(float depth) {
        Log.e("depth","depth"+depth);
        Log.e("points","points"+points);
        if(area!=null) {
            Log.e("area.getDrawDepth()", "area.getDrawDepth()" + area.getDrawDepth());
            return GeometryUtils.scaleSketchPoints(points, area.getDrawDepth() / depth);
        }else{
            return GeometryUtils.scaleSketchPoints(points, depth);
        }
    }

    public void setPoints(RealmList<SketchPoint> points) {
        this.points = points;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }
}
