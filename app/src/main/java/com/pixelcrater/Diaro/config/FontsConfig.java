package com.pixelcrater.Diaro.config;

import com.pixelcrater.Diaro.MyApp;
import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.KeyValuePair;

import java.util.ArrayList;

public interface FontsConfig {

    // Do not add these fonts to font selection dialog
    static String FONT_PATH_MOODSFONT = "fonts/diaro_moods.ttf";
    static String FONT_PATH_FONTAWESOMEFONT = "fonts/fontawesome_webfont.ttf";
    static String FONT_PATH_WEATHERFONT = "fonts/weathericons_regular_webfont.ttf";

    // Note : if you add a font here, also add the @font-face in pdf.css
    // Font Helpers
    String[] FONT_Baskerville = {"Baskerville", "fonts/baskerville.ttf"};
    String[] FONT_DancingScript = {"Dancing Script", "fonts/dancing_script.ttf"};
    String[] FONT_IndieFlower = {"Indie Flower", "fonts/indieflower.ttf"};
    String[] FONT_Lato = {"Lato", "fonts/lato_regular.ttf"};
    String[] FONT_Lemonada = {"Lemonada", "fonts/lemonada_light.ttf"};
    String[] FONT_Lora = {"Lora", "fonts/lora_regular.ttf"};
    String[] FONT_Merriweather = {"Merriweather", "fonts/merriweather_regular.ttf"};
    String[] FONT_Montserrat = {"Montserrat", "fonts/montserrat_regular.ttf"};
    String[] FONT_Noto_Sans = {"Noto Sans", "fonts/notosans.ttf"};
    String[] FONT_Open_Sans = {"Open Sans", "fonts/opensans_light.ttf"};
    String[] FONT_Oswald = {"Oswald", "fonts/oswald_light.ttf"};
    String[] FONT_Pacifico = {"Pacifico", "fonts/pacifico.ttf"};
    String[] FONT_Raleway = {"Raleway", "fonts/raleway_light.ttf"};
    String[] FONT_Roboto = {"Roboto", "fonts/roboto_light.ttf"};
    String[] FONT_Slabo = {"Slabo", "fonts/slabo.ttf"};
    String[] FONT_Ubuntu = {"Ubuntu", "fonts/ubuntu_light.ttf"};

    static ArrayList<String[]> getAvailableFonts() {
        ArrayList<String[]> availableFonts = new ArrayList<>();
        // first add default system font
        String[] FONT_DEFAULT = {MyApp.getInstance().getString(R.string.settings_default), ""};
        availableFonts.add(FONT_DEFAULT);

        availableFonts.add(FONT_Baskerville);
        availableFonts.add(FONT_DancingScript);
        availableFonts.add(FONT_IndieFlower);
        availableFonts.add(FONT_Lato);
        availableFonts.add(FONT_Lemonada);
        availableFonts.add(FONT_Lora);
        availableFonts.add(FONT_Merriweather);
        availableFonts.add(FONT_Montserrat);
        availableFonts.add(FONT_Noto_Sans);
        availableFonts.add(FONT_Open_Sans);
        availableFonts.add(FONT_Oswald);
        availableFonts.add(FONT_Pacifico);
        availableFonts.add(FONT_Raleway);
        availableFonts.add(FONT_Roboto);
        availableFonts.add(FONT_Slabo);
        availableFonts.add(FONT_Ubuntu);
        return availableFonts;
    }

    // returns FONT NAME , PATH list
    static ArrayList<KeyValuePair> getFontOptions() {
        ArrayList<KeyValuePair> options = new ArrayList<>();
        for (String[] fonts : FontsConfig.getAvailableFonts()) {
            options.add(new KeyValuePair(fonts[0], fonts[1]));
        }
        return options;
    }

    static String getFontPathByName(String fontName) {
        ArrayList<KeyValuePair> options = getFontOptions();
        String fontPath = "";

        for (KeyValuePair keyValuePair : options) {
            if (keyValuePair.key.equalsIgnoreCase(fontName)) {
                fontPath = keyValuePair.value;
                break;
            }
        }

        return fontPath;
    }

    //options.add(new KeyValuePair("Arial", "arial"));
    //  options.add(new KeyValuePair("Cochin", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair("CourierNew", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair("Futura", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair("Georgia", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair("Gill Sans", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair( "Helvetica Neue", ctx.getString(R.string.satellite)));
    //    options.add(new KeyValuePair("Helvetica Neue Light", ctx.getString(R.string.satellite)));
    //    options.add(new KeyValuePair("Kohinoor Devanagari", ctx.getString(R.string.satellite)));
    //    options.add(new KeyValuePair( "Snell Roundhand", ctx.getString(R.string.satellite)));
    //  options.add(new KeyValuePair( "Times New Roman", ctx.getString(R.string.satellite)));
    //  options.add(new KeyValuePair( "Trebuchet MS", ctx.getString(R.string.satellite)));
    //   options.add(new KeyValuePair( "Verdana", ctx.getString(R.string.satellite)));


}
