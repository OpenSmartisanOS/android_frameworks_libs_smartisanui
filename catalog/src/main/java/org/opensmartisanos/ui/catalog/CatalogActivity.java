/*
 * Copyright (C) 2026 The OpenSmartisanOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensmartisanos.ui.catalog;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Small public-SDK-only host used to exercise and compare components. */
public final class CatalogActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int spacing = getResources().getDimensionPixelSize(
                org.opensmartisanos.ui.R.dimen.smartisan_spacing_unit);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(spacing * 4, spacing * 4, spacing * 4, spacing * 4);

        TextView title = new TextView(this);
        title.setText(R.string.smartisan_catalog_heading);
        content.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        Button dialogButton = new Button(this);
        dialogButton.setText(R.string.smartisan_catalog_show_dialog);
        dialogButton.setOnClickListener(view -> new AlertDialog.Builder(this)
                .setTitle(R.string.smartisan_catalog_dialog_title)
                .setMessage(R.string.smartisan_catalog_dialog_message)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, null)
                .show());
        content.addView(dialogButton, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        setContentView(content);
    }
}
