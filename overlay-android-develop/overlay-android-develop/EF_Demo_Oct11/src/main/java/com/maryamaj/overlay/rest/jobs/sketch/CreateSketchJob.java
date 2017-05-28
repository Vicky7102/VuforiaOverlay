package com.maryamaj.overlay.rest.jobs.sketch;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.SketchResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class CreateSketchJob extends RestAPIParamsJob<SketchResponse> {
    private String uuid;

    public CreateSketchJob(String uuid, String areaUuid) {
        super(Sketch.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<SketchResponse> getResponse(Realm realm) throws IOException{
        Sketch sketch = realm.where(Sketch.class).equalTo("uuid", uuid).findFirst();
        Sketch unManagedSketch = realm.copyFromRealm(sketch);
        return RestClient.getRestAPI().createSketch(unManagedSketch).execute();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        super.onCancel(cancelReason, throwable);
        OverlayApplication.getInstance().getJobManager().cancelJobsInBackground(null, TagConstraint.ALL, Sketch.TAG, uuid);
    }
}
