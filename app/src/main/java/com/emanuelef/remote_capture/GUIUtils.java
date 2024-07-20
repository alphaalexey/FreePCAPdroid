package com.emanuelef.remote_capture;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.emanuelef.remote_capture.model.Prefs;

public class GUIUtils {
    protected final SharedPreferences mPrefs;

    protected GUIUtils(Context ctx) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static GUIUtils newInstance(Context ctx) {
        return new GUIUtils(ctx);
    }

    public boolean isFirewallVisible() {
        if (CaptureService.isServiceActive())
            return !CaptureService.isCapturingAsRoot() && !CaptureService.isReadingFromPcapFile();
        else
            return !Prefs.isRootCaptureEnabled(mPrefs);
    }
}
