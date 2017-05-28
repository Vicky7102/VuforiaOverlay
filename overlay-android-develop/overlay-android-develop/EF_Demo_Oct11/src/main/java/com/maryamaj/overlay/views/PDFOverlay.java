package com.maryamaj.overlay.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.lukedeighton.wheelview.WheelView;
import com.lukedeighton.wheelview.adapter.WheelAdapter;
import com.maryamaj.overlay.adapter.DownloadManager;
import com.maryamaj.overlay.adapter.TextDrawable;
import com.maryamaj.overlay.api.ARListener;
import com.maryamaj.overlay.api.Control;
import com.maryamaj.overlay.api.TutorialManagerListener;
import com.maryamaj.overlay.applicationmain.TutorialManager;
import com.maryamaj.overlay.gestures.ClickDetector;
import com.maryamaj.overlay.gestures.DragDropListener;
import com.maryamaj.overlay.gestures.LongPressDragDrop;
import com.maryamaj.overlay.models.Area;
import com.maryamaj.overlay.models.Audio;
import com.maryamaj.overlay.models.PDFFile;
import com.maryamaj.overlay.models.Point2D;
import com.maryamaj.overlay.models.Text;
import com.maryamaj.overlay.rest.RestClient;
import com.maryamaj.overlay.rest.responses.PDFResponse;
import com.maryamaj.overlay.utils.GeometryUtils;
import com.maryamaj.ubitile_marker.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.RealmQuery;


public class PDFOverlay extends FrameLayout implements ARListener, TutorialManagerListener, DownloadManager.DownloadListener {

    private static final String TAG = "AudioOverlay";

    private Context context;
    private Control control;
    private GestureDetector clickDetector;
    private LongPressDragDrop longPressDragDrop;
    private TutorialManager tutorialManager;
    private FloatingActionButton toggleTextBtn;

    private WheelView wheelView;

    private int index = 0;

    private PDFFile mPdfFile;

    private List<PDFResponse> files;
    private List<String> fileNames;

    public PDFOverlay(Context c) {
        this(c, null);
    }

    public PDFOverlay(Context c, AttributeSet attrs) {
        super(c, attrs);
        context = c;
        control = (Control) context;
        init();
    }

    public void init() {
        tutorialManager = TutorialManager.getInstance();
        tutorialManager.register(this, R.id.pdf_overlay);

        clickDetector = new GestureDetector(context, new ClickDetector());
        longPressDragDrop = new LongPressDragDrop(this, new MyDrapDropListener());

        OnTouchListener buttonOnTouch = (v, event) -> {
            if (tutorialManager.isBuildMode()) {
                longPressDragDrop.onMotionEvent(event);
            }
            if (!longPressDragDrop.isDragging() && clickDetector.onTouchEvent(event)) {
                v.performClick();
            }
            return true;
        };

        control.getRealm().where(PDFFile.class).findAll().addChangeListener(element -> {
            if (mPdfFile != null && !mPdfFile.isValid()) {
                onPdfDeletion();
                Log.d(TAG, "PdfFile deletion called due to update");
            }
        });


        View view = LayoutInflater.from(context).inflate(R.layout.activity_main, null);

        addView(view);

        wheelView = (WheelView) view.findViewById(R.id.wheelview);

        wheelView.setVisibility(View.GONE);

        wheelView.setOnWheelItemClickListener((parent, position, isSelected) -> {

            control.getRealm().executeTransaction(realm -> {

                Area area = tutorialManager.getActiveArea();

                mPdfFile = realm.createObject(PDFFile.class, UUID.randomUUID().toString());
                mPdfFile.setArea(area);
                mPdfFile.setName(fileNames.get(position));

                Point2D point2D = GeometryUtils.translate(area.getCenter(), new Point2D(area.getRadius() + 10, -area.getRadius() - 10));

                mPdfFile.setPosition(realm.copyToRealm(point2D));
            });


        });


        toggleTextBtn = new FloatingActionButton(context);
        toggleTextBtn.setId(View.generateViewId());
        toggleTextBtn.setImageResource(R.drawable.ic_title_white_24dp);
        toggleTextBtn.setColorFilter(Color.BLACK);
        toggleTextBtn.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
        toggleTextBtn.setSize(FloatingActionButton.SIZE_MINI);
        toggleTextBtn.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        toggleTextBtn.setVisibility(View.GONE);
        addView(toggleTextBtn);

        toggleTextBtn.setOnTouchListener((v, event) -> {
            if (tutorialManager.isBuildMode()) {
                longPressDragDrop.onMotionEvent(event);
            }
            if (clickDetector.onTouchEvent(event)) {
                //showTextCallout = !showTextCallout;
                invalidate();
            }
            return true;
        });
    }

    private List getFiles() {

        List<String> files = new ArrayList<>();

        File dir = new File(context.getFilesDir().getAbsolutePath() + "/pdf");

        if (dir.exists()) {
            for (File f : dir.listFiles()) {

                if (f.isFile()) {
                    String name = f.getName();
                    files.add(name);
                }
            }
        }

        return files;
    }


