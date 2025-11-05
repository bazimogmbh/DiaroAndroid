package com.sandstorm.weather;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.sanstorm.R;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherSelectDialog extends DialogFragment implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Spinner spn_weatherType;
    private EditText et_temperature;
    private TextView btn_tempSign;

    private TextView tv_weatherHeader, tv_tempratureHeader;

    private String mTitleBackgroundColor, unitSuffix, mClearTitle;
    boolean isDay;

    WeatherInfo weatherInfo;

    public interface OnWeatherSelectedListener {
        void onWeatherSelected(WeatherInfo weatherInfo);
    }

    // OK button click listener
    private OnWeatherSelectedListener onWeatherSelectedListener;

    public void setDialogWeatherSelectedListener(OnWeatherSelectedListener l) {
        onWeatherSelectedListener = l;
    }

    public WeatherSelectDialog() {
    }

    public WeatherSelectDialog(String titleBackgroundColor, String unitSuffix, boolean isDay, WeatherInfo weatherInfo, String clearTitle) {
        this.mTitleBackgroundColor = titleBackgroundColor;

        this.unitSuffix = unitSuffix;
        this.isDay = isDay;
        this.weatherInfo = weatherInfo;
        this.mClearTitle = clearTitle;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(Objects.requireNonNull(getActivity()));
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.weather_select_dialog, null);
        dialogBuilder.setView(dialogView);

        tv_weatherHeader = dialogView.findViewById(R.id.tv_weatherHeader);
        tv_weatherHeader.setText(getString(R.string.weather));

        tv_tempratureHeader = dialogView.findViewById(R.id.tv_tempratureHeader);
        tv_tempratureHeader.setText(String.format("%s %s", getString(R.string.temperature), unitSuffix));

        // Allow only -50 to 150
        btn_tempSign = dialogView.findViewById(R.id.btn_tempSign);
        btn_tempSign.setOnClickListener(this);

        et_temperature = dialogView.findViewById(R.id.et_temprature);
        et_temperature.setHint(getString(R.string.temperature) + " " + unitSuffix);
        et_temperature.setKeyListener(DigitsKeyListener.getInstance(true, true));
        et_temperature.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(5, 1)});

        spn_weatherType = dialogView.findViewById(R.id.spn_weatherType);

        String[] weatherValuesPrint = new String[WeatherHelper.weatherValues.length];
        for (int i = 0; i < WeatherHelper.weatherValues.length; i++) {
            weatherValuesPrint[i] = getActivity().getString(WeatherHelper.getLocalizedDescription(WeatherHelper.weatherValues[i]));
        }

        ArrayAdapter<String> weatherArray = new ArrayAdapter<>(Objects.requireNonNull(this.getContext()), android.R.layout.simple_spinner_dropdown_item, weatherValuesPrint);
        spn_weatherType.setAdapter(weatherArray);
        spn_weatherType.setOnItemSelectedListener(this);

        TextView titleTextView = dialogView.findViewById(R.id.alert_title);
        titleTextView.setText(getString(R.string.weather));

        LinearLayout titleBackground = dialogView.findViewById(R.id.titleBackground);

        int color = 0;

        try {
            color = Color.parseColor(mTitleBackgroundColor);
        } catch (Exception ignore) {
            color = Color.parseColor("#3192EA");
        }

        titleBackground.setBackgroundColor(color);

        if (this.weatherInfo != null) {
            String description = weatherInfo.getDescription();
            int index = 0;
            for (int i = 0; i < WeatherHelper.weatherValues.length; i++) {

                if (WeatherHelper.weatherValues[i].compareTo(description) == 0) {
                    index = i;
                    break;
                }

            }

            spn_weatherType.setSelection(index);

            double temperature = weatherInfo.getTemperature();
            if (unitSuffix.compareTo(WeatherHelper.STRING_FAHRENHEIT) == 0) {
                temperature = WeatherHelper.celsiusToFahrenheit(temperature);
            }
            double absoluteTemp = Math.abs(temperature);
            et_temperature.setText(String.format(Locale.US, "%.1f", absoluteTemp));

            if (temperature < 0) {
                btn_tempSign.setText("-");
            }

        }

        dialogBuilder.setPositiveButton(android.R.string.ok, null);
        dialogBuilder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();
            }
        });

        MaterialAlertDialogBuilder materialAlertDialogBuilder = dialogBuilder.setNeutralButton(mClearTitle, (dialog, which) -> {
            if (dialog != null) {
                dialog.dismiss();

                // Send the event back to the host activity
                if (onWeatherSelectedListener != null) {
                    onWeatherSelectedListener.onWeatherSelected(null);
                }
            }
        });

        final AlertDialog dialog = dialogBuilder.create();
        dialog.setOnShowListener(dialog1 -> {
            Button positiveButton = ((AlertDialog) dialog1).getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {

                String temperature = et_temperature.getText().toString();

                double temp = 0.00;

                int minTempLimit = -50;
                int maxTempLimit = 50;

                if(unitSuffix == null)
                    unitSuffix = WeatherHelper.STRING_CELSIUS;

                if (unitSuffix.compareTo(WeatherHelper.STRING_FAHRENHEIT) == 0) {
                    maxTempLimit = 122;
                }

                try {
                    temp = Double.parseDouble(temperature);

                    if (temp < minTempLimit || temp > maxTempLimit) {
                        Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                        et_temperature.startAnimation(shake);
                        return;
                    }

                } catch (NumberFormatException e) {
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    et_temperature.startAnimation(shake);
                    return;
                }

                dialog1.dismiss();

                String btnText = btn_tempSign.getText().toString().trim();
                if (btnText.equals("-"))
                    temp = -temp;

                if (unitSuffix.compareTo(WeatherHelper.STRING_FAHRENHEIT) == 0) {
                    // convert it to celsius, as we always save celsius in db
                    temp = WeatherHelper.fahrenheitToCelcius(temp);
                }


                int selection = spn_weatherType.getSelectedItemPosition();
                String icon = WeatherHelper.weatherValueDayIcons[selection];
                if (!isDay)
                    icon = WeatherHelper.weatherValueNightIcons[selection];

                String description = WeatherHelper.weatherValues[selection];
                Log.e("temperature", temp + " selection" + selection);

                // Send the event back to the host activity
                if (onWeatherSelectedListener != null) {
                    WeatherInfo weatherInfo = new WeatherInfo("", "", temp, icon, description);
                    onWeatherSelectedListener.onWeatherSelected(weatherInfo);
                }

            });

        });

        return dialog;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Prevent dismiss on touch outside
        getDialog().setCanceledOnTouchOutside(false);

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.btn_tempSign) {
            Log.e("onclick", btn_tempSign.getText() + "");
            String btnText = btn_tempSign.getText().toString().trim();

            /**  String temperature = et_temperature.getText().toString();
             Double temp = 0.0;
             try {
             temp = Double.parseDouble(temperature);
             } catch (NumberFormatException e){

             }**/
            if (btnText.equals("+")) {
                btn_tempSign.setText("-");
                //   et_temperature.setText("-" + Math.abs(temp));
            }

            if (btnText.equals("-")) {
                btn_tempSign.setText("+");
                //  et_temperature.setText(Math.abs(temp) +"");
            }

        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }

    static class DecimalDigitsInputFilter implements InputFilter {
        private final Pattern mPattern;

        DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero) {
            mPattern = Pattern.compile("[0-9]{0," + (digitsBeforeZero - 1) + "}+((\\.[0-9]{0," + (digitsAfterZero - 1) + "})?)||(\\.)?");
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            if (source.toString().compareTo("-") == 0) return "";

            Matcher matcher = mPattern.matcher(dest);
            if (!matcher.matches())
                return "";
            return null;
        }
    }
}
