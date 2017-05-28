package com.maryamaj.overlay.rest.responses;

import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.SketchPoint;
import com.maryamaj.overlay.models.Text;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;

public class AreaDetailResponse {
    public String uuid;
    public int sequence;
    public AreaPoint center;
    public float radius;
    public float draw_depth;
    public ArrayList<TextResponse> texts;
    public ArrayList<AudioResponse> audios;
    public ArrayList<SketchResponse> sketches;

    private Area mArea;

    public Area getArea() {
        if (mArea == null) {
            mArea = new Area();
            mArea.setUuid(uuid);
            mArea.setSequence(sequence);
            mArea.setCenter(new Point2D(center.x, center.y));
            mArea.setRadius(radius);
            mArea.setDrawDepth(draw_depth);
        }
        return mArea;
    }

    public List<Text> getTexts() {
        List<Text> textList = new ArrayList<>();
        for(TextResponse textResponse: texts) {
            Text text = new Text();
            text.setUuid(textResponse.uuid);
            text.setText(textResponse.text);
            text.setArea(getArea());
            text.setPosition(new Point2D(textResponse.position.x, textResponse.position.y));
            textList.add(text);
        }
        return textList;
    }

    public List<Audio> getAudios() {
        List<Audio> audioList = new ArrayList<>();
        for(AudioResponse audioResponse: audios) {
            Audio audio = new Audio();
            audio.setUuid(audioResponse.uuid);
            audio.setAudioFile(audioResponse.audio_file);
            audio.setArea(getArea());
            audio.setPosition(new Point2D(audioResponse.position.x, audioResponse.position.y));
            audioList.add(audio);
        }
        return audioList;
    }

    public List<Sketch> getSketches() {
        List<Sketch> sketchList = new ArrayList<>();
        for(SketchResponse sketchResponse: sketches) {
            Sketch sketch = new Sketch();
            sketch.setUuid(sketchResponse.uuid);
            RealmList<SketchPoint> points = new RealmList<>();
            for(SketchPointResponse sketchPointResponse : sketchResponse.points) {
                SketchPoint point = new SketchPoint();
                point.setX(sketchPointResponse.x);
                point.setY(sketchPointResponse.y);
                point.setInitial(sketchPointResponse.is_initial);
                points.add(point);
            }
            sketch.setPoints(points);
            sketch.setArea(getArea());
            sketchList.add(sketch);
        }
        return sketchList;
    }

    public void persist(Realm realm) {
        Area area = realm.createObject(Area.class, uuid);
        area.setSequence(sequence);
        area.setCenter(realm.copyToRealm(new Point2D(center.x, center.y)));
        area.setRadius(radius);
        for(TextResponse textResponse: texts) {
            Text text = realm.createObject(Text.class, textResponse.uuid);
            text.setText(textResponse.text);
            text.setArea(area);
            text.setPosition(realm.copyToRealm(new Point2D(textResponse.position.x, textResponse.position.y)));
        }
        for(AudioResponse audioResponse: audios) {
            Audio audio = realm.createObject(Audio.class, audioResponse.uuid);
            audio.setAudioFile(audioResponse.audio_file);
            audio.setArea(area);
            audio.setPosition(realm.copyToRealm(new Point2D(audioResponse.position.x, audioResponse.position.y)));
            audio.downloadToDevice();
        }
        for(SketchResponse sketchResponse: sketches) {
            Sketch sketch = realm.createObject(Sketch.class, sketchResponse.uuid);
            RealmList<SketchPoint> points = new RealmList<>();
            for(SketchPointResponse sketchPointResponse : sketchResponse.points) {
                SketchPoint point = realm.createObject(SketchPoint.class);
                point.setX(sketchPointResponse.x);
                point.setY(sketchPointResponse.y);
                point.setInitial(sketchPointResponse.is_initial);
                points.add(point);
            }
            sketch.setPoints(points);
            sketch.setArea(area);
        }
    }
}

class AreaPoint {
    public float x;
    public float y;
}