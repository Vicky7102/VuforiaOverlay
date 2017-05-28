package com.maryamaj.overlay.rest.jobs.audio;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.AudioResponse;

import org.apache.commons.io.IOUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import io.realm.Realm;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;


public class CreateAudioJob extends RestAPIParamsJob<AudioResponse> {
    private String uuid;

    public CreateAudioJob(String uuid, String areaUuid) {
        super(Audio.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response<AudioResponse> getResponse(Realm realm) throws IOException{
        Audio audio = realm.where(Audio.class).equalTo("uuid", uuid).findFirst();
        InputStream inputStream = new FileInputStream(audio.getAudioFileLocal());
        MediaType MEDIA_TYPE_MP3 = MediaType.parse("audio/mpeg");
        RequestBody requestBody = RequestBody.create(MEDIA_TYPE_MP3, IOUtils.toByteArray(inputStream));
        MultipartBody.Part body = MultipartBody.Part.createFormData("audio_file", "audio.mp3", requestBody);
        RequestBody uuidBody = RequestBody.create(MediaType.parse("text/plain"), audio.getUuid());
        RequestBody areaUuidBody = RequestBody.create(MediaType.parse("text/plain"), audio.getArea().getUuid());
        return RestClient.getRestAPI().createAudio(uuidBody, areaUuidBody, audio.getPosition().x, audio.getPosition().y, body).execute();
    }

    @Override
    protected void onSuccess(AudioResponse response, Realm realm) {
        Audio audio = realm.where(Audio.class).equalTo("uuid", uuid).findFirst();
        audio.setAudioFile(response.audio_file);
        audio.transferToExternalStorage();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        super.onCancel(cancelReason, throwable);
        OverlayApplication.getInstance().getJobManager().cancelJobsInBackground(null, TagConstraint.ALL, Audio.TAG, uuid);
    }
}
