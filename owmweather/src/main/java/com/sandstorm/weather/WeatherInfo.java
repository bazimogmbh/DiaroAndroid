package com.sandstorm.weather;

public class WeatherInfo {

    private String name = "";
    private String countryCode = "" ;
    private double temperature = 0.0;
    private String icon = "";
    private String description = "";

    public static final String FIELD_COUNTRY_NAME = "name";
    public static final String FIELD_COUNTRY_CODE = "country";
    public static final String FIELD_TEMPRATURE = "temperature";
    public static final String FIELD_ICON = "icon";
    public static final String FIELD_DESCRIPTION = "description";

    public WeatherInfo () {
    }

    public WeatherInfo (String countryName, String countryCode, double temperature, String icon, String description ){
        this.name = countryName;
        this.countryCode = countryCode;
        this.temperature = temperature * 10 /10; // we just want 1 decimal
        this.icon = icon;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String countryName) {
        this.name = countryName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public int getLocalizedDescription() {
        return WeatherHelper.getLocalizedDescription(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
