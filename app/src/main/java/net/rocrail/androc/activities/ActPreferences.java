/*
 Rocrail - Model Railroad Software

 Copyright (C) 2002-2010 - Rob Versluis <r.j.versluis@rocrail.net>

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

package net.rocrail.androc.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.test.R;

public class ActPreferences extends ActBase implements OnItemSelectedListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuSelection = 0;
        connectWithService();
    }

    public void connectedWithService() {
        super.connectedWithService();
        initView();
        updateTitle(getText(R.string.Preferences).toString());
    }

    public void initView() {
        setContentView(R.layout.preferences);
        CheckBox cb = findViewById(R.id.prefMonitoring);
        cb.setChecked(m_RocrailService.Prefs.Monitoring);
        cb = findViewById(R.id.prefKeepScreenOn);
        cb.setChecked(m_RocrailService.Prefs.KeepScreenOn);
        cb = findViewById(R.id.prefModview);
        cb.setChecked(m_RocrailService.Prefs.Modview);
        cb = findViewById(R.id.prefImagesOnDemand);
        cb.setChecked(m_RocrailService.Prefs.ImagesOnDemand);
        cb = findViewById(R.id.prefSortByAddr);
        cb.setChecked(m_RocrailService.Prefs.SortByAddr);
        cb = findViewById(R.id.prefLocoCatList);
        cb.setChecked(m_RocrailService.Prefs.LocoCatList);
        cb = findViewById(R.id.prefPowerOff4EBreak);
        cb.setChecked(m_RocrailService.Prefs.PowerOff4EBreak);
        cb = findViewById(R.id.prefSyncSpeed);
        cb.setChecked(m_RocrailService.Prefs.SyncSpeed);
        Button b = findViewById(R.id.prefClearRecent);
        b.setEnabled(!m_RocrailService.Prefs.Recent.isEmpty());
        b.setOnClickListener(v -> {
            m_RocrailService.Prefs.Recent = "";
            v.setEnabled(false);
        });

        EditText et = findViewById(R.id.prefR2RHost);
        et.setText(m_RocrailService.Prefs.RRHost);
        et = findViewById(R.id.prefR2RPort);
        et.setText("" + m_RocrailService.Prefs.RRPort);

        Spinner color = findViewById(R.id.BackgroundColor);

        color.setPrompt(getString(R.string.SelectColor));

        ArrayAdapter<String> m_adapterForSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        color.setAdapter(m_adapterForSpinner);

        m_adapterForSpinner.add(getString(R.string.Green));
        m_adapterForSpinner.add(getString(R.string.Grey));
        m_adapterForSpinner.add(getString(R.string.Blue));
        color.setOnItemSelectedListener(this);
        color.setSelection(m_RocrailService.Prefs.Color);

        Spinner category = findViewById(R.id.prefCategory);

        color.setPrompt(getString(R.string.SelectColor));

        ArrayAdapter<String> m_adapterForSpinner2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(m_adapterForSpinner2);

        m_adapterForSpinner2.add(getString(R.string.CatEngine));
        m_adapterForSpinner2.add(getString(R.string.CatEra));
        m_adapterForSpinner2.add(getString(R.string.CatRoadname));
        category.setOnItemSelectedListener(this);
        category.setSelection(m_RocrailService.Prefs.Category);
    }

    void savePrefs() {
        CheckBox cb = findViewById(R.id.prefMonitoring);
        m_RocrailService.Prefs.Monitoring = cb.isChecked();
        cb = findViewById(R.id.prefKeepScreenOn);
        m_RocrailService.Prefs.KeepScreenOn = cb.isChecked();
        cb = findViewById(R.id.prefModview);
        m_RocrailService.Prefs.Modview = cb.isChecked();
        cb = findViewById(R.id.prefImagesOnDemand);
        m_RocrailService.Prefs.ImagesOnDemand = cb.isChecked();
        cb = findViewById(R.id.prefSortByAddr);
        m_RocrailService.Prefs.SortByAddr = cb.isChecked();
        cb = findViewById(R.id.prefLocoCatList);
        m_RocrailService.Prefs.LocoCatList = cb.isChecked();
        cb = findViewById(R.id.prefPowerOff4EBreak);
        m_RocrailService.Prefs.PowerOff4EBreak = cb.isChecked();
        cb = findViewById(R.id.prefSyncSpeed);
        m_RocrailService.Prefs.SyncSpeed = cb.isChecked();

        EditText et = findViewById(R.id.prefR2RHost);
        m_RocrailService.Prefs.RRHost = et.getText().toString();
        et = findViewById(R.id.prefR2RPort);
        m_RocrailService.Prefs.RRPort = Integer.parseInt(et.getText().toString());

        Spinner color = findViewById(R.id.BackgroundColor);
        if (m_RocrailService.Prefs.Color != color.getSelectedItemPosition()) {
            m_RocrailService.Prefs.Color = color.getSelectedItemPosition();
            // Inform level view.
            if (m_RocrailService.LevelView != null) {
                m_RocrailService.LevelView.setBackgroundColor();
            }
        }

        Spinner category = findViewById(R.id.prefCategory);
        m_RocrailService.Prefs.Category = category.getSelectedItemPosition();

        m_RocrailService.Prefs.save();
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePrefs();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }
}
