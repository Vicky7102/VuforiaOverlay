package com.maryamaj.overlay.rest.jobs;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;
import com.maryamaj.overlay.rest.RestClient;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;


public abstract class RestAPIJob<T> extends Job {
    private static final String TAG = "RestAPIParamsJob";

    public RestAPIJob(Params params) {
        super(params);
    }

    @Override
    public void onAdded() {
        Log.d(TAG, "Job added: " + this.getClass().getName());
    }

    @Override
    public void onRun() throws Throwable {
        Log.e(TAG, "Job running: " + this.getClass().getName());
        try (Realm realm = Realm.getDefaultInstance()) {
            Response<T> response = getResponse(realm);
            //if (response != null) {
            if (!response.isSuccessful()) {
                throw new IllegalStateException(response.message());
            } else {
                realm.executeTransaction(realm1 -> onSuccess(response.body(), realm1));
            }
            // }
        }
    }

    protected void onSuccess(T response, Realm realm) {
    }

    protected abstract Response<T> getResponse(Realm realm) throws IOException;


    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        Log.d(TAG, "Job cancelled: " + this.getClass().getName(), throwable);
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        Log.d(TAG, "Job Rerun: " + this.getClass().getName() + " Run count: " + runCount + ", Max Run Count: " + maxRunCount, throwable);
        return RetryConstraint.CANCEL;
    }
}
