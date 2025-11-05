package com.sandstorm.weather;

import com.sanstorm.R;

public class WeatherHelper {

    public static String STRING_CELSIUS = "°C";
    public static String STRING_FAHRENHEIT = "°F";

    public static int CELSIUS = 0;
    public static int FAHRENHEIT = 1;

    public static String[] FAHRENHEIT_COUNTRIES = {"US", "BS", "BZ", "KY", "PW"};

    public static double celsiusToFahrenheit(double celsius) {
        double fahrenheit = (9.0 / 5.0) * celsius + 32;
        return round(fahrenheit, 1);
    }

    public static double fahrenheitToCelcius(double fahrenheit) {
        double celsius = (( 5 *(fahrenheit - 32.0)) / 9.0);
        return round(celsius, 2);
    }

    public static double round(double value, int precision) {
        int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }


    // this comes from server, and only in english
    public static final String light_rain = "light rain";
    public static final String moderate_rain = "moderate rain";
    public static final String heavy_intensity_rain = "heavy intensity rain";
    public static final String very_heavy_rain = "very heavy rain";
    public static final String extreme_rain = "extreme rain";
    public static final String light_intensity_drizzle = "light intensity drizzle";
    public static final String drizzle = "drizzle";
    public static final String heavy_intensity_drizzle = "heavy intensity drizzle";
    public static final String light_intensity_drizzle_rain = "light intensity drizzle rain";
    public static final String drizzle_rain = "drizzle rain";
    public static final String heavy_intensity_drizzle_rain = "heavy intensity drizzle rain";
    public static final String shower_rain_and_drizzle = "shower rain and drizzle";
    public static final String heavy_shower_rain_and_drizzle = "heavy shower rain and drizzle";
    public static final String shower_drizzle = "shower drizzle";
    public static final String light_intensity_shower_rain = "light intensity shower rain";
    public static final String shower_rain = "shower rain";
    public static final String heavy_intensity_shower_rain = "heavy intensity shower rain";
    public static final String ragged_shower_rain = "ragged shower rain";
    public static final String thunderstorm_with_light_rain = "thunderstorm with light rain";
    public static final String thunderstorm_with_rain = "thunderstorm with rain";
    public static final String thunderstorm_with_heavy_rain = "thunderstorm with heavy rain";
    public static final String light_thunderstorm = "light thunderstorm";
    public static final String thunderstorm = "thunderstorm";
    public static final String heavy_thunderstorm = "heavy thunderstorm";
    public static final String ragged_thunderstorm = "ragged thunderstorm";
    public static final String thunderstorm_with_light_drizzle = "thunderstorm with light drizzle";
    public static final String thunderstorm_with_drizzle = "thunderstorm with drizzle";
    public static final String thunderstorm_with_heavy_drizzle = "thunderstorm with heavy drizzle";
    public static final String light_snow = "light snow";
    public static final String freezing_rain = "freezing rain";
    public static final String snow = "snow";
    public static final String heavy_snow = "heavy snow";
    public static final String sleet = "sleet";
    public static final String shower_sleet = "shower sleet";
    public static final String light_rain_and_snow = "light rain and snow";
    public static final String rain_and_snow = "rain and snow";
    public static final String light_shower_snow = "light shower snow";
    public static final String shower_snow = "shower snow";
    public static final String heavy_shower_snow = "heavy shower snow";
    public static final String mist = "mist";
    public static final String smoke = "smoke";
    public static final String haze = "haze";
    public static final String sand_dust_whirls = "sand, dust whirls";
    public static final String fog = "fog";
    public static final String sand = "sand";
    public static final String dust = "dust";
    public static final String volcanic_ash = "volcanic ash";
    public static final String squalls = "squalls";
    public static final String tornado = "tornado";
    public static final String clear_sky = "clear sky";
    public static final String sky_is_clear = "sky is clear";
    public static final String few_clouds = "few clouds";
    public static final String scattered_clouds = "scattered clouds";
    public static final String broken_clouds = "broken clouds";
    public static final String overcast_clouds = "overcast clouds";

