package com.maryamaj.overlay.rest.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.responses.TextResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public abstract class RestAPIParamsJob<T> extends RestAPIJob<T> {
    private static final int PRIORITY = 1;

    public RestAPIParamsJob(String... tags) {
        super(new Params(PRIORITY).requireNetwork().persist().groupBy(RestClient.TAG).addTags(tags));
    }

}
