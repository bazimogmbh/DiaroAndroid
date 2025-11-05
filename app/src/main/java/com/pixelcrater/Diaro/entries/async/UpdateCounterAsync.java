package com.pixelcrater.Diaro.entries.async;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import com.pixelcrater.Diaro.R;
import com.pixelcrater.Diaro.utils.Static;

public class UpdateCounterAsync extends AsyncTask<Object, String, Boolean> {

    private Context mContext;
    private String stateEntryTitle;
    private String stateEntryText;
    private TextView charsCounter;
    private TextView wordsCounter;
    private int charsCount;
    private int wordsCount;

    public UpdateCounterAsync(Context context, String stateEntryTitle, String stateEntryText, TextView charsCounter, TextView wordsCounter) {
        mContext = context;
        this.stateEntryTitle = stateEntryTitle;
        this.stateEntryText = stateEntryText;
        this.charsCounter = charsCounter;
        this.wordsCounter = wordsCounter;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        // Chars count
        charsCount = Static.countUnicodeChars(stateEntryTitle) + Static.countUnicodeChars(stateEntryText);

        // Words count
        int wordsInTitle = Static.countWords(stateEntryTitle);
        int wordsInText = Static.countWords(stateEntryText);
        wordsCount = wordsInTitle + wordsInText;

        return true;
    }

    @Override
    protected void onPostExecute(Boolean succeeded) {
        try {
            charsCounter.setText(mContext.getText(R.string.chars) + ": " + charsCount);
            wordsCounter.setText(mContext.getText(R.string.words) + ": " + wordsCount);
        } catch (Exception e) {
        }
    }
}
