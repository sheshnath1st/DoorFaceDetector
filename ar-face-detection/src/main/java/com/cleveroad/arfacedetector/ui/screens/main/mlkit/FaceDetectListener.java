package com.cleveroad.arfacedetector.ui.screens.main.mlkit;

import android.graphics.Bitmap;

import java.util.ArrayList;

public interface FaceDetectListener {
    void onSuccess(ArrayList<Bitmap> var1);

    void onFail(String var1);
}
