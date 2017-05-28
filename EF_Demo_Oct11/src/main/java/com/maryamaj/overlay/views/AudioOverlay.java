package com.maryamaj.overlay.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.gestures.DragDropListener;
import com.maryamaj.overlay.gestures.LongPressDragDrop;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import io.realm.RealmQuery;


public class AudioOverlay extends FrameLayout implements ARListener, TutorialManagerListener {
    private static final String TAG = "AudioOverlay";
    private static final int PLAY_ID = 2;
    private static final int STOPPLAY_ID = 3;

    private Context context;
    private Control control;
    private GestureDetector clickDetector;
    private LongPressDragDrop longPressDragDrop;
    private TutorialManager tutorialManager;
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer;
    private Audio audio;
    private FloatingActionButton playBtn, stopRecordBtn;
    private String audioFilePath;

    public AudioOverlay(Context c) {
        this(c, null);
    }

    public AudioOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    public void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.audio_overlay);
        mPlayer = new MediaPlayer();
        mRecorder = new MediaRecorder();
        clickDetector = new GestureDetector(context, new ClickDetector());
        longPressDragDrop = new LongPressDragDrop(this, new MyDrapDropListener());

        playBtn = new FloatingActionButton(context);
        playBtn.setId(View.generateViewId());
        playBtn.setTag(PLAY_ID);
        playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        playBtn.setColorFilter(Color.RED);
        playBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        playBtn.setSize(FloatingActionButton.SIZE_MINI);
        final LayoutParams audioBtnParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        playBtn.setLayoutParams(audioBtnParams);
        playBtn.setVisibility(View.GONE);


        stopRecordBtn = new FloatingActionButton(context);
        stopRecordBtn.setId(View.generateViewId());
        stopRecordBtn.setImageResource(R.drawable.ic_stop_white_24dp);
        stopRecordBtn.setColorFilter(Color.RED);
        stopRecordBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        stopRecordBtn.setSize(FloatingActionButton.SIZE_MINI);
        LayoutParams recordBtnParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        recordBtnParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        recordBtnParams.setMargins(0, 0, 0, GeometryUtils.dpToPx(context, 15));
        stopRecordBtn.setLayoutParams(recordBtnParams);
        stopRecordBtn.setVisibility(View.GONE);

        addView(playBtn);
        addView(stopRecordBtn);

        OnTouchListener buttonOnTouch = (v, event) -> {
            if (tutorialManager.isBuildMode()) {
                longPressDragDrop.onMotionEvent(event);
            }
            if (!longPressDragDrop.isDragging() && clickDetector.onTouchEvent(event)) {
                v.performClick();
            }
            return true;
        };

        playBtn.setOnTouchListener(buttonOnTouch);
        stopRecordBtn.setOnTouchListener(buttonOnTouch);
        playBtn.setOnClickListener(v -> {
            switch ((int) (playBtn.getTag())) {
                case PLAY_ID:
                    startPlaying();
                    break;
                case STOPPLAY_ID:
                    stopPlaying();
            }
        });

        stopRecordBtn.setOnClickListener(v -> stopRecording());

        control.getRealm().where(Audio.class).findAll().addChangeListener(element -> {
            if (audio != null && !audio.isValid()) {
                onAudioDeletion();
                Log.d(TAG, "Audio deletion called due to update");
            }
        });
    }

    private void startPlaying() {
        try {
            mPlayer.setDataSource(audio.getAudioFileLocal());
            mPlayer.setOnCompletionListener(mp -> stopPlaying());
            mPlayer.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Setting media player failed", e);
        }
        playBtn.setImageResource(R.drawable.ic_stop_white_24dp);
        playBtn.setTag(STOPPLAY_ID);
        mPlayer.start();
    }

    private void stopPlaying() {
        playBtn.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        playBtn.setTag(PLAY_ID);
        mPlayer.reset();
    }

    private void startRecording() {

        if (audio == null) {

            try {
                audioFilePath = File.createTempFile("audio", ".mp3").getAbsolutePath();
            } catch (IOException e) {
                Log.e(TAG, "Failed to create new temp audio file");
                return;
            }
        } else {
            audioFilePath = audio.getAudioFileLocal();
        }

        if(MediaRecorder.AudioSource.MIC != -1) {
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setOutputFile(audioFilePath);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        }
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "Setting media recorder failed", e);
        }

        mRecorder.start();
    }

    private void stopRecording() {
        mRecorder.stop();
        if (audio == null) {
            control.getRealm().executeTransaction(realm -> {
                Area area = tutorialManager.getActiveArea();
                audio = realm.createObject(Audio.class, UUID.randomUUID().toString());
                audio.setArea(area);
                Point2D position = GeometryUtils.translate(area.getCenter(), new Point2D(area.getRadius() + 10, area.getRadius() + 10));
                audio.setPosition(realm.copyToRealm(position));
                audio.setAudioFileLocal(audioFilePath);
            });
            RestClient.createAudio(audio);
        } else {
            RestClient.updateAudio(audio, false);
        }

        stopRecordBtn.setVisibility(View.GONE);
        control.resumeCamera();
        playBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onAreaChange(Area area) {
        if (area == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        RealmQuery<Audio> audioRealmQuery = control.getRealm().where(Audio.class).equalTo("area.uuid", area.getUuid());
        if (audioRealmQuery.count() == 0) {
            audio = null;
            playBtn.setVisibility(View.GONE);
        } else {
            audio = audioRealmQuery.findFirst();
            positionPlayBtn();
            playBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onModeChange(boolean buildMode) {

    }

    @Override
    public void editMode() {
        control.pauseCamera();
        playBtn.setVisibility(View.GONE);
        startRecording();
        stopRecordBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPauseCamera() {
        if (!longPressDragDrop.isDragging())
            playBtn.setVisibility(View.GONE);
    }

    @Override
    public void onResumeCamera() {
        if (audio != null)
            playBtn.setVisibility(View.VISIBLE);
    }

    public void positionPlayBtn() {
        Point2D position = audio.getPositionAt(control.getDepth());
        if (control.getMarkerCenter() != null)
            position = GeometryUtils.translate(position, control.getMarkerCenter());
        playBtn.setX(position.x);
        playBtn.setY(position.y);
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {
        post(() -> {
            if (audio != null && audio.isValid()) {
                positionPlayBtn();
            }
        });
    }

    private void onAudioDeletion() {
        playBtn.setVisibility(View.GONE);
        audio = null;
    }

    private class MyDrapDropListener extends DragDropListener {

        @Override
        public void onStartDrag(Point2D start) {

        }

        @Override
        public void onMove(Point2D delta) {
            playBtn.setX(playBtn.getX() + delta.x);
            playBtn.setY(playBtn.getY() + delta.y);
        }

        @Override
        public void onDrop() {
            Point2D curPosition = new Point2D(playBtn.getX(), playBtn.getY());
            Point2D position = GeometryUtils.translate(curPosition, control.getMarkerCenter().opposite());
            control.getRealm().executeTransaction(realm -> audio.setPosition(realm.copyToRealm(GeometryUtils.scale(position, control.getDepth() / audio.getArea().getDrawDepth()))));
            RestClient.updateAudio(audio, true);
        }

        @Override
        public void onDelete() {
            if (audio != null) {
                File file = new File(audio.getAudioFileLocal());
                RestClient.deleteAudio(audio);
                if (!file.delete()) {
                    Log.w(TAG, "Deleting audio file failed");
                }
                control.getRealm().executeTransaction(realm -> audio.deleteFromRealm());
                onAudioDeletion();
            }
        }
    }
}