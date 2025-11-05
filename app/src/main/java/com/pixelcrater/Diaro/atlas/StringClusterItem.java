package com.pixelcrater.Diaro.atlas;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

public class StringClusterItem implements ClusterItem {

    final String title;
    final LatLng latLng;

    public StringClusterItem(String title, LatLng latLng) {
        this.title = title;
        this.latLng = latLng;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    @Nullable

    public Float getZIndex() {
        return null;
    }

}