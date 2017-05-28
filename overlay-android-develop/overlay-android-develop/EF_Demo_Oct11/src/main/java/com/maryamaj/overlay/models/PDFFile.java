package com.maryamaj.overlay.models;

import com.google.gson.annotations.JsonAdapter;
import com.maryamaj.overlay.utils.GeometryUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by android on 20/4/17.
 */

public class PDFFile extends RealmObject {
    public static final String TAG = "PDFFile";

    @PrimaryKey
    private String uuid;

    private Point2D position;

    private String filePath;
    private String url;
    private String name;

    @JsonAdapter(AreaAdapter.class)
    private Area area;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return GeometryUtils.scale(position, area.getDrawDepth() / depth);
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

}