package com.sandstorm.diary.piceditor.features.picker.event;


import com.sandstorm.diary.piceditor.features.picker.entity.Photo;

public interface Selectable {

    int getSelectedItemCount();

    boolean isSelected(Photo photo);

}
