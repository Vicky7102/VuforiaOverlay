package com.maryamaj.overlay.rest;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.PDFFile;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.jobs.ReloadTutorialJob;
import com.maryamaj.overlay.rest.jobs.area.CreateAreaJob;
import com.maryamaj.overlay.rest.jobs.area.DeleteAreaJob;
import com.maryamaj.overlay.rest.jobs.area.UpdateAreaJob;
import com.maryamaj.overlay.rest.jobs.audio.CreateAudioJob;
import com.maryamaj.overlay.rest.jobs.audio.DeleteAudioJob;
import com.maryamaj.overlay.rest.jobs.audio.UpdateAudioFileJob;
import com.maryamaj.overlay.rest.jobs.audio.UpdateAudioPositionJob;
import com.maryamaj.overlay.rest.jobs.sketch.CreateSketchJob;
import com.maryamaj.overlay.rest.jobs.sketch.DeleteSketchJob;
import com.maryamaj.overlay.rest.jobs.sketch.UpdateSketchJob;
import com.maryamaj.overlay.rest.jobs.text.CreateTextJob;
import com.maryamaj.overlay.rest.jobs.text.DeleteTextJob;
import com.maryamaj.overlay.rest.jobs.text.UpdateTextJob;
import com.maryamaj.overlay.rest.responses.PDFResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestClient {
    public static final String TAG = "RestClient";
    private static RestAPI restAPI;
    private static JobManager jobManager = OverlayApplication.getInstance().getJobManager();

    static {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
        Retrofit retrofit = new Retrofit.Builder()
//                .baseUrl("http://ar.talkai.xyz/")
                .baseUrl("http://52.77.223.41/")
//                .baseUrl("https://3c3f5bdb.ngrok.io/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        restAPI = retrofit.create(RestAPI.class);
    }

    public static RestAPI getRestAPI() {
        return restAPI;
    }

    public static void reloadTutorial(int tutorialId) {
        jobManager.addJobInBackground(new ReloadTutorialJob(tutorialId));
    }

    public static void createArea(final Area area) {
        jobManager.addJobInBackground(new CreateAreaJob(area.getUuid()));
    }

    public static void updateArea(final Area area) {
        jobManager.addJobInBackground(new UpdateAreaJob(area.getUuid()));
    }

    public static void deleteArea(final Area area) {
        String uuid = area.getUuid();
        jobManager.cancelJobsInBackground(cancelResult -> {
            boolean created = true;
            for (Job job : cancelResult.getCancelledJobs()) {
                if (job instanceof CreateAreaJob) {
                    created = false;
                    break;
                }
            }
            if(created) {
                jobManager.addJobInBackground(new DeleteAreaJob(uuid));
            }
        }, TagConstraint.ALL, uuid);
     //   jobManager.addJobInBackground(new DeleteAreaJob(uuid));
    }

    public static void createText(final Text text) {
        jobManager.addJobInBackground(new CreateTextJob(text.getUuid(), text.getArea().getUuid()));
    }

    public static void updateText(final Text text) {
        jobManager.addJobInBackground(new UpdateTextJob(text.getUuid(), text.getArea().getUuid()));
    }

    public static void deleteText(final Text text) {
        String uuid = text.getUuid();
        jobManager.cancelJobsInBackground(cancelResult -> {
//            boolean created = true;
//            for (Job job : cancelResult.getCancelledJobs()) {
//                if (job instanceof CreateTextJob) {
//                    created = false;
//                    break;
//                }
//            }
//            if(created) {
//                jobManager.addJob(new DeleteTextJob(uuid));
//            }
        }, TagConstraint.ALL, Text.TAG, uuid);
        jobManager.addJobInBackground(new DeleteTextJob(uuid, text.getArea().getUuid()));
    }

    public static void createAudio(final Audio audio) {
        jobManager.addJobInBackground(new CreateAudioJob(audio.getUuid(), audio.getArea().getUuid()));
    }

    public static void updateAudio(final Audio audio, boolean updatePosition) {
        if (updatePosition) {
            jobManager.addJobInBackground(new UpdateAudioPositionJob(audio.getUuid(), audio.getArea().getUuid()));
        } else {
            jobManager.addJobInBackground(new UpdateAudioFileJob(audio.getUuid(), audio.getArea().getUuid()));
        }
    }

    public static void deleteAudio(final Audio audio) {
        String uuid = audio.getUuid();
        jobManager.cancelJobsInBackground(cancelResult -> {
//            boolean created = true;
//            for (Job job : cancelResult.getCancelledJobs()) {
//                if (job instanceof CreateAudioJob) {
//                    created = false;
//                    break;
//                }
//            }
//            if(created) {
//                jobManager.addJob(new DeleteAudioJob(uuid));
//            }
        }, TagConstraint.ALL, Audio.TAG, uuid);
        jobManager.addJobInBackground(new DeleteAudioJob(uuid, audio.getArea().getUuid()));
    }

    public static void createSketch(final Sketch sketch) {
        jobManager.addJobInBackground(new CreateSketchJob(sketch.getUuid(), sketch.getArea().getUuid()));
    }

    public static void updateSketch(final Sketch sketch) {
        jobManager.addJobInBackground(new UpdateSketchJob(sketch.getUuid(), sketch.getArea().getUuid()));
    }

    public static void deleteSketch(final Sketch sketch) {
        String uuid = sketch.getUuid();
        jobManager.cancelJobsInBackground(cancelResult -> {
//            boolean created = true;
//            for (Job job : cancelResult.getCancelledJobs()) {
//                if (job instanceof CreateSketchJob) {
//                    created = false;
//                    break;
//                }
//            }
//            if(created) {
//                jobManager.addJob(new DeleteSketchJob(uuid));
//            }
        }, TagConstraint.ALL, Sketch.TAG, uuid);
        jobManager.addJobInBackground(new DeleteSketchJob(uuid, sketch.getArea().getUuid()));
    }


    public static List<PDFResponse> listPDF(final PDFFile file) {

        List<PDFResponse> files = new ArrayList<>();

        jobManager.cancelJobsInBackground(cancelResult -> {

        }, TagConstraint.ALL, PDFResponse.TAG);

        try {
            retrofit2.Response<List<PDFResponse>> response = RestClient.getRestAPI().listPDFFiles().execute();

            files = response.body();

            System.out.println("response.body() : " + response.body());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return files;
    }

    public static void createPdf(final PDFFile pdfFile) {
        //jobManager.addJobInBackground(new CreateAudioJob(pdfFile.getUuid(), pdfFile.getArea().getUuid()));
    }
}
