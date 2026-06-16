package com.termux.ai;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.termux.BuildConfig;

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
    public static final int MLC_SDK_MINIMUM = 24;
    public static final int MLC_MEMORY_ESTIMATE_MB = 2048;

    public final String model;
    public final String manufacturer;
    public final String socModel;
    public final int sdkInt;
    public final List<String> supportedAbis;
    public final long memoryBytes;
    public final String memorySource;
    public final boolean pixel10;
    public final boolean liteRtLmAbiSupported;

    public final boolean mlcSupported;
    @Nullable public final String mlcUnsupportedReason;
    public final int mlcSdkMinimum;
    public final int mlcMemoryEstimateMb;
    @Nullable public final String mlcAcceleratorInfo;
    public final boolean mlcBundledLibrariesAvailable;

    private static volatile String sDebugMlcUnsupportedReason = null;

    private TaiDeviceCapabilities(
        @NonNull String model,
        @NonNull String manufacturer,
        @NonNull String socModel,
        int sdkInt,
        @NonNull List<String> supportedAbis,
        long memoryBytes,
        @NonNull String memorySource,
        boolean pixel10,
        boolean liteRtLmAbiSupported,
        boolean mlcSupported,
        @Nullable String mlcUnsupportedReason,
        int mlcSdkMinimum,
        int mlcMemoryEstimateMb,
        @Nullable String mlcAcceleratorInfo,
        boolean mlcBundledLibrariesAvailable
    ) {
        this.model = model;
        this.manufacturer = manufacturer;
        this.socModel = socModel;
        this.sdkInt = sdkInt;
        this.supportedAbis = Collections.unmodifiableList(new ArrayList<>(supportedAbis));
        this.memoryBytes = memoryBytes;
        this.memorySource = memorySource;
        this.pixel10 = pixel10;
        this.liteRtLmAbiSupported = liteRtLmAbiSupported;
        this.mlcSupported = mlcSupported;
        this.mlcUnsupportedReason = mlcUnsupportedReason;
        this.mlcSdkMinimum = mlcSdkMinimum;
        this.mlcMemoryEstimateMb = mlcMemoryEstimateMb;
        this.mlcAcceleratorInfo = mlcAcceleratorInfo;
        this.mlcBundledLibrariesAvailable = mlcBundledLibrariesAvailable;
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
        List<String> abis = Arrays.asList(Build.SUPPORTED_ABIS);
        boolean liteRtSupported = containsSupportedAbi(abis);
        MlcCapabilityResult mlcResult = computeMlcCapabilities(abis, Build.VERSION.SDK_INT);
        String acceleratorInfo = Build.HARDWARE == null ? "" : Build.HARDWARE;
        return new TaiDeviceCapabilities(
            model,
            Build.MANUFACTURER == null ? "" : Build.MANUFACTURER,
            soc,
            Build.VERSION.SDK_INT,
            abis,
            memoryBytes,
            memorySource,
            model.toLowerCase(Locale.ROOT).contains("pixel 10"),
            liteRtSupported,
            mlcResult.mlcSupported,
            mlcResult.mlcUnsupportedReason,
            MLC_SDK_MINIMUM,
            MLC_MEMORY_ESTIMATE_MB,
            acceleratorInfo,
            mlcResult.mlcBundledLibrariesAvailable
        );
    }

    /**
     * Package-private factory for unit tests that need to inject fake ABIs
     * and control MLC capability computation without relying on {@link Build}.
     */
    static TaiDeviceCapabilities createForTest(
        @NonNull String model,
        @NonNull String manufacturer,
        @NonNull String socModel,
        int sdkInt,
        @NonNull List<String> supportedAbis,
        long memoryBytes,
        @NonNull String memorySource,
        boolean pixel10
    ) {
        boolean liteRtSupported = containsSupportedAbi(supportedAbis);
        MlcCapabilityResult mlcResult = computeMlcCapabilities(supportedAbis, sdkInt);
        return new TaiDeviceCapabilities(
            model,
            manufacturer,
            socModel,
            sdkInt,
            supportedAbis,
            memoryBytes,
            memorySource,
            pixel10,
            liteRtSupported,
            mlcResult.mlcSupported,
            mlcResult.mlcUnsupportedReason,
            MLC_SDK_MINIMUM,
            MLC_MEMORY_ESTIMATE_MB,
            "",
            mlcResult.mlcBundledLibrariesAvailable
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

    public static final class ModelCapabilityCheck {
        @Nullable public final String warning;
        @Nullable public final String blockingReason;

        public ModelCapabilityCheck(@Nullable String warning, @Nullable String blockingReason) {
            this.warning = warning;
            this.blockingReason = blockingReason;
        }
    }

    @NonNull
    public ModelCapabilityCheck checkModelCapability(@NonNull TaiModelSpec modelSpec) {
        if (TaiModelSpec.BACKEND_MLC_LLM.equals(modelSpec.backend)) {
            if (!mlcSupported) {
                return new ModelCapabilityCheck(null,
                    mlcUnsupportedReason != null ? mlcUnsupportedReason : "MLC backend is not supported on this device.");
            }
        }
        if (modelSpec.recommendedRamGb > 0 && memoryBytes > 0L) {
            long requiredBytes = modelSpec.recommendedRamGb * BYTES_PER_GIB;
            if (memoryBytes < requiredBytes) {
                return new ModelCapabilityCheck(
                    "Device memory is below the model's recommended " + modelSpec.recommendedRamGb + " GiB.",
                    null);
            }
        }
        return new ModelCapabilityCheck(null, null);
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
        json.put("mlcSupported", mlcSupported);
        json.put("mlcUnsupportedReason", mlcUnsupportedReason == null ? JSONObject.NULL : mlcUnsupportedReason);
        json.put("mlcSdkMinimum", mlcSdkMinimum);
        json.put("mlcMemoryEstimateMb", mlcMemoryEstimateMb);
        json.put("mlcAcceleratorInfo", mlcAcceleratorInfo == null ? JSONObject.NULL : mlcAcceleratorInfo);
        json.put("mlcBundledLibrariesAvailable", mlcBundledLibrariesAvailable);
        JSONObject backends = new JSONObject();
        backends.put(TaiModelSpec.BACKEND_LITERT_LM, liteRtLmAbiSupported);
        backends.put(TaiModelSpec.BACKEND_MLC_LLM, mlcSupported);
        json.put("backends", backends);
        JSONArray accelerators = new JSONArray();
        if (supportsAccelerator("cpu")) accelerators.put("cpu");
        if (supportsAccelerator("gpu")) accelerators.put("gpu");
        json.put("phase1Accelerators", accelerators);
        json.put("gpuPolicy", pixel10
            ? "disabled to match Google AI Edge Gallery's Pixel 10 compatibility rule"
            : "validated by LiteRT-LM engine initialization");
        return json;
    }

    /**
     * Debug-build-only override. When called with a non-null reason in a debug build,
     * subsequent {@link #detect} calls will force {@code mlcSupported=false} and surface
     * the provided reason. Completely ignored in release builds.
     *
     * <p>QA can enable this via ADB without code changes:
     * <pre>
     *   adb shell am broadcast -a com.termux.ai.FORCE_MLC_UNSUPPORTED \
     *       --es reason "Simulated unsupported ABI"
     * </pre>
     * Or by calling this method from a test harness.
     */
    public static void setDebugMlcUnsupportedReason(@Nullable String reason) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        sDebugMlcUnsupportedReason = reason;
    }

    /** Clears the debug override. Ignored in release builds. */
    public static void clearDebugMlcUnsupportedReason() {
        if (!BuildConfig.DEBUG) {
            return;
        }
        sDebugMlcUnsupportedReason = null;
    }

    /** Package-private for unit tests to verify release-build behavior. */
    static boolean shouldApplyDebugOverride(boolean isDebugBuild, @Nullable String overrideReason) {
        return isDebugBuild && overrideReason != null;
    }

    private static boolean containsSupportedAbi(@NonNull List<String> abis) {
        for (String abi : abis) {
            if ("arm64-v8a".equals(abi) || "x86_64".equals(abi)) return true;
        }
        return false;
    }

    @Nullable
    private static String findMlcSupportedAbi(@NonNull List<String> abis) {
        for (String abi : abis) {
            if (MlcBundledLibraryRegistry.isAbiSupported(abi)) {
                return abi;
            }
        }
        return null;
    }

    private static final class MlcCapabilityResult {
        final boolean mlcSupported;
        @Nullable final String mlcUnsupportedReason;
        final boolean mlcBundledLibrariesAvailable;

        MlcCapabilityResult(boolean mlcSupported, @Nullable String mlcUnsupportedReason, boolean mlcBundledLibrariesAvailable) {
            this.mlcSupported = mlcSupported;
            this.mlcUnsupportedReason = mlcUnsupportedReason;
            this.mlcBundledLibrariesAvailable = mlcBundledLibrariesAvailable;
        }
    }

    @NonNull
    private static MlcCapabilityResult computeMlcCapabilities(@NonNull List<String> abis, int sdkInt) {
        String deviceAbi = findMlcSupportedAbi(abis);
        boolean mlcBundledLibrariesAvailable = deviceAbi != null;
        boolean mlcSupported = mlcBundledLibrariesAvailable && sdkInt >= MLC_SDK_MINIMUM;
        String mlcUnsupportedReason = null;

        if (!mlcSupported) {
            List<String> reasons = new ArrayList<>();
            if (sdkInt < MLC_SDK_MINIMUM) {
                reasons.add("MLC requires Android 7.0 (API " + MLC_SDK_MINIMUM + ") or higher. Device is API " + sdkInt + ".");
            }
            if (!mlcBundledLibrariesAvailable) {
                StringBuilder supported = new StringBuilder();
                for (String abi : MlcBundledLibraryRegistry.supportedAbis()) {
                    if (supported.length() > 0) supported.append(", ");
                    supported.append(abi);
                }
                reasons.add("MLC runtime is not available for this device ABI. Supported: " + supported + ".");
            }
            if (!reasons.isEmpty()) {
                StringBuilder reasonBuilder = new StringBuilder();
                for (int i = 0; i < reasons.size(); i++) {
                    if (i > 0) reasonBuilder.append(" ");
                    reasonBuilder.append(reasons.get(i));
                }
                mlcUnsupportedReason = reasonBuilder.toString();
            }
        }

        if (shouldApplyDebugOverride(BuildConfig.DEBUG, sDebugMlcUnsupportedReason)) {
            mlcSupported = false;
            mlcUnsupportedReason = sDebugMlcUnsupportedReason;
        }

        return new MlcCapabilityResult(mlcSupported, mlcUnsupportedReason, mlcBundledLibrariesAvailable);
    }
}
