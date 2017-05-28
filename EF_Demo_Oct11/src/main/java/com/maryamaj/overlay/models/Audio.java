package com.maryamaj.overlay.models;

import android.os.Environment;

import com.google.gson.annotations.JsonAdapter;
import com.maryamaj.overlay.utils.FileUtils;
import com.maryamaj.overlay.utils.GeometryUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class Audio extends RealmObject {
    public static final String TAG = "Audio";
    @PrimaryKey
    private String uuid;
    private Point2D position;
    private String audioFile;
    private String audioFileLocal;

    public Point2D getPosition() {
        return position;
    }

    public Point2D getPositionAt(float depth) {
        if(area!= null) {
            return GeometryUtils.scale(position, area.getDrawDepth() / depth);
        }else{
            return GeometryUtils.scale(position, depth);
        }
    }

    public void setPosition(Point2D position) {
        this.position = position;
    }

    @JsonAdapter(AreaAdapter.class)
    private Area area;

    public String getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void downloadToDevice() {
        audioFileLocal = getExternalStoragePath();
        FileUtils.downloadFileFromURL(getAudioFile(), audioFileLocal);
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    private String getExternalStoragePath() {
        return Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/" + uuid + ".mp3";
    }

    public void transferToExternalStorage() {
        FileUtils.moveFile(audioFileLocal, getExternalStoragePath());
        setAudioFileLocal(getExternalStoragePath());
    }

    public String getAudioFileLocal() {
        return audioFileLocal;
    }

    public void setAudioFileLocal(String audioFileLocal) {
        this.audioFileLocal = audioFileLocal;
    }


}
