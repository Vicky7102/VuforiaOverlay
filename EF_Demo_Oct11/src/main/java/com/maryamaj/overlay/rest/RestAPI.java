package com.maryamaj.overlay.rest;

import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.responses.AreaDetailResponse;
import com.maryamaj.overlay.rest.responses.AudioResponse;
import com.maryamaj.overlay.rest.responses.PDFResponse;
import com.maryamaj.overlay.rest.responses.SketchResponse;
import com.maryamaj.overlay.rest.responses.TextResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface RestAPI {
    @GET("api/area")
    Call<List<Area>> listAreas();

    @GET("api/area_detail")
    Call<List<AreaDetailResponse>> listAreasDetail();

    @POST("api/area")
    Call<Area> createArea(@Body Area area);

    @PUT("api/area/{uuid}")
    Call<Area> updateArea(@Path("uuid") String uuid, @Body Area area);

    @DELETE("api/area/{uuid}")
    Call<Void> deleteArea(@Path("uuid") String uuid);

    @GET("api/text")
    Call<List<Text>> listTexts();

    @POST("api/text")
    Call<TextResponse> createText(@Body Text text);

    @PUT("api/text/{uuid}")
    Call<TextResponse> updateText(@Path("uuid") String uuid, @Body Text text);

    @DELETE("api/text/{uuid}")
    Call<TextResponse> deleteText(@Path("uuid") String uuid);

    @GET("api/audio")
    Call<List<Audio>> listAudios();

    @Multipart
    @POST("api/audio")
    Call<AudioResponse> createAudio(@Part("uuid") RequestBody uuid, @Part("area") RequestBody area, @Part("position.x") float x, @Part("position.y") float y, @Part MultipartBody.Part audio_file);

    @Multipart
    @PATCH("api/audio/{uuid}")
    Call<AudioResponse> updateAudioFile(@Path("uuid") String uuid, @Part MultipartBody.Part audio_file);

    @Multipart
    @PATCH("api/audio/{uuid}")
    Call<AudioResponse> updateAudioPosition(@Path("uuid") String uuid, @Part("position.x") float x, @Part("position.y") float y);

    @DELETE("api/audio/{uuid}")
    Call<AudioResponse> deleteAudio(@Path("uuid") String uuid);

    @GET("api/sketch")
    Call<List<Sketch>> listSketches();

    @POST("api/sketch")
    Call<SketchResponse> createSketch(@Body Sketch sketch);

    @PUT("api/sketch/{uuid}")
    Call<SketchResponse> updateSketch(@Path("uuid") String uuid, @Body Sketch sketch);

    @DELETE("api/sketch/{uuid}")
    Call<SketchResponse> deleteSketch(@Path("uuid") String uuid);

    @GET("api/pdf")
    Call<List<PDFResponse>> listPDFFiles();


}