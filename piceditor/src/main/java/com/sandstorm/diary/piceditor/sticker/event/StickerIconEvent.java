package com.sandstorm.diary.piceditor.sticker.event;

import android.view.MotionEvent;

import com.sandstorm.diary.piceditor.sticker.StickerView;

public interface StickerIconEvent {
    void onActionDown(StickerView paramStickerView, MotionEvent paramMotionEvent);

    void onActionMove(StickerView paramStickerView, MotionEvent paramMotionEvent);

    void onActionUp(StickerView paramStickerView, MotionEvent paramMotionEvent);
}
