package com.pixelcrater.Diaro.utils;

import okhttp3.OkHttpClient;

public class OkHttpHelper {

    static OkHttpHelper okHttpHelper;

    private OkHttpClient client;

     OkHttpHelper (){
        client = new OkHttpClient();
    }

    public static OkHttpHelper sharedInstance() {
        if (okHttpHelper == null) {
            okHttpHelper = new OkHttpHelper();
        }
        return okHttpHelper;
    }

    public OkHttpClient getClient () {
         return  client;
    }

}
