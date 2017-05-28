package com.maryamaj.overlay.rest.responses;

public class AudioResponse {
    public String uuid;
    public String audio_file;
    public String area;
    public AudioPointResponse position;
}

class AudioPointResponse {
    public float x;
    public float y;
}
