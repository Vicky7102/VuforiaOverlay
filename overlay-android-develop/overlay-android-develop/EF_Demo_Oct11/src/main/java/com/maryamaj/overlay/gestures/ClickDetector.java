package com.maryamaj.overlay.gestures;

import android.view.GestureDetector;
import android.view.MotionEvent;


public class ClickDetector extends GestureDetector.SimpleOnGestureListener {
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}