    @Override
    public void onAreaChange(Area area) {
        System.out.println("PDFOverlay.onAreaChange");
        if (area == null) {
            setVisibility(View.GONE);
            return;
        }

        setVisibility(View.VISIBLE);
        RealmQuery<PDFFile> pdfRealmQuery = control.getRealm().where(PDFFile.class).equalTo("area.uuid", area.getUuid());
        if (pdfRealmQuery.count() == 0) {
            mPdfFile = null;
            toggleTextBtn.setVisibility(GONE);
        } else {
            mPdfFile = pdfRealmQuery.findFirst();

            System.out.println("PDFOverlay.onAreaChange :: " + mPdfFile.getFilePath());
            positionPDF();
            toggleTextBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onModeChange(boolean buildMode) {
        System.out.println("PDFOverlay.onModeChange");
    }

    @Override
    public void editMode() {

        control.pauseCamera();
        fileNames = getFiles();

        if (fileNames.size() > 0) {

            wheelView.setVisibility(VISIBLE);
            wheelView.setWheelItemCount(fileNames.size());

            wheelView.setAdapter(new WheelAdapter() {
                @Override
                public Drawable getDrawable(int position) {

                    return new TextDrawable(fileNames.get(position));
                }

                @Override
                public int getCount() {
                    return fileNames.size();
                }
            });

            control.resumeCamera();
        } else {

            files = RestClient.listPDF(new PDFFile());
            if (index > 0) {
                DownloadManager.init(context).download(files.get(index).pdf_file, this);
            } /*else {
                DownloadManager.init(context).download(files.get(0).pdf_file, this);
            }*/

           /* if (index > 0) {
                DownloadManager.init(context).download(files.get(index).pdf_file, this);
            } else {
               // index++;
                control.resumeCamera();

                fileNames = getFiles();

                if (fileNames.size() > 0) {

                    wheelView.setVisibility(VISIBLE);
                    wheelView.setWheelItemCount(fileNames.size());

                    wheelView.setAdapter(new WheelAdapter() {
                        @Override
                        public Drawable getDrawable(int position) {

                            return new TextDrawable(fileNames.get(position));
                        }

                        @Override
                        public int getCount() {
                            return fileNames.size();
                        }
                    });

                    control.resumeCamera();
                }
            }*/
        }
    }

    private void onPdfDeletion() {

        mPdfFile = null;
        toggleTextBtn.setVisibility(View.GONE);
        invalidate();
    }

    @Override
    public void onPauseCamera() {

        if (!longPressDragDrop.isDragging()) {
            toggleTextBtn.setVisibility(GONE);
        }
    }

    @Override
    public void onResumeCamera() {

        if (mPdfFile != null) {
            toggleTextBtn.setVisibility(VISIBLE);
        }
    }

    private void positionPDF() {
        if (mPdfFile == null || !mPdfFile.isValid())
            return;
        if (control.getMarkerCenter() != null) {
            Point2D position = GeometryUtils.translate(mPdfFile.getPositionAt(control.getDepth()), control.getMarkerCenter());
            toggleTextBtn.setX(position.x);
            toggleTextBtn.setY(position.y);
        }
    }

    @Override
    public void setMarkerCenter(Point2D markerCenter, float depth, int trackerId) {

        post(() -> {
            if (mPdfFile != null && mPdfFile.isValid()) {
                positionPDF();
            }
        });
    }

    @Override
    public void onDownloaded() {
        index++;

        if (files.size() > index) {
            DownloadManager.init(context).download(files.get(index).pdf_file, this);
        } else {

            control.resumeCamera();

            fileNames = getFiles();

            if (fileNames.size() > 0) {

                wheelView.setVisibility(VISIBLE);
                wheelView.setWheelItemCount(fileNames.size());

                wheelView.setAdapter(new WheelAdapter() {
                    @Override
                    public Drawable getDrawable(int position) {

                        return new TextDrawable(fileNames.get(position));
                    }

                    @Override
                    public int getCount() {
                        return fileNames.size();
                    }
                });

                control.resumeCamera();
            }
        }
    }


    private class MyDrapDropListener extends DragDropListener {

        @Override
        public void onStartDrag(Point2D start) {

        }

        @Override
        public void onMove(Point2D delta) {

            toggleTextBtn.setX(toggleTextBtn.getX() + delta.x);
            toggleTextBtn.setY(toggleTextBtn.getY() + delta.y);
        }

        @Override
        public void onDrop() {

            Point2D curPosition = new Point2D(toggleTextBtn.getX(), toggleTextBtn.getY());
            Point2D position = GeometryUtils.translate(curPosition, control.getMarkerCenter().opposite());
            control.getRealm().executeTransaction(realm -> mPdfFile.setPosition(realm.copyToRealm(GeometryUtils.scale(position, control.getDepth() / mPdfFile.getArea().getDrawDepth()))));
            //RestClient.updateText(mPdfFile);
        }

        @Override
        public void onDelete() {

            File file = new File(mPdfFile.getFilePath());
            // RestClient.deleteAudio(mPdfFile);
            if (!file.delete()) {
                Log.w(TAG, "Deleting audio file failed");
            }
            control.getRealm().executeTransaction(realm -> mPdfFile.deleteFromRealm());
            onPdfDeletion();
        }
    }

}