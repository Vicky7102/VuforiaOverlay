package com.maryamaj.overlay.rest.responses;

public class TextResponse {
    public String uuid;
    public String text;
    public String area;
    public TextPoint position;
}

class TextPoint {
    public float x, y;
}