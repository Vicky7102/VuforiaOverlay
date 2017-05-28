package com.maryamaj.overlay.rest.jobs.sketch;

import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.SketchResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class UpdateSketchJob extends RestAPIParamsJob<SketchResponse> {
    private String uuid;

    public UpdateSketchJob(String uuid, String areaUuid) {
        super(Sketch.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<SketchResponse> getResponse(Realm realm) throws IOException{
        Sketch sketch = realm.where(Sketch.class).equalTo("uuid", uuid).findFirst();
        Sketch unManagedSketch = realm.copyFromRealm(sketch);
        return RestClient.getRestAPI().updateSketch(unManagedSketch.getUuid(), unManagedSketch).execute();
    }
}
