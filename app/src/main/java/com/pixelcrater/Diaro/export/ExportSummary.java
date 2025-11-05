package com.pixelcrater.Diaro.export;

public class ExportSummary {

    String date;
    String folder;
    String location;
    String search;
    String tag;

    public ExportSummary(String date, String folder, String location, String search, String tag) {
        this.date = date;
        this.folder = folder;
        this.location = location;
        this.search = search;
        this.tag = tag;
    }
}
