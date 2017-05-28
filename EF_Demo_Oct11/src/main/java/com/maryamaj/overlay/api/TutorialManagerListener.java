package com.maryamaj.overlay.api;

import com.maryamaj.overlay.models.Area;

public interface TutorialManagerListener {
    void onAreaChange(Area area);
    void onModeChange(boolean buildMode);
    void editMode();
}

