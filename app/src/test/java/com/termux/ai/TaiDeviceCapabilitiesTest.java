package com.termux.ai;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
public class TaiDeviceCapabilitiesTest {

    @After
    public void tearDown() {
        TaiDeviceCapabilities.clearDebugMlcUnsupportedReason();
    }

    @Test
    public void supportedArm64Device_mlcSupportedIsTrue() {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Pixel 9", "Google", "tensor", 34,
            Arrays.asList("arm64-v8a", "armeabi-v7a", "armeabi"),
            8L * 1024L * 1024L * 1024L, "totalMem", false);

        assertTrue(device.liteRtLmAbiSupported);
        assertTrue(device.mlcSupported);
        assertTrue(device.mlcBundledLibrariesAvailable);
        assertNull(device.mlcUnsupportedReason);
        assertEquals(TaiDeviceCapabilities.MLC_SDK_MINIMUM, device.mlcSdkMinimum);
        assertEquals(TaiDeviceCapabilities.MLC_MEMORY_ESTIMATE_MB, device.mlcMemoryEstimateMb);
    }

    @Test
    public void unsupportedAbi_mlcSupportedIsFalseWithReason() {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Emulator", "Google", "", 34,
            Arrays.asList("x86"),
            4L * 1024L * 1024L * 1024L, "totalMem", false);

        assertFalse(device.liteRtLmAbiSupported);
        assertFalse(device.mlcSupported);
        assertFalse(device.mlcBundledLibrariesAvailable);
        assertNotNull(device.mlcUnsupportedReason);
        assertTrue("Expected ABI unsupported reason, got: " + device.mlcUnsupportedReason,
            device.mlcUnsupportedReason.contains("not available for this device ABI"));
        assertTrue("Expected supported ABI list, got: " + device.mlcUnsupportedReason,
            device.mlcUnsupportedReason.contains("arm64-v8a"));
    }

    @Test
    public void lowSdk_mlcSupportedIsFalseWithReason() {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Old Phone", "Generic", "", 21,
            Arrays.asList("arm64-v8a"),
            4L * 1024L * 1024L * 1024L, "totalMem", false);

        assertTrue(device.liteRtLmAbiSupported);
        assertFalse(device.mlcSupported);
        assertTrue(device.mlcBundledLibrariesAvailable);
        assertNotNull(device.mlcUnsupportedReason);
        assertTrue("Expected SDK reason, got: " + device.mlcUnsupportedReason,
            device.mlcUnsupportedReason.contains("requires Android 7.0"));
    }

    @Test
    public void debugOverrideForcesUnsupportedState() {
        TaiDeviceCapabilities.setDebugMlcUnsupportedReason("Debug override: forced unsupported.");

        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Pixel 9", "Google", "tensor", 34,
            Arrays.asList("arm64-v8a"),
            8L * 1024L * 1024L * 1024L, "totalMem", false);

        assertFalse(device.mlcSupported);
        assertEquals("Debug override: forced unsupported.", device.mlcUnsupportedReason);
    }

    @Test
    public void releaseBuildIgnoresDebugOverride() {
        assertFalse(TaiDeviceCapabilities.shouldApplyDebugOverride(false, "reason"));
        assertTrue(TaiDeviceCapabilities.shouldApplyDebugOverride(true, "reason"));
    }

    @Test
    public void mlcModelOnUnsupportedDevice_blocksWithReason() {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Emulator", "Google", "", 34,
            Arrays.asList("x86"),
            4L * 1024L * 1024L * 1024L, "totalMem", false);

        TaiModelSpec model = new TaiModelSpec(
            "phi-3-mini-mlc",
            "Phi-3 Mini MLC",
            "chat",
            "test",
            "/models/phi-3-mini-mlc/model.mlc",
            "test",
            0L,
            new LinkedHashSet<>(Collections.singletonList(TaiModelSpec.CAPABILITY_TEXT_CHAT)),
            false,
            null,
            TaiModelSpec.BACKEND_MLC_LLM,
            TaiModelSpec.FORMAT_MLC,
            null,
            null,
            4096,
            4,
            null
        );

        TaiDeviceCapabilities.ModelCapabilityCheck check = device.checkModelCapability(model);
        assertNotNull(check.blockingReason);
        assertNull(check.warning);
        assertTrue(check.blockingReason.contains("not available for this device ABI"));
    }

    @Test
    public void modelMemoryRequirement_producesWarning() {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Low-RAM Phone", "Generic", "", 34,
            Arrays.asList("arm64-v8a"),
            2L * 1024L * 1024L * 1024L, "totalMem", false);

        TaiModelSpec model = new TaiModelSpec(
            "large-model",
            "Large Model",
            "chat",
            "test",
            "/models/large/model.bin",
            "test",
            0L,
            new LinkedHashSet<>(Collections.singletonList(TaiModelSpec.CAPABILITY_TEXT_CHAT)),
            false,
            null,
            TaiModelSpec.BACKEND_LITERT_LM,
            TaiModelSpec.FORMAT_LITERTLM,
            null,
            null,
            4096,
            8,
            null
        );

        TaiDeviceCapabilities.ModelCapabilityCheck check = device.checkModelCapability(model);
        assertNull(check.blockingReason);
        assertNotNull(check.warning);
        assertTrue(check.warning.contains("below the model's recommended"));
    }

    @Test
    public void jsonOutput_containsCorrectBackendsStructure() throws Exception {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Pixel 9", "Google", "tensor", 34,
            Arrays.asList("arm64-v8a"),
            8L * 1024L * 1024L * 1024L, "totalMem", false);

        JSONObject json = device.toJson();
        assertTrue(json.getBoolean("liteRtLmAbiSupported"));
        assertTrue(json.getBoolean("mlcSupported"));
        assertEquals(JSONObject.NULL, json.get("mlcUnsupportedReason"));

        JSONObject backends = json.getJSONObject("backends");
        assertTrue(backends.getBoolean(TaiModelSpec.BACKEND_LITERT_LM));
        assertTrue(backends.getBoolean(TaiModelSpec.BACKEND_MLC_LLM));
    }

    @Test
    public void jsonOutput_containsMlcUnsupportedReasonWhenFalse() throws Exception {
        TaiDeviceCapabilities device = TaiDeviceCapabilities.createForTest(
            "Emulator", "Google", "", 34,
            Arrays.asList("x86"),
            4L * 1024L * 1024L * 1024L, "totalMem", false);

        JSONObject json = device.toJson();
        assertFalse(json.getBoolean("mlcSupported"));
        assertFalse(json.getJSONObject("backends").getBoolean(TaiModelSpec.BACKEND_MLC_LLM));
        assertEquals(device.mlcUnsupportedReason, json.getString("mlcUnsupportedReason"));
    }
}
