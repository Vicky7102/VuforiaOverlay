package com.maryamaj.overlay.rest.jobs.area;

import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class DeleteAreaJob extends RestAPIParamsJob<Void> {
    private String uuid;

    public DeleteAreaJob(String uuid) {
        super(Area.TAG, uuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<Void> getResponse(Realm realm) throws IOException{
        return RestClient.getRestAPI().deleteArea(uuid).execute();
    }
}
