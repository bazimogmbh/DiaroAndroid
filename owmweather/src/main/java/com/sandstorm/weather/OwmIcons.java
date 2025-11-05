package com.sandstorm.weather;

import java.util.HashMap;
import java.util.Map;

public enum OwmIcons {

    day_thunderstorm("day-thunderstorm", "\uf010"),
    day_lightning_("day-lightning", "\uf005"),
    day_sprinkle("day-sprinkle", "\uf00b"),
    day_rain("day-rain", "\uf008"),
    day_rain_mix("day-rain-mix", "\uf006"),
    day_showers("day-showers", "\uf009"),
    day_storm_showers("day-storm-showers", "\uf00e"),
    day_snow("day-snow", "\uf00a"),
    day_sleet("day-sleet", "\uf0b2"),
    smoke("smoke", "\uf062"),
    day_haze("day-haze", "\uf0b6"),
    dust("dust", "\uf063"),
    day_fog("day-fog", "\uf003"),
    tornado("tornado", "\uf056"),
    day_sunny("day-sunny", "\uf00d"),
    day_cloudy_gusts("day-cloudy-gusts", "\uf000"),
    day_sunny_overcast("day-sunny-overcast", "\uf00c"),
    hurricane("hurricane", "\uf073"),
    snowflake_cold("snowflake-cold", "\uf076"),
    hot("hot", "\uf072"),
    day_hail("day-hail", "\uf004"),
    strong_wind("strong-wind", "\uf050"),
    night_alt_thunderstorm("night-alt-thunderstorm", "\uf02d"),
    night_alt_lightning("night-alt-lightning", "\uf025"),
    night_alt_sprinkle("night-alt-sprinkle", "\uf02b"),
    night_alt_rain("night-alt-rain", "\uf028"),
    night_alt_rain_mix("night-alt-rain-mix", "\uf026"),
    night_alt_showers("night-alt-showers", "\uf029"),
    night_alt_storm_showers("night-alt-storm-showers", "\uf02c"),
    night_alt_snow("night-alt-snow", "\uf02a"),
    night_alt_sleet("night-alt-sleet", "\uf0b4"),
    night_fog("night-fog", "\uf04a"),
    night_clear("night-clear", "\uf02e"),
    night_alt_cloudy_gusts("night-alt-cloudy-gusts", "\uf022"),
    night_alt_cloudy("night-alt-cloudy", "\uf086"),
    night_alt_hail("night-alt-hail", "\uf024"),


    day_cloudy("day-cloudy", "\uf002"),
    day_cloudy_high("day-cloudy-high", "\uf07d"),
    night_alt_cloudy_high("night-alt-cloudy-high", "\uf07e"),
    night_sprinkle("night-sprinkle", "\uf039"),
    night_rain("night-rain", "\uf036"),
    night_thunderstorm("night-thunderstorm", "\uf03b"),
    ;

    // Reverse-lookup map for getting a day from an abbreviation
    private static final Map<String, String> lookup = new HashMap<String, String>();

    static {
        for (OwmIcons owmIcon : OwmIcons.values()) {
            lookup.put(owmIcon.iconDescription, owmIcon.fontCode);
        }
    }

    public static String getFontCode(String iconDescription) {
        return lookup.get(iconDescription);
    }

    private String iconDescription;
    private String fontCode;


    OwmIcons(String iconDescription, String fontCode) {
        this.iconDescription = iconDescription;
        this.fontCode = fontCode;

    }
}
