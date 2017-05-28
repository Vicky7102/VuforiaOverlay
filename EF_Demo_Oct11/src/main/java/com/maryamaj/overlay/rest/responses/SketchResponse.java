package com.maryamaj.overlay.rest.responses;

import java.util.ArrayList;

public class SketchResponse {
    public String uuid;
    public ArrayList<SketchPointResponse> points;
    public String area;
}

class SketchPointResponse {
    public float x;
    public float y;
    public boolean is_initial;
}