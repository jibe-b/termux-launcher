package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.termux.R;

public final class TaiModelPreference extends Preference {
    private boolean showProgress;
    private boolean indeterminate;
    private int progress;

    public TaiModelPreference(@NonNull Context context) {
        super(context);
        setLayoutResource(R.layout.preference_tai_model);
    }

    public void setDownloadProgress(boolean showProgress, boolean indeterminate, int progress) {
        this.showProgress = showProgress;
        this.indeterminate = indeterminate;
        this.progress = Math.max(0, Math.min(10000, progress));
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View view = holder.findViewById(R.id.tai_download_progress);
        if (view instanceof ProgressBar) {
            ProgressBar progressBar = (ProgressBar) view;
            progressBar.setVisibility(showProgress ? View.VISIBLE : View.GONE);
            progressBar.setIndeterminate(indeterminate);
            progressBar.setProgress(progress);
        }
    }
}
