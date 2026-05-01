package com.termux.launcherctl;

import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LauncherCtlNotificationListenerTest {

    @Test
    public void notificationSnapshots_includeDisconnectedHintsAndSettingsAction() throws Exception {
        JSONObject notifications = LauncherCtlNotificationListener.getNotificationsSnapshot();
        JSONObject media = LauncherCtlNotificationListener.getNowPlayingSnapshot();
        JSONObject art = LauncherCtlNotificationListener.getNowPlayingArtSnapshot();

        assertEquals(false, notifications.getBoolean("listenerConnected"));
        assertEquals(false, media.getBoolean("listenerConnected"));
        assertEquals(false, art.getBoolean("listenerConnected"));

        assertEquals("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS",
            notifications.getString("settingsAction"));
        assertEquals(LauncherCtlNotificationListener.getListenerSettingsAction(),
            media.getString("settingsAction"));
        assertEquals(LauncherCtlNotificationListener.getListenerSettingsAction(),
            art.getString("settingsAction"));

        assertEquals(LauncherCtlNotificationListener.getListenerHint(), notifications.getString("hint"));
        assertEquals(LauncherCtlNotificationListener.getListenerHint(), media.getString("hint"));
        assertEquals(LauncherCtlNotificationListener.getListenerHint(), art.getString("hint"));
        assertTrue(media.isNull("nowPlaying"));
        assertTrue(art.isNull("art"));
    }
}
