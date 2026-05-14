package com.termux.app.terminal;

import android.content.Context;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.google.android.material.color.MaterialColors;
import com.termux.R;

import java.util.Properties;

public final class MaterialTerminalColorScheme {

    private MaterialTerminalColorScheme() {}

    @NonNull
    public static Properties create(@NonNull Context context) {
        Properties props = new Properties();

        int background = materialColor(context, com.google.android.material.R.attr.colorSurface,
            R.color.termux_surface_base);
        int foreground = materialColor(context, com.google.android.material.R.attr.colorOnSurface,
            R.color.termux_on_surface);
        int primary = materialColor(context, com.google.android.material.R.attr.colorPrimary,
            R.color.termux_primary);
        int secondary = materialColor(context, com.google.android.material.R.attr.colorSecondary,
            R.color.termux_secondary);
        int tertiary = materialColor(context, com.google.android.material.R.attr.colorTertiary,
            R.color.termux_primary);
        int error = materialColor(context, com.google.android.material.R.attr.colorError,
            R.color.termux_error);
        int errorContainer = materialColor(context, com.google.android.material.R.attr.colorErrorContainer,
            R.color.termux_error_container);
        int neutral = materialColor(context, com.google.android.material.R.attr.colorSurfaceVariant,
            R.color.termux_surface_panel);
        int subtleText = materialColor(context, com.google.android.material.R.attr.colorOnSurfaceVariant,
            R.color.termux_on_surface_variant);

        boolean dark = perceivedBrightness(background) < 128;

        props.setProperty("background", hex(background));
        props.setProperty("foreground", hex(foreground));
        props.setProperty("cursor", hex(primary));

        props.setProperty("color0", hex(dark ? darken(neutral, 0.72f) : darken(subtleText, 0.38f)));
        props.setProperty("color1", hex(tintToward(error, errorContainer, dark ? 0.12f : 0.18f)));
        props.setProperty("color2", hex(materialAnsi("#5CF19E", "#0B6B3A", secondary, dark)));
        props.setProperty("color3", hex(materialAnsi("#FFD740", "#8A5A00", tertiary, dark)));
        props.setProperty("color4", hex(materialAnsi("#40C4FF", "#00639B", primary, dark)));
        props.setProperty("color5", hex(materialAnsi("#FF4081", "#A23A6F", primary, dark)));
        props.setProperty("color6", hex(materialAnsi("#64FCDA", "#006D63", secondary, dark)));
        props.setProperty("color7", hex(dark ? lighten(neutral, 0.72f) : darken(neutral, 0.54f)));

        props.setProperty("color8", hex(dark ? lighten(neutral, 0.34f) : darken(subtleText, 0.18f)));
        props.setProperty("color9", hex(dark ? lighten(error, 0.22f) : lighten(error, 0.16f)));
        props.setProperty("color10", hex(materialAnsi("#B9F6CA", "#228C55", secondary, dark)));
        props.setProperty("color11", hex(materialAnsi("#FFE57F", "#A76F00", tertiary, dark)));
        props.setProperty("color12", hex(materialAnsi("#80D8FF", "#1976A8", primary, dark)));
        props.setProperty("color13", hex(materialAnsi("#FF80AB", "#B84F83", primary, dark)));
        props.setProperty("color14", hex(materialAnsi("#A7FDEB", "#008577", secondary, dark)));
        props.setProperty("color15", hex(foreground));

        return props;
    }

    public static int signature(@NonNull Context context) {
        int result = 17;
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorSurface,
            R.color.termux_surface_base);
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorOnSurface,
            R.color.termux_on_surface);
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorPrimary,
            R.color.termux_primary);
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorSecondary,
            R.color.termux_secondary);
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorTertiary,
            R.color.termux_primary);
        result = 31 * result + materialColor(context, com.google.android.material.R.attr.colorError,
            R.color.termux_error);
        return result;
    }

    @ColorInt
    private static int materialColor(@NonNull Context context, int attr, int fallbackRes) {
        return MaterialColors.getColor(context, attr, ContextCompat.getColor(context, fallbackRes));
    }

    private static String hex(@ColorInt int color) {
        return String.format("#%06X", color & 0x00FFFFFF);
    }

    @ColorInt
    private static int materialAnsi(String darkBaseHex, String lightBaseHex, @ColorInt int materialColor, boolean dark) {
        int semanticBase = Color.parseColor(dark ? darkBaseHex : lightBaseHex);
        return tintToward(semanticBase, materialColor, dark ? 0.42f : 0.36f);
    }

    @ColorInt
    private static int tintToward(@ColorInt int base, @ColorInt int target, float amount) {
        float[] baseHsv = new float[3];
        float[] targetHsv = new float[3];
        Color.colorToHSV(base, baseHsv);
        Color.colorToHSV(target, targetHsv);
        baseHsv[1] = Math.max(0f, Math.min(1f, baseHsv[1] * (1f - amount) + targetHsv[1] * amount));
        baseHsv[2] = Math.max(0f, Math.min(1f, baseHsv[2] * (1f - amount) + targetHsv[2] * amount));
        return blend(Color.HSVToColor(baseHsv), target, amount * 0.45f);
    }

    @ColorInt
    private static int lighten(@ColorInt int color, float amount) {
        return blend(color, Color.WHITE, amount);
    }

    @ColorInt
    private static int darken(@ColorInt int color, float amount) {
        return blend(color, Color.BLACK, amount);
    }

    @ColorInt
    private static int blend(@ColorInt int from, @ColorInt int to, float amount) {
        float clamped = Math.max(0f, Math.min(1f, amount));
        int red = Math.round(Color.red(from) + (Color.red(to) - Color.red(from)) * clamped);
        int green = Math.round(Color.green(from) + (Color.green(to) - Color.green(from)) * clamped);
        int blue = Math.round(Color.blue(from) + (Color.blue(to) - Color.blue(from)) * clamped);
        return Color.rgb(red, green, blue);
    }

    private static int perceivedBrightness(@ColorInt int color) {
        return (int) Math.floor(Math.sqrt(
            Math.pow(Color.red(color), 2) * 0.241
                + Math.pow(Color.green(color), 2) * 0.691
                + Math.pow(Color.blue(color), 2) * 0.068
        ));
    }
}
