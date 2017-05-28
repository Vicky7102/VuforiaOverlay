package com.maryamaj.overlay.rest.jobs.area;

import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class UpdateAreaJob extends RestAPIParamsJob<Area> {
    private String uuid;

    public UpdateAreaJob(String uuid) {
        super(Area.TAG, uuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<Area> getResponse(Realm realm) throws IOException{
        Area area = realm.where(Area.class).equalTo("uuid", uuid).findFirst();
        Area unManagedArea = realm.copyFromRealm(area);
        return RestClient.getRestAPI().updateArea(unManagedArea.getUuid(), unManagedArea).execute();
    }
}
