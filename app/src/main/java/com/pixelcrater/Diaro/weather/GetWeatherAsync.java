package com.pixelcrater.Diaro.weather;

import android.os.AsyncTask;

import com.pixelcrater.Diaro.config.GlobalConstants;
import com.pixelcrater.Diaro.utils.Static;
import com.pixelcrater.Diaro.utils.AppLog;
import com.sandstorm.weather.WeatherInfo;

import org.json.JSONObject;

import java.io.IOException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class GetWeatherAsync extends AsyncTask<Object, String, Boolean> {

    private String responseText = null;
    private String mLat = null;
    private String mLng = null;

    private WeatherAsyncCallback listener;

    public GetWeatherAsync(WeatherAsyncCallback listner, String lat, String lng) {
        this.listener = listner;
        this.mLat = lat;
        this.mLng = lng;
    }

    @Override
    protected Boolean doInBackground(Object... params) {

        Request request = null;
        try {
            RequestBody formBody = null;
            boolean locationInfoEmpty = false;

            if (mLat == null) {
                locationInfoEmpty = true;
            }
            if (mLng == null) {
                locationInfoEmpty = true;

            }

            if (locationInfoEmpty) {
                formBody = new FormBody.Builder()
                        .build();

            } else {
                // Connect to API
                formBody = new FormBody.Builder()
                        .add("lat", mLat)
                        .add("lng", mLng)
                        .build();
            }

            request = new Request.Builder()
                    .url(Static.getApiUrl() + GlobalConstants.API_GET_WEATHER)
                    .post(formBody)
                    .build();

            OkHttpClient client = new OkHttpClient();
            Response response = client.newCall(request).execute();
            responseText = response.body().string();
        }catch (final SSLHandshakeException e) {
            try {
                Response response = Static.getUnsafeOkHttpClient().newCall(request).execute();
                responseText = response.body().string();
            } catch (IOException ex) {
                AppLog.e("Exception: " + e);
                return false;
            }

            return true;
        } catch (Exception e) {
            AppLog.e("Exception: " + e);

            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        AppLog.e("succeeded: " + succeeded + ", responseText: " + responseText);


      //  responseText: {"name":"Linkofpings Kommun","country":"SE","temperature":-1.7,"icon":"day-cloudy-gusts","description":"scattered clouds"}
        //{"name":"Patna","country":"IN","temperature":25,"icon":"day-haze","description":"haze"}

        if (succeeded && responseText != null) {
            // Static.showToast(responseText, Toast.LENGTH_LONG);

            try {
                JSONObject jsonObject = new JSONObject(responseText);

                String name = jsonObject.getString(WeatherInfo.FIELD_COUNTRY_NAME);
                String countryCode = jsonObject.getString(WeatherInfo.FIELD_COUNTRY_CODE);
                double temperature = jsonObject.getDouble(WeatherInfo.FIELD_TEMPRATURE);
                String icon = jsonObject.getString(WeatherInfo.FIELD_ICON);
                String description = jsonObject.getString(WeatherInfo.FIELD_DESCRIPTION);

                WeatherInfo weatherInfo = new WeatherInfo(name, countryCode, temperature, icon, description);
                listener.onWeatherInfoReceived(weatherInfo);

            } catch (Exception e) {

            }

        } else {

            // TODO : check and fix, add mapping of icons
          /**  OpenWeatherMapHelper helper =  new OpenWeatherMapHelper("5fb96d8085c605365decdcac71adcd76");
            helper.getCurrentWeatherByGeoCoordinates(
                    Double.valueOf(mLat),
                    Double.valueOf(mLng),
                    new CurrentWeatherCallback() {
                        @Override
                        public void onSuccess(CurrentWeather currentWeather) {
                            Log.v("Diaro", "Coordinates: " + currentWeather.getCoord().getLat() + ", "+currentWeather.getCoord().getLon() +"\n"
                                            +"Weather Description: " + currentWeather.getWeather().get(0).getDescription() + "\n"
                                            +"Temperature: " + currentWeather.getMain().getTempMax()+"\n"
                                            +"Wind Speed: " + currentWeather.getWind().getSpeed() + "\n"
                                            +"City, Country: " + currentWeather.getName() + ", " + currentWeather.getSys().getCountry());

                            String name = currentWeather.getSys().getCountry();
                            String countryCode = "";
                            double temperature = currentWeather.getMain().getTempMax();
                            String icon = jsonObject.getString(WeatherInfo.FIELD_ICON);
                            String description =  currentWeather.getWeather().get(0).getDescription();

                            WeatherInfo weatherInfo = new WeatherInfo(name, countryCode, temperature, icon, description);
                            listener.onWeatherInfoReceived(weatherInfo);
                        }

                        @Override
                        public void onFailure(Throwable throwable) {
                          //  Toast.makeText(Weather_Activity.this, throwable.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                        }
                    }); **/
        }
    }

    public interface WeatherAsyncCallback {
        void onWeatherInfoReceived(WeatherInfo weather);
    }

}
