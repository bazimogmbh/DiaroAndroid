package com.sandstorm.diary.piceditor.features.picker.event;


import com.sandstorm.diary.piceditor.features.picker.entity.Photo;

public interface OnItemCheckListener {
    boolean onItemCheck(int i, Photo photo, int i2);
}
