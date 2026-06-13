package com.termux.app.fragments.settings.termux;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.termux.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Two-column grid of runtime override key/value cells, matching the TL handoff
 * "Runtime overrides" design. Each cell is tappable; persistence is handled by
 * the hosting fragment.
 */
@Keep
public class TaiOverridesPreference extends Preference {

    public static final class Item {
        public final CharSequence label;
        public final CharSequence valueLabel;

        public Item(CharSequence label, CharSequence valueLabel) {
            this.label = label;
            this.valueLabel = valueLabel;
        }
    }

    public interface OnOverrideClickListener {
        void onOverrideClick(int index);
    }

    private final List<Item> mItems = new ArrayList<>();
    private OnOverrideClickListener mListener;

    public TaiOverridesPreference(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TaiOverridesPreference(@NonNull Context context) {
        super(context);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.preference_tai_overrides);
        setPersistent(false);
        setSelectable(false);
        setIconSpaceReserved(false);
    }

    public void setOnOverrideClickListener(OnOverrideClickListener listener) {
        mListener = listener;
    }

    public void setItems(@NonNull List<Item> items) {
        mItems.clear();
        mItems.addAll(items);
        notifyChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        GridLayout grid = (GridLayout) holder.findViewById(R.id.tai_overrides_grid);
        if (grid == null) return;
        grid.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < mItems.size(); i++) {
            Item item = mItems.get(i);
            View cell = inflater.inflate(R.layout.preference_tai_override_cell, grid, false);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams(
                GridLayout.spec(GridLayout.UNDEFINED),
                GridLayout.spec(GridLayout.UNDEFINED, 1f));
            params.width = 0;
            cell.setLayoutParams(params);
            TextView key = cell.findViewById(R.id.tai_override_key);
            if (key != null) key.setText(item.label);
            TextView value = cell.findViewById(R.id.tai_override_value);
            if (value != null) value.setText(item.valueLabel);
            final int index = i;
            cell.setOnClickListener(v -> {
                if (mListener != null) mListener.onOverrideClick(index);
            });
            grid.addView(cell);
        }
    }
}
