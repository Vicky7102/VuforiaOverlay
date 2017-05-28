package com.maryamaj.overlay.rest.jobs;

import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.maryamaj.overlay.applicationmain.OverlayApplication;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.Sketch;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.responses.AreaDetailResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import retrofit2.Response;


public class ReloadTutorialJob extends RestAPIJob<List<AreaDetailResponse>> {
    private static final String TAG = "ReloadTutorialJob";
    private int tutorialId;
    private static final int PRIORITY = 1;

    public ReloadTutorialJob(int tutorialId) {
        super(new Params(PRIORITY).persist().groupBy(RestClient.TAG));
        this.tutorialId = tutorialId;
    }

    @Override
    protected void onSuccess(List<AreaDetailResponse> areaDetailResponses, Realm realm) {
        realm.delete(Area.class);
        realm.delete(Text.class);
        realm.delete(Audio.class);
        realm.delete(Sketch.class);
        Set<String> areas = new HashSet<>();
        Set<String> texts = new HashSet<>();
        Set<String> audios = new HashSet<>();
        Set<String> sketches = new HashSet<>();
        for (AreaDetailResponse areaDetailResponse : areaDetailResponses) {
            Area area = areaDetailResponse.getArea();
            realm.copyToRealmOrUpdate(area);
            areas.add(area.getUuid());
            for (Text text : areaDetailResponse.getTexts()) {
                realm.copyToRealmOrUpdate(text);
                texts.add(text.getUuid());
            }
            for (Audio audio : areaDetailResponse.getAudios()) {
                audio.downloadToDevice();
                realm.copyToRealmOrUpdate(audio);
                audios.add(audio.getUuid());
            }
            for (Sketch sketch : areaDetailResponse.getSketches()) {
                realm.copyToRealmOrUpdate(sketch);
                sketches.add(sketch.getUuid());
            }
        }
        List<Area> areasToDelete = new ArrayList<>();
        for(Area realmArea: realm.where(Area.class).findAll()) {
            if(!areas.contains(realmArea.getUuid()))
                areasToDelete.add(realmArea);
        }
        for(Text realmText: realm.where(Text.class).findAll()) {
            if(!texts.contains(realmText.getUuid()))
                realmText.deleteFromRealm();
        }
        for(Audio realmAudio: realm.where(Audio.class).findAll()) {
            if(!audios.contains(realmAudio.getUuid()))
                realmAudio.deleteFromRealm();
        }
        for(Sketch realmSketch: realm.where(Sketch.class).findAll()) {
            if(!sketches.contains(realmSketch.getUuid()))
                realmSketch.deleteFromRealm();
        }
        TutorialManager.getInstance().deleteAreasFromRealm(areasToDelete);
    }

    @Override
    protected Response<List<AreaDetailResponse>> getResponse(Realm realm) throws IOException{
        return RestClient.getRestAPI().listAreasDetail().execute();
    }

    @Override
    public void onRun() throws Throwable {
        if(OverlayApplication.getInstance().getJobManager().count() > 0)
            throw new IllegalStateException("Cannot run this job as others are scheduled ahead");
        super.onRun();
    }
}
