package com.maryamaj.overlay.applicationmain;

import android.app.Application;
import android.os.Build;
import android.util.Log;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.config.Configuration;
import com.birbit.android.jobqueue.log.CustomLogger;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class OverlayApplication extends Application {
    private static OverlayApplication instance;
    private JobManager jobManager;

    public OverlayApplication() {
        instance = this;
    }

    public static OverlayApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);


    }

    private void configureJobManager() {
        Configuration.Builder builder = new Configuration.Builder(this)
                .minConsumerCount(1)//always keep at least one consumer alive
                .maxConsumerCount(1)//up to 3 consumers at a time
                .loadFactor(1)//3 jobs per consumer
                .consumerKeepAlive(120);//wait 2 minute
        builder.scheduler(FrameworkJobSchedulerService.createSchedulerFor(this, JobManagerService.class), true);
        jobManager = new JobManager(builder.build());
    }

    public synchronized JobManager getJobManager() {
        if (jobManager == null) {
            configureJobManager();
        }
        return jobManager;
    }
}
