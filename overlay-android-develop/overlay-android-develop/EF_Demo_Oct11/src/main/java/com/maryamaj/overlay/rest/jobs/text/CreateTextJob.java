package com.maryamaj.overlay.rest.jobs.text;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.TextResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class CreateTextJob extends RestAPIParamsJob<TextResponse> {
    private String uuid;

    public CreateTextJob(String uuid, String areaUuid) {
        super(Text.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<TextResponse> getResponse(Realm realm) throws IOException {
        Text text = realm.where(Text.class).equalTo("uuid", uuid).findFirst();
        Text unManagedText = realm.copyFromRealm(text);
        return RestClient.getRestAPI().createText(unManagedText).execute();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        super.onCancel(cancelReason, throwable);
        OverlayApplication.getInstance().getJobManager().cancelJobsInBackground(null, TagConstraint.ALL, Text.TAG, uuid);
    }
}
