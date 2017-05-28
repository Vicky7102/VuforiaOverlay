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


public class DeleteSketchJob extends RestAPIParamsJob<SketchResponse> {
    private String uuid;

    public DeleteSketchJob(String uuid, String areaUuid) {
        super(Sketch.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<SketchResponse> getResponse(Realm realm) throws IOException{
        return RestClient.getRestAPI().deleteSketch(uuid).execute();
    }

}
