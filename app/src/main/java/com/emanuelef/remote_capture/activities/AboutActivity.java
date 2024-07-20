/*
 * This file is part of PCAPdroid.
 *
 * PCAPdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCAPdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PCAPdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2020-24 - Emanuele Faranda
 */

package com.emanuelef.remote_capture.activities;

import android.app.Service;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.os.Build;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.text.HtmlCompat;
import androidx.core.view.MenuProvider;

import com.emanuelef.remote_capture.CaptureService;
import com.emanuelef.remote_capture.MitmAddon;
import com.emanuelef.remote_capture.R;
import com.emanuelef.remote_capture.Utils;
import com.emanuelef.remote_capture.model.Prefs;

public class AboutActivity extends BaseActivity implements MenuProvider {
    private static final String TAG = "AboutActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.about);
        setContentView(R.layout.about_activity);
        addMenuProvider(this);

        TextView appVersion = findViewById(R.id.app_version);
        appVersion.setText("PCAPdroid " + Utils.getAppVersion(this));

        ((TextView) findViewById(R.id.app_license)).setMovementMethod(LinkMovementMethod.getInstance());
        ((TextView) findViewById(R.id.opensource_licenses)).setMovementMethod(LinkMovementMethod.getInstance());

        TextView sourceLink = findViewById(R.id.app_source_link);
        String localized = sourceLink.getText().toString();
        sourceLink.setText(HtmlCompat.fromHtml("<a href='" + MainActivity.GITHUB_PROJECT_URL + "'>" + localized + "</a>", HtmlCompat.FROM_HTML_MODE_LEGACY));
        sourceLink.setMovementMethod(LinkMovementMethod.getInstance());
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.about_menu, menu);
    }

    @Override
    public boolean onMenuItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.on_boarding) {
            Intent intent = new Intent(this, OnBoardingActivity.class);
            intent.putExtra(OnBoardingActivity.ENABLE_BACK_BUTTON, true);
            startActivity(intent);
            return true;
        } else if (id == R.id.build_info) {
            String deviceInfo = Utils.getBuildInfo(this) + "\n\n" +
                    Prefs.asString(this);

            // Private DNS
            Utils.PrivateDnsMode dns_mode = CaptureService.getPrivateDnsMode();
            if (dns_mode == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(Service.CONNECTIVITY_SERVICE);
                    Network net = cm.getActiveNetwork();

                    if (net != null) {
                        LinkProperties lp = cm.getLinkProperties(net);
                        if (lp != null)
                            dns_mode = Utils.getPrivateDnsMode(lp);
                    }
                }
            }

            if (dns_mode != null)
                deviceInfo += "\n" + "PrivateDnsMode: " + dns_mode;

            // Mitm doze
            deviceInfo += "\n" + "MitmBatteryOptimized: " + ((MitmAddon.isInstalled(this) && MitmAddon.isDozeEnabled(this)) ? "true" : "false");

            LayoutInflater inflater = LayoutInflater.from(this);
            View view = inflater.inflate(R.layout.scrollable_dialog, null);
            ((TextView) view.findViewById(R.id.text)).setText(deviceInfo);

            final String deviceInfoStr = deviceInfo;
            new AlertDialog.Builder(this)
                    .setTitle(R.string.build_info)
                    .setView(view)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    })
                    .setNeutralButton(R.string.copy_to_clipboard, (dialogInterface, i) ->
                            Utils.copyToClipboard(this, deviceInfoStr)).show();
            return true;
        }

        return false;
    }
}
