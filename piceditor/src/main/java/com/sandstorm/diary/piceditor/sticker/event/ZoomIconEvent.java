package com.sandstorm.diary.piceditor.sticker.event;

import android.view.MotionEvent;

import com.sandstorm.diary.piceditor.sticker.StickerView;

public class ZoomIconEvent implements StickerIconEvent {
    public void onActionDown(StickerView paramStickerView, MotionEvent paramMotionEvent) {
    }

    public void onActionMove(StickerView paramStickerView, MotionEvent paramMotionEvent) {
        paramStickerView.zoomAndRotateCurrentSticker(paramMotionEvent);
    }

    public void onActionUp(StickerView paramStickerView, MotionEvent paramMotionEvent) {
        if (paramStickerView.getOnStickerOperationListener() != null)
            paramStickerView.getOnStickerOperationListener().onStickerZoomFinished(paramStickerView.getCurrentSticker());
    }
}
