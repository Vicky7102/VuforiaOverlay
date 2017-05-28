package com.maryamaj.overlay.rest.jobs.audio;

import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.AudioResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public class UpdateAudioPositionJob extends RestAPIParamsJob<AudioResponse> {
    private String uuid;

    public UpdateAudioPositionJob(String uuid, String areaUuid) {
        super(Audio.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }


    @Override
    protected Response<AudioResponse> getResponse(Realm realm) throws IOException{
        Audio audio = realm.where(Audio.class).equalTo("uuid", uuid).findFirst();
        return RestClient.getRestAPI().updateAudioPosition(audio.getUuid(), audio.getPosition().x, audio.getPosition().y).execute();
    }
}
