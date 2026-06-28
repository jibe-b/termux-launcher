package com.termux.ai;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TaiRuntimeServiceDispatchTest {

    @Test
    public void cancelAndUnload_useConcurrentControlLane() {
        assertTrue(TaiRuntimeService.isConcurrentControlOperation(TaiRuntimeIpc.OP_CANCEL));
        assertTrue(TaiRuntimeService.isConcurrentControlOperation(TaiRuntimeIpc.OP_UNLOAD_MODEL));
        assertFalse(TaiRuntimeService.isConcurrentControlOperation(TaiRuntimeIpc.OP_OPENAI_CHAT));
        assertFalse(TaiRuntimeService.isConcurrentControlOperation(TaiRuntimeIpc.OP_LOAD_MODEL));
    }
}
