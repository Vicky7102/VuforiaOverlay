package com.maryamaj.overlay.rest.jobs.text;

import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.TextResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class UpdateTextJob extends RestAPIParamsJob<TextResponse> {
    private String uuid;

    public UpdateTextJob(String uuid, String areaUuid) {
        super(Text.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<TextResponse> getResponse(Realm realm) throws IOException{
        Text text = realm.where(Text.class).equalTo("uuid", uuid).findFirst();
        Text unManagedText = realm.copyFromRealm(text);
        return RestClient.getRestAPI().updateText(unManagedText.getUuid(), unManagedText).execute();
    }
}
