package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.termux.R;

/**
 * 2x2 grid of TAI runtime action cards (load / keep warm / cancel / unload),
 * matching the TL handoff "Runtime control" design.
 */
@Keep
public class TaiRuntimeActionsPreference extends Preference {

    public interface OnActionClickListener {
        void onLoad();
        void onKeepWarm();
        void onCancel();
        void onUnload();
    }

    private OnActionClickListener mListener;
    private CharSequence mLoadSub = "";
    private boolean mLoadEnabled = true;
    private boolean mKeepWarmEnabled = true;
    private boolean mCancelEnabled = false;
    private boolean mUnloadEnabled = false;

    public TaiRuntimeActionsPreference(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TaiRuntimeActionsPreference(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.preference_tai_actions);
        setPersistent(false);
        setSelectable(false);
        setIconSpaceReserved(false);
    }

    public void setOnActionClickListener(OnActionClickListener listener) {
        mListener = listener;
    }

    public void setLoadSub(CharSequence loadSub) {
        mLoadSub = loadSub == null ? "" : loadSub;
        notifyChanged();
    }

    public void setActionStates(boolean loadEnabled, boolean keepWarmEnabled, boolean cancelEnabled, boolean unloadEnabled) {
        mLoadEnabled = loadEnabled;
        mKeepWarmEnabled = keepWarmEnabled;
        mCancelEnabled = cancelEnabled;
        mUnloadEnabled = unloadEnabled;
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        TextView loadLabel = (TextView) holder.findViewById(R.id.tai_action_load_label);
        if (loadLabel != null) {
            loadLabel.setText(R.string.termux_ai_runtime_load_title);
        }
        TextView loadSub = (TextView) holder.findViewById(R.id.tai_action_load_sub);
        if (loadSub != null) {
            loadSub.setText(mLoadSub);
        }

        bindCard(holder, R.id.tai_action_load, mLoadEnabled, () -> {
            if (mListener != null) mListener.onLoad();
        });
        bindCard(holder, R.id.tai_action_keep_warm, mKeepWarmEnabled, () -> {
            if (mListener != null) mListener.onKeepWarm();
        });
        bindCard(holder, R.id.tai_action_cancel, mCancelEnabled, () -> {
            if (mListener != null) mListener.onCancel();
        });
        bindCard(holder, R.id.tai_action_unload, mUnloadEnabled, () -> {
            if (mListener != null) mListener.onUnload();
        });
    }

    private void bindCard(@NonNull PreferenceViewHolder holder, int id, boolean enabled, @NonNull Runnable action) {
        View card = holder.findViewById(id);
        if (card == null) return;
        card.setAlpha(enabled ? 1f : 0.45f);
        card.setEnabled(enabled);
        card.setClickable(enabled);
        card.setOnClickListener(enabled ? v -> action.run() : null);
    }
}
