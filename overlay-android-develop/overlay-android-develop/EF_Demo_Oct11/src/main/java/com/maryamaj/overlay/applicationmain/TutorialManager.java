package com.maryamaj.overlay.applicationmain;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.util.SparseArray;

import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.rest.RestClient;

import java.util.List;

import io.realm.RealmResults;

public class TutorialManager {
    private static final String TAG = "TutorialManager";
    private RealmResults<Area> areas;
    private Area activeArea;
    private static TutorialManager instance;
    private Control control;
    private Context context;
    private SparseArray<TutorialManagerListener> listeners;
    private boolean buildMode = false;

    private TutorialManager(Context context) {
        this.context = context;
        control = ((Control) context);
        areas = control.getRealm().where(Area.class).findAllSorted("sequence");
        RestClient.reloadTutorial(0);
        listeners = new SparseArray<>();
    }

    public static void init(Context context) {
        instance = new TutorialManager(context);
    }

    public static TutorialManager getInstance() {
        if (instance == null) {
            throw new RuntimeException("Call init() first");
        }
        return instance;
    }

    public void register(TutorialManagerListener listener, int id) {
        listeners.append(id, listener);
    }

    public void addArea(Area area) {
        RestClient.createArea(area);
        setActiveArea(area);
    }

    public void removeArea(Area area) {
        Log.d(TAG, "Removing area: " + area.getUuid());
        control.getRealm().executeTransaction(realm -> {
            for(Area nextArea: realm.where(Area.class).greaterThan("sequence", area.getSequence()).findAll()) {
                nextArea.setSequence(nextArea.getSequence() - 1);
            }
        });
        RestClient.deleteArea(area);
        control.getRealm().executeTransaction(realm -> areas.deleteFromRealm(area.getSequence() - 1));
        setActiveArea(null);
    }

    public void changeAreaSeq(int oldSeq, int newSeq) {
        if(oldSeq == newSeq)
            return;
        Area area = areas.get(oldSeq - 1);
        control.getRealm().executeTransaction(realm -> {
            if(oldSeq < newSeq) {
                for(Area nextArea: realm.where(Area.class).greaterThan("sequence", oldSeq).lessThanOrEqualTo("sequence", newSeq).findAll()) {
                    nextArea.setSequence(nextArea.getSequence() - 1);
                }
            }
            else if(oldSeq > newSeq) {
                for(Area nextArea: realm.where(Area.class).lessThan("sequence", oldSeq).greaterThanOrEqualTo("sequence", newSeq).findAll()) {
                    nextArea.setSequence(nextArea.getSequence() + 1);
                }
            }
            area.setSequence(newSeq);
        });
        RestClient.updateArea(area);
    }

    public boolean hasNext() {
        return activeArea != null && areas.size() > activeArea.getSequence();
    }

    public void selectNext() {
        if (hasNext()) {
            setActiveArea(areas.get(activeArea.getSequence()));
        }
    }

    public boolean hasPrevious() {
        return activeArea != null && activeArea.getSequence() != 1;
    }

    public void selectPrevious() {
        if (hasPrevious()) {
            setActiveArea(areas.get(activeArea.getSequence() - 2));
        }
    }

    public Area getActiveArea() {
        return activeArea;
    }

    public void setActiveArea(Area activeArea) {
        this.activeArea = activeArea;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.valueAt(i).onAreaChange(activeArea);
        }
    }

    public void startEditMode(int id) {

        listeners.get(id).editMode();
    }

    public List<Area> getAreas() {
        return areas;
    }

    public boolean isBuildMode() {
        return buildMode;
    }

    public void setBuildMode(boolean buildMode) {
        this.buildMode = buildMode;
        for (int i = 0; i < listeners.size(); i++) {
            listeners.valueAt(i).onModeChange(buildMode);
        }
    }

    public void deleteAreasFromRealm(List<Area> areasToDelete) {
        ((Activity) context).runOnUiThread(() -> {
            control.getRealm().executeTransaction(realm -> {
                for(Area areaToDelete: areasToDelete) {
                    areas.deleteFromRealm(areaToDelete.getSequence() - 1);
                }
            });
        });
    }
}
