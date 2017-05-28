package com.maryamaj.overlay.applicationmain;

import android.support.annotation.NonNull;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.scheduling.FrameworkJobSchedulerService;

public class JobManagerService extends FrameworkJobSchedulerService{
    @NonNull
    @Override
    protected JobManager getJobManager() {
        return OverlayApplication.getInstance().getJobManager();
    }
}