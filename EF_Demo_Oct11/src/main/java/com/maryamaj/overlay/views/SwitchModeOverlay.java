package com.maryamaj.overlay.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

public class SwitchModeOverlay extends RelativeLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "SwitchModeOverlay";

    private GestureDetector clickDetector;
    private FloatingActionButton switchMode;
    private Context context;
    private TutorialManager tutorialManager;


    public SwitchModeOverlay(Context c) {
        this(c, null);
    }

    public SwitchModeOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        init();
    }

    private void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.switch_mode_overlay);
        clickDetector = new GestureDetector(context, new ClickDetector());
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        switchMode = new FloatingActionButton(context);
        switchMode.setId(View.generateViewId());
        if (tutorialManager.isBuildMode()) {
            switchMode.setImageResource(R.drawable.ic_remove_red_eye_white_24dp);
        } else {
            switchMode.setImageResource(R.drawable.ic_build_white_24dp);
        }
        switchMode.setSize(FloatingActionButton.SIZE_MINI);
        switchMode.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        switchMode.setColorFilter(Color.BLACK);
        switchMode.setElevation(10f);
        final LayoutParams switchParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        switchParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        switchParams.addRule(RelativeLayout.ALIGN_PARENT_START);
        switchParams.setMargins(GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 15), 0, 0);
        switchMode.setLayoutParams(switchParams);
        addView(switchMode);

        switchMode.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clickDetector.onTouchEvent(event)) {
                    if (tutorialManager.isBuildMode()) {
                        tutorialManager.setBuildMode(false);
                    } else {
                        tutorialManager.setBuildMode(true);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
    }

    @Override
    public void onPauseCamera() {
        setVisibility(GONE);
    }

    @Override
    public void onResumeCamera() {
        setVisibility(VISIBLE);
    }

    @Override
    public void onAreaChange(Area area) {
    }

    @Override
    public void onModeChange(boolean buildMode) {
        if (buildMode) {
            switchMode.setImageResource(R.drawable.ic_remove_red_eye_white_24dp);
        } else {
            switchMode.setImageResource(R.drawable.ic_build_white_24dp);
        }
    }

    @Override
    public void editMode() {

    }
}
