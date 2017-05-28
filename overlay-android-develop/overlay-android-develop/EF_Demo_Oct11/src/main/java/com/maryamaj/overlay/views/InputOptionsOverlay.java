package com.maryamaj.overlay.views;

import com.maryamaj.overlay.api.ARListener;
//import com.maryamaj.overlay.utils.PlayFeature;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import io.realm.Realm;


public class InputOptionsOverlay extends RelativeLayout implements ARListener, TutorialManagerListener {

    private static final String TAG = "InputOptionsOverlay";

    private GestureDetector clickDetector;
    private FloatingActionButton editOptions;
    private Context context;
    private FloatingActionButton text, audio, sketch, area;
    private FloatingActionButton pdf;
    private TutorialManager tutorialManager;

    public InputOptionsOverlay(Context c) {
        this(c, null);
    }

    public InputOptionsOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        init();
    }

    private void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.input_options_overlay);
        clickDetector = new GestureDetector(context, new ClickDetector());
        this.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        Realm realm = Realm.getDefaultInstance();

        if (!tutorialManager.isBuildMode())
            setVisibility(GONE);

        editOptions = new FloatingActionButton(context);
        editOptions.setId(View.generateViewId());
        editOptions.setImageResource(R.drawable.ic_edit_white_24dp);
        editOptions.setRippleColor(Color.WHITE);
        editOptions.setSize(FloatingActionButton.SIZE_MINI);
        editOptions.setColorFilter(Color.RED);
        editOptions.setElevation(10f);
        LayoutParams editParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        editParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        editParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        editParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 15));
        editOptions.setLayoutParams(editParams);
       // editOptions.setVisibility(View.GONE);
        if(realm.isEmpty()) {
            editOptions.setVisibility(View.GONE);
        }else{
            editOptions.setVisibility(View.VISIBLE);
        }
        addView(editOptions);

        area = new FloatingActionButton(context);
        area.setId(View.generateViewId());
        area.setTag(R.id.area_overlay);
        area.setImageResource(R.drawable.ic_add_white_24dp);
        area.setColorFilter(Color.BLACK);
        area.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        area.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams areaParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        areaParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        areaParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        areaParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 15));
        area.setLayoutParams(areaParams);
      //  area.setVisibility(View.VISIBLE);
        if(realm.isEmpty()) {
            area.setVisibility(View.VISIBLE);
        }else{
            area.setVisibility(View.GONE);
        }


        text = new FloatingActionButton(context);
        text.setId(View.generateViewId());
        text.setTag(R.id.text_overlay);
        text.setImageResource(R.drawable.ic_title_white_24dp);
        text.setColorFilter(Color.BLACK);
        text.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        text.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams tParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tParams.addRule(RelativeLayout.ABOVE, editOptions.getId());
        tParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        tParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 7));
        text.setLayoutParams(tParams);
        text.setVisibility(View.GONE);

        audio = new FloatingActionButton(context);
        audio.setId(View.generateViewId());
        audio.setTag(R.id.audio_overlay);
        audio.setImageResource(R.drawable.ic_fiber_manual_record_white_24dp);
        audio.setColorFilter(Color.RED);
        audio.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        audio.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams aParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        aParams.addRule(RelativeLayout.ABOVE, text.getId());
        aParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        aParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 7));
        audio.setLayoutParams(aParams);
        audio.setVisibility(View.GONE);

        sketch = new FloatingActionButton(context);
        sketch.setId(View.generateViewId());
        sketch.setTag(R.id.sketch_overlay);
        sketch.setImageResource(R.drawable.ic_gesture_white_24dp);
        sketch.setColorFilter(Color.GREEN);
        sketch.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        sketch.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams sParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        sParams.addRule(RelativeLayout.ABOVE, audio.getId());
        sParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        sParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 7));
        sketch.setLayoutParams(sParams);
        sketch.setVisibility(View.GONE);


        pdf = new FloatingActionButton(context);
        pdf.setId(View.generateViewId());
        pdf.setTag(R.id.pdf_overlay);
        pdf.setImageResource(R.drawable.ic_attach_file_24dp);
        pdf.setColorFilter(Color.RED);
        pdf.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        pdf.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams pParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        pParams.addRule(RelativeLayout.ABOVE, sketch.getId());
        pParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        pParams.setMargins(0, 0, GeometryUtils.dpToPx(context, 15), GeometryUtils.dpToPx(context, 7));
        pdf.setLayoutParams(pParams);
        pdf.setVisibility(View.GONE);

        addView(text);
        addView(audio);
        addView(sketch);
        addView(area);
        addView(pdf);


        OnTouchListener buttonOnTouch = new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clickDetector.onTouchEvent(event)) {
                    TutorialManager.getInstance().startEditMode((int) v.getTag());
                }
                return true;
            }
        };

        text.setOnTouchListener(buttonOnTouch);
        sketch.setOnTouchListener(buttonOnTouch);
        audio.setOnTouchListener(buttonOnTouch);
        area.setOnTouchListener(buttonOnTouch);
        pdf.setOnTouchListener(buttonOnTouch);


        editOptions.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (clickDetector.onTouchEvent(event)) {
                    if (text.getVisibility() == View.GONE) {
                        text.setVisibility(View.VISIBLE);
                        audio.setVisibility(View.VISIBLE);
                        sketch.setVisibility(View.VISIBLE);
                        pdf.setVisibility(View.VISIBLE);
                    } else {
                        text.setVisibility(View.GONE);
                        audio.setVisibility(View.GONE);
                        sketch.setVisibility(View.GONE);
                        pdf.setVisibility(View.GONE);
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
        if (tutorialManager.isBuildMode()) {
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void onAreaChange(Area newArea) {
        if (newArea == null) {
            editOptions.setVisibility(GONE);
            text.setVisibility(GONE);
            sketch.setVisibility(GONE);
            audio.setVisibility(GONE);
            pdf.setVisibility(GONE);

            area.setVisibility(VISIBLE);
        } else {
            editOptions.setVisibility(VISIBLE);
            area.setVisibility(GONE);
        }
    }

    @Override
    public void onModeChange(boolean buildMode) {
        if (buildMode) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }

    @Override
    public void editMode() {

    }
}
