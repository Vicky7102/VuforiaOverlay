package com.maryamaj.overlay.rest.jobs.audio;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.AudioResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class DeleteAudioJob extends RestAPIParamsJob<AudioResponse> {
    private String uuid;

    public DeleteAudioJob(String uuid, String areaUuid) {
        super(Audio.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }


    @Override
    protected Response<AudioResponse> getResponse(Realm realm) throws IOException{
        return RestClient.getRestAPI().deleteAudio(uuid).execute();
    }
}