    /**   "01d" => "day-sunny" "01n" => "night-clear",
          "02d" => "day-cloudy-gusts", "02n" => "night-alt-cloudy-gusts",
          "03d" => "day-cloudy-gusts", "03n" => "night-alt-cloudy-gusts",
          "04d" => "day-sunny-overcast", "04n" => "night-alt-cloudy",
          "09d" => "day-showers", "09n" => "night-alt-showers",
          "10d" => "day-sprinkle",  "10n" => "night-alt-sprinkle",
          "11d" => "day-lightning", "11n" => "night-alt-lightning",
          "13d" => "day-snow",  "13n" => "night-alt-snow",
          "50d" => "day-fog",   "50n" => "night-fog" **/


                                                          //Clear 800 ,         Clouds 801 804,                                 Rain 500 502 ,                           Snow 600 602 611,                                  Thunderstorm 211       Atmosphere 701 711 721 741 761 771
    public static final String[] weatherValues          = {clear_sky,          few_clouds,            overcast_clouds,            light_rain,    heavy_intensity_rain,   light_snow, heavy_snow, sleet,                     thunderstorm ,          mist,         smoke,    haze,      fog,       dust,   tornado};
    public static final String[] weatherValueDayIcons   = {"day-sunny",        "day-cloudy",         "day-cloudy-high",          "day-sprinkle", "day-rain",            "day-snow", "day-snow", "day-rain-mix",              "day-thunderstorm",    "day-showers", "smoke", "day-haze", "day-fog", "dust", "tornado"};
    public static final String[] weatherValueNightIcons = {"night-clear",      "night-alt-cloudy",   "night-alt-cloudy-high",    "night-sprinkle", "night-rain",        "night-alt-snow", "night-alt-snow", "day-rain-mix",  "night-thunderstorm",   "day-showers", "smoke", "day-haze", "night-fog", "dust", "tornado"};
    public static int getLocalizedDescription(String description) {

        int retVal = 0;

        if (description == null)
            return 0;
        if (description.isEmpty())
            return 0;

        if (description.equals(light_rain)) {
            retVal = R.string.ow_light_rain;
        }
        if (description.equals(moderate_rain)) {
            retVal = R.string.ow_moderate_rain;
        }
        if (description.equals(heavy_intensity_rain)) {
            retVal = R.string.ow_heavy_intensity_rain;
        }
        if (description.equals(very_heavy_rain)) {
            retVal = R.string.ow_very_heavy_rain;
        }
        if (description.equals(extreme_rain)) {
            retVal = R.string.ow_extreme_rain;
        }
        if (description.equals(light_intensity_drizzle)) {
            retVal = R.string.ow_light_intensity_drizzle;
        }
        if (description.equals(drizzle)) {
            retVal = R.string.ow_drizzle;
        }
        if (description.equals(heavy_intensity_drizzle)) {
            retVal = R.string.ow_heavy_intensity_drizzle;
        }
        if (description.equals(light_intensity_drizzle_rain)) {
            retVal = R.string.ow_light_intensity_drizzle_rain;
        }
        if (description.equals(drizzle_rain)) {
            retVal = R.string.ow_drizzle_rain;
        }
        if (description.equals(heavy_intensity_drizzle_rain)) {
            retVal = R.string.ow_heavy_intensity_drizzle_rain;
        }
        if (description.equals(shower_rain_and_drizzle)) {
            retVal = R.string.ow_shower_rain_and_drizzle;
        }
        if (description.equals(heavy_shower_rain_and_drizzle)) {
            retVal = R.string.ow_heavy_shower_rain_and_drizzle;
        }
        if (description.equals(shower_drizzle)) {
            retVal = R.string.ow_shower_drizzle;
        }
        if (description.equals(light_intensity_shower_rain)) {
            retVal = R.string.ow_light_intensity_shower_rain;
        }
        if (description.equals(shower_rain)) {
            retVal = R.string.ow_shower_rain;
        }
        if (description.equals(heavy_intensity_shower_rain)) {
            retVal = R.string.ow_heavy_intensity_shower_rain;
        }
        if (description.equals(ragged_shower_rain)) {
            retVal = R.string.ow_ragged_shower_rain;
        }
        if (description.equals(thunderstorm_with_light_rain)) {
            retVal = R.string.ow_thunderstorm_with_light_rain;
        }
        if (description.equals(thunderstorm_with_rain)) {
            retVal = R.string.ow_thunderstorm_with_rain;
        }
        if (description.equals(thunderstorm_with_heavy_rain)) {
            retVal = R.string.ow_thunderstorm_with_heavy_rain;
        }
        if (description.equals(light_thunderstorm)) {
            retVal = R.string.ow_light_thunderstorm;
        }
        if (description.equals(thunderstorm)) {
            retVal = R.string.ow_thunderstorm;
        }
        if (description.equals(heavy_thunderstorm)) {
            retVal = R.string.ow_heavy_thunderstorm;
        }
        if (description.equals(ragged_thunderstorm)) {
            retVal = R.string.ow_ragged_thunderstorm;
        }
        if (description.equals(thunderstorm_with_light_drizzle)) {
            retVal = R.string.ow_thunderstorm_with_light_drizzle;
        }
        if (description.equals(thunderstorm_with_drizzle)) {
            retVal = R.string.ow_thunderstorm_with_drizzle;
        }
        if (description.equals(thunderstorm_with_heavy_drizzle)) {
            retVal = R.string.ow_thunderstorm_with_heavy_drizzle;
        }
        if (description.equals(light_snow)) {
            retVal = R.string.ow_light_snow;
        }
        if (description.equals(freezing_rain)) {
            retVal = R.string.ow_freezing_rain;
        }
        if (description.equals(snow)) {
            retVal = R.string.ow_snow;
        }
        if (description.equals(heavy_snow)) {
            retVal = R.string.ow_heavy_snow;
        }
        if (description.equals(sleet)) {
            retVal = R.string.ow_sleet;
        }
        if (description.equals(shower_sleet)) {
            retVal = R.string.ow_shower_sleet;
        }
        if (description.equals(light_rain_and_snow)) {
            retVal = R.string.ow_light_rain_and_snow;
        }
        if (description.equals(rain_and_snow)) {
            retVal = R.string.ow_rain_and_snow;
        }
        if (description.equals(light_shower_snow)) {
            retVal = R.string.ow_light_shower_snow;
        }
        if (description.equals(shower_snow)) {
            retVal = R.string.ow_shower_snow;
        }
        if (description.equals(heavy_shower_snow)) {
            retVal = R.string.ow_heavy_shower_snow;
        }
        if (description.equals(mist)) {
            retVal = R.string.ow_mist;
        }
        if (description.equals(smoke)) {
            retVal = R.string.ow_smoke;
        }
        if (description.equals(haze)) {
            retVal = R.string.ow_haze;
        }
        if (description.equals(sand_dust_whirls)) {
            retVal = R.string.ow_sand_dust_whirls;
        }
        if (description.equals(fog)) {
            retVal = R.string.ow_fog;
        }
        if (description.equals(sand)) {
            retVal = R.string.ow_sand;
        }
        if (description.equals(dust)) {
            retVal = R.string.ow_dust;
        }
        if (description.equals(volcanic_ash)) {
            retVal = R.string.ow_volcanic_ash;
        }
        if (description.equals(squalls)) {
            retVal = R.string.ow_squalls;
        }
        if (description.equals(tornado)) {
            retVal = R.string.ow_tornado;
        }
        if (description.equals(clear_sky)) {
            retVal = R.string.ow_clear_sky;
        }
        if (description.equals(sky_is_clear)) {
            retVal = R.string.ow_sky_is_clear;
        }
        if (description.equals(few_clouds)) {
            retVal = R.string.ow_few_clouds;
        }
        if (description.equals(scattered_clouds)) {
            retVal = R.string.ow_scattered_clouds;
        }
        if (description.equals(broken_clouds)) {
            retVal = R.string.ow_broken_clouds;
        }
        if (description.equals(overcast_clouds)) {
            retVal = R.string.ow_overcast_clouds;
        }

        return retVal;
    }

}
