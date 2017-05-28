package com.maryamaj.overlay.rest.jobs.pdf;

import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.TagConstraint;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.models.PDFFile;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.jobs.RestAPIParamsJob;
import com.maryamaj.overlay.rest.responses.PDFResponse;
import com.maryamaj.overlay.rest.responses.TextResponse;

import java.io.IOException;

import io.realm.Realm;
import retrofit2.Response;

/**
 * Created by android on 20/4/17.
 */

public class ListPdfFiles extends RestAPIParamsJob<PDFResponse> {

    private String uuid;

    public ListPdfFiles(String uuid, String areaUuid) {
        super(PDFFile.TAG, uuid, areaUuid);
        this.uuid = uuid;
    }

    @Override
    protected Response getResponse(Realm realm) throws IOException {
        PDFFile text = realm.where(PDFFile.class).equalTo("uuid", uuid).findFirst();
        PDFFile unManagedText = realm.copyFromRealm(text);
        return RestClient.getRestAPI().listPDFFiles().execute();
    }

    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
        super.onCancel(cancelReason, throwable);
        OverlayApplication.getInstance().getJobManager().cancelJobsInBackground(null, TagConstraint.ALL, Text.TAG, uuid);
    }
}
