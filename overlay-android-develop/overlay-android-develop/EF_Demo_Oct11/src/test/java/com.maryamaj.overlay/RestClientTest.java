package com.maryamaj.overlay;


import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestAPI;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.responses.AreaDetailResponse;
import com.maryamaj.overlay.rest.responses.AudioResponse;
import com.maryamaj.overlay.rest.responses.SketchResponse;
import com.maryamaj.overlay.rest.responses.TextResponse;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Response;

public class RestClientTest {
    private RestAPI restAPI;

    public RestClientTest() throws IOException {
        restAPI = RestClient.getRestAPI();
    }

    @Test
    public void connection_test() throws IOException {
        Call<List<Area>> areaListCall = restAPI.listAreas();
        Response<List<Area>> response = areaListCall.execute();
        Assert.assertTrue(response.isSuccessful());
    }

    @Test
    public void create_area() throws IOException {
//        Area area = new Area();
//        area.setCenter(new Point2D(0, 0));
//        area.setRadius(10f);
//        Area new_area = restAPI.createArea(area).execute().body();
//        Assert.assertNotNull(new_area.getId());
//        restAPI.deleteArea(new_area.getId()).execute();

    }

    @Test
    public void create_objects() throws IOException {
//        Area area = new Area();
//        area.setCenter(new Point2D(0, 0));
//        area.setRadius(10f);
//        Area new_area = restAPI.createArea(area).execute().body();
//        Assert.assertNotNull(new_area.getId());
//
//        Text text = new Text();
//        text.setText("Hello World");
//        text.setArea(new_area);
//        TextResponse textResponse = restAPI.createText(text).execute().body();
//        Assert.assertNotNull(textResponse.id);
//
//        Sketch sketch = new Sketch();
//        final Point2D p1 = new Point2D();
//        p1.setX(0);
//        p1.setY(0);
//        Point2D p2 = new Point2D();
//        p2.setX(1);
//        p2.setY(1);
//        Point2D p3 = new Point2D();
//        p3.setX(0);
//        p3.setY(1);
//        sketch.setPoints(new RealmList<>(p1, p2, p3));
//        sketch.setArea(new_area);
//        SketchResponse sketchResponse = restAPI.createSketch(sketch).execute().body();
//        Assert.assertNotNull(sketchResponse.id);
//
//        InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("page1.mp3");
//        MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
//        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MP3, IOUtils.toByteArray(inputStream));
//        MultipartBody.Part body = MultipartBody.Part.createFormData("audio_file", "page1.mp3", requestBody);
//        Response<AudioResponse> audioResponse = restAPI.createAudio(new_area.getId(), body).execute();
//        Assert.assertNotNull(audioResponse.body().id);
//
//        restAPI.deleteArea(new_area.getId()).execute();
    }

    @Test
    public void listObjects() throws IOException {
        List<AreaDetailResponse> areas = restAPI.listAreasDetail().execute().body();
        Assert.assertNotNull(areas);
        AreaDetailResponse areaDetailResponse = areas.get(0);
        Assert.assertNotNull(areaDetailResponse.texts);
        Assert.assertNotNull(areaDetailResponse.sketches);
        Assert.assertNotNull(areaDetailResponse.audios);
        Assert.assertNotNull(areaDetailResponse.center);
        Assert.assertNotNull(areaDetailResponse.radius);
    }

    @Test
    public void deleteArea() throws IOException {
//        Area area = new Area();
//        area.setCenter(new Point2D(0, 0));
//        area.setRadius(10f);
//        Area new_area = restAPI.createArea(area).execute().body();
//        Assert.assertNotNull(new_area.getId());
//        Response<Void> response = restAPI.deleteArea(new_area.getId()).execute();
//        Assert.assertTrue(response.isSuccessful());
//        List<Area> areas = restAPI.listAreas().execute().body();
//        boolean found = false;
//        for(Area saved_area: areas) {
//            if(saved_area.getId() == new_area.getId()) {
//                found = true;
//                break;
//            }
//        }
//        Assert.assertFalse(found);
    }
}
