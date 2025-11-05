package com.pixelcrater.Diaro.locations;

import android.location.Address;

import java.util.Locale;

public class LocationUtils {

    public static String getFormattedAddress(Address address) {
        String formattedAddress = "";

        if (address != null) {
            for (int i = 0; i < address.getMaxAddressLineIndex() + 1; i++) {
                String addressLine = address.getAddressLine(i);
                if (!addressLine.equals("")) {
                    if (!formattedAddress.equals("")) formattedAddress += ", ";
                    formattedAddress += addressLine;
                }
            }
        }

        return formattedAddress;
    }

    private static double[] getLatLng(String lat, String lng) {
        double latD = Double.parseDouble(lat);
        double longD = Double.parseDouble(lng);
        return  new double[]{latD, longD} ;
    }

    public static double[] normalizeLatLng(double[] latLng) {
        double lat = Double.parseDouble(getFormattedCoord(latLng[0]));
        double lng = Double.parseDouble(getFormattedCoord(latLng[1]));

        return new double[]{lat, lng};
    }

    public static String getFormattedCoord(double d) {
        return String.format(Locale.ENGLISH, "%.6f", d);
    }

}
