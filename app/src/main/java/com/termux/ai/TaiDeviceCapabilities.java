package com.termux.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class TaiDeviceCapabilities {
    private static final long BYTES_PER_GIB = 1024L * 1024L * 1024L;

    public final String model;
    public final String manufacturer;
    public final String socModel;
    public final int sdkInt;
    public final List<String> supportedAbis;
    public final long memoryBytes;
    public final String memorySource;
    public final boolean pixel10;
    public final boolean liteRtLmAbiSupported;

    private TaiDeviceCapabilities(
        @NonNull String model,
        @NonNull String manufacturer,
        @NonNull String socModel,
        int sdkInt,
        @NonNull List<String> supportedAbis,
        long memoryBytes,
        @NonNull String memorySource,
        boolean pixel10
    ) {
        this.model = model;
        this.manufacturer = manufacturer;
        this.socModel = socModel;
        this.sdkInt = sdkInt;
        this.supportedAbis = Collections.unmodifiableList(new ArrayList<>(supportedAbis));
        this.memoryBytes = memoryBytes;
        this.memorySource = memorySource;
        this.pixel10 = pixel10;
        this.liteRtLmAbiSupported = containsSupportedAbi(supportedAbis);
    }

    @NonNull
    public static TaiDeviceCapabilities detect(@NonNull Context context) {
        long memoryBytes = 0L;
        String memorySource = "unavailable";
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            memoryBytes = memoryInfo.totalMem;
            memorySource = "totalMem";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && memoryInfo.advertisedMem > 0L) {
                memoryBytes = memoryInfo.advertisedMem;
                memorySource = "advertisedMem";
            }
        }
        String model = Build.MODEL == null ? "" : Build.MODEL;
        String soc = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && Build.SOC_MODEL != null ? Build.SOC_MODEL : "";
        return new TaiDeviceCapabilities(
            model,
            Build.MANUFACTURER == null ? "" : Build.MANUFACTURER,
            soc,
            Build.VERSION.SDK_INT,
            Arrays.asList(Build.SUPPORTED_ABIS),
            memoryBytes,
            memorySource,
            model.toLowerCase(Locale.ROOT).contains("pixel 10")
        );
    }

    public boolean supportsAccelerator(@NonNull String accelerator) {
        if (!liteRtLmAbiSupported) return false;
        if ("cpu".equalsIgnoreCase(accelerator)) return true;
        if ("gpu".equalsIgnoreCase(accelerator)) return !pixel10;
        return false;
    }

    @NonNull
    public List<String> compatibleAccelerators(@NonNull TaiModelProfile profile) {
        ArrayList<String> result = new ArrayList<>();
        for (String accelerator : profile.compatibleAccelerators) {
            if (supportsAccelerator(accelerator)) result.add(accelerator);
        }
        return result;
    }

    @Nullable
    public String memoryWarning(@NonNull TaiModelProfile profile) {
        if (profile.minDeviceMemoryInGb == null || memoryBytes <= 0L) return null;
        long requiredBytes = profile.minDeviceMemoryInGb * BYTES_PER_GIB;
        if (memoryBytes >= requiredBytes) return null;
        return "Device memory is below the model's Edge Gallery minimum of "
            + profile.minDeviceMemoryInGb + " GiB.";
    }

    @NonNull
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("model", model);
        json.put("manufacturer", manufacturer);
        json.put("socModel", socModel);
        json.put("sdkInt", sdkInt);
        JSONArray abis = new JSONArray();
        for (String abi : supportedAbis) abis.put(abi);
        json.put("supportedAbis", abis);
        json.put("memoryBytes", memoryBytes);
        json.put("memoryGiB", memoryBytes > 0L ? memoryBytes / (double) BYTES_PER_GIB : JSONObject.NULL);
        json.put("memorySource", memorySource);
        json.put("pixel10", pixel10);
        json.put("liteRtLmAbiSupported", liteRtLmAbiSupported);
        JSONArray accelerators = new JSONArray();
        if (supportsAccelerator("cpu")) accelerators.put("cpu");
        if (supportsAccelerator("gpu")) accelerators.put("gpu");
        json.put("phase1Accelerators", accelerators);
        json.put("gpuPolicy", pixel10
            ? "disabled to match Google AI Edge Gallery's Pixel 10 compatibility rule"
            : "validated by LiteRT-LM engine initialization");
        return json;
    }

    private static boolean containsSupportedAbi(@NonNull List<String> abis) {
        for (String abi : abis) {
            if ("arm64-v8a".equals(abi) || "x86_64".equals(abi)) return true;
        }
        return false;
    }
}
