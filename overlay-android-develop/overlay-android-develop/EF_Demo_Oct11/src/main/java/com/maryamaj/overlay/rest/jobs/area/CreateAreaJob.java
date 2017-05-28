package com.maryamaj.overlay.rest.jobs.area;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class CreateAreaJob extends RestAPIParamsJob<Area> {
    private String uuid;

    public CreateAreaJob(String uuid) {
        super(Area.TAG, uuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<Area> getResponse(Realm realm) throws IOException{
        Area area = realm.where(Area.class).equalTo("uuid", uuid).findFirst();
        Area unManagedArea = realm.copyFromRealm(area);
        return RestClient.getRestAPI().createArea(unManagedArea).execute();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        super.onCancel(cancelReason, throwable);
        OverlayApplication.getInstance().getJobManager().cancelJobsInBackground(null, TagConstraint.ALL, Area.TAG, uuid);
    }
}
