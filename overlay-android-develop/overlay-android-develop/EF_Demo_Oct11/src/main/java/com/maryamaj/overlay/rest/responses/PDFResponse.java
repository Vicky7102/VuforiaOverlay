package com.maryamaj.overlay.rest.responses;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 * Created by android on 20/4/17.
 */

public class PDFResponse {

    public static final String TAG = "PdfFile";
    @Expose
    public String uuid;
    @Expose
    public String pdf_file;
    @Expose
    public Position position;
    @Expose
    public String area;
    @Expose
    public String updated;

}

class Position {
    @Expose
    public double x;
    @Expose
    public double y;
}
