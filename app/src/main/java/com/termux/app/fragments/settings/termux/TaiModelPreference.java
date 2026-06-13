package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.TypedValue;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.termux.R;

/**
 * Card-style row for a TAI model: name + status pill, role tag (summary),
 * monospace size/accelerator/memory meta line, and a download progress bar.
 */
public final class TaiModelPreference extends Preference {
    private boolean showProgress;
    private boolean indeterminate;
    private int progress;
    private CharSequence pillText = "";
    private boolean pillAccent;
    private CharSequence metaLine = "";

    public TaiModelPreference(@NonNull Context context) {
        super(context);
        setLayoutResource(R.layout.preference_tai_model);
        setIconSpaceReserved(false);
    }

    public void setDownloadProgress(boolean showProgress, boolean indeterminate, int progress) {
        this.showProgress = showProgress;
        this.indeterminate = indeterminate;
        this.progress = Math.max(0, Math.min(10000, progress));
        notifyChanged();
    }

    public void setPill(@Nullable CharSequence text, boolean accent) {
        this.pillText = text == null ? "" : text;
        this.pillAccent = accent;
        notifyChanged();
    }

    public void setMetaLine(@Nullable CharSequence metaLine) {
        this.metaLine = metaLine == null ? "" : metaLine;
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

        TextView pill = (TextView) holder.findViewById(R.id.tai_model_pill);
        if (pill != null) {
            if (pillText.length() == 0) {
                pill.setVisibility(View.GONE);
            } else {
                pill.setVisibility(View.VISIBLE);
                pill.setText(pillText);
                int color = resolveAttrColor(pillAccent
                    ? com.termux.shared.R.attr.termuxColorPrimary
                    : com.termux.shared.R.attr.termuxColorOnSurfaceVariant);
                pill.setTextColor(color);
                pill.setBackgroundTintList(ColorStateList.valueOf(ColorUtils.setAlphaComponent(color, 46)));
            }
        }

        TextView meta = (TextView) holder.findViewById(R.id.tai_model_meta);
        if (meta != null) {
            if (metaLine.length() == 0) {
                meta.setVisibility(View.GONE);
            } else {
                meta.setVisibility(View.VISIBLE);
                meta.setText(metaLine);
            }
        }
    }

    private int resolveAttrColor(int attr) {
        TypedValue value = new TypedValue();
        if (getContext().getTheme().resolveAttribute(attr, value, true)) {
            return value.data;
        }
        return 0xFF888888;
    }
}
