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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.test.R;

import net.rocrail.androc.interfaces.ModelListener;
import net.rocrail.androc.interfaces.SystemListener;
import net.rocrail.androc.objects.RRConnection;

import eu.esu.mobilecontrol2.sdk.MobileControl2;

public class ActConnect extends ActBase implements ModelListener, SystemListener, OnItemSelectedListener {
    static final int PROGRESS_DIALOG = 0;
    boolean progressPlan = false;
    boolean Reconnect = false;
    int progressValue = 0;
    ProgressDialog progressDialog = null;
    // Define the Handler that receives messages from the thread and update the progress
    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESS_DIALOG) {
                int total = msg.getData().getInt("total");
                progressDialog.setProgress(total);
                if (total >= 100 && progressDialog != null && progressDialog.isShowing()) {
                    dismissDialog(PROGRESS_DIALOG);
                    m_RocrailService.Prefs.saveConnection();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(() -> {
            MenuSelection = ActBase.MENU_PREFERENCES;
            connectWithService();
        }, 0);
    }

    public void connectedWithService() {
        super.connectedWithService();
        m_RocrailService.m_Model.addListener(this);
        m_RocrailService.addListener(this);
        initView();
    }

    @Override
    public void modelListLoaded(int MODELLIST) {
        if (!progressPlan) return;

        if (MODELLIST == ModelListener.MODELLIST_PLAN) {
            progressPlan = false;
            progressValue = 100;
        } else if (MODELLIST == ModelListener.MODELLIST_PLAN_START) {
            progressValue = 5;
        } else {
            // progress
            progressValue += 10;
        }

        Message msg = handler.obtainMessage();
        Bundle b = new Bundle();
        b.putInt("total", progressValue);
        msg.setData(b);
        handler.sendMessage(msg);

        if (progressValue >= 100) {
            ActConnect.this.throttleView();
            ActConnect.this.finish();
        }

    }

    protected Dialog onCreateDialog(int id) {
        if (id != PROGRESS_DIALOG) return null;
        progressDialog = new ProgressDialog(ActConnect.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getText(R.string.LoadingPlan));
        return progressDialog;
    }

    void doConnect(String host, int port, String ctrlcode) {
        m_RocrailService.Prefs.Host = host;
        m_RocrailService.Prefs.Port = port;
        m_RocrailService.Prefs.CtrlCode = ctrlcode;

        try {
            m_RocrailService.connect(Reconnect);
            progressPlan = true;
            progressValue = 0;

            showDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            e.printStackTrace();
            AlertDialog.Builder builder = new AlertDialog.Builder(ActConnect.this);
            builder.setMessage(e.getClass().getName() + "\nCould not connect to " + m_RocrailService.Prefs.Host + ":" + m_RocrailService.Prefs.Port).setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
                //andRoc.this.finish();
                dialog.cancel();
            });

            AlertDialog alert = builder.create();
            alert.show();

            Button v = findViewById(R.id.ButtonConnect);
            v.setEnabled(true);

        }
    }

    public void initView() {
        setContentView(R.layout.connect);

        Spinner recent = findViewById(R.id.connectRecent);

        recent.setPrompt("Select connection");

        ArrayAdapter<String> m_adapterForSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recent.setAdapter(m_adapterForSpinner);


        m_adapterForSpinner.add("none");
        // get the connections
        for (RRConnection con : m_RocrailService.Prefs.conList) {
            m_adapterForSpinner.add(con.toString());
        }
        recent.setOnItemSelectedListener(this);


        final Button button = findViewById(R.id.ButtonConnect);
        button.setOnClickListener(v -> {
            // Perform action on click
            v.setEnabled(false);

            EditText s = findViewById(R.id.connectHost);
            m_RocrailService.Prefs.Host = s.getText().toString();
            s = findViewById(R.id.controlCode);
            m_RocrailService.Prefs.CtrlCode = s.getText().toString();
            s = findViewById(R.id.connectPort);
            try {
                m_RocrailService.Prefs.Port = Integer.parseInt(s.getText().toString());
                RRConnection.addToList(m_RocrailService.Prefs.Title, m_RocrailService.Prefs.Host, m_RocrailService.Prefs.Port, m_RocrailService.Prefs.CtrlCode, m_RocrailService.Prefs.conList);

                doConnect(m_RocrailService.Prefs.Host, m_RocrailService.Prefs.Port, m_RocrailService.Prefs.CtrlCode);
            } catch (Exception e) {
                // port not a number
                v.setEnabled(true);
            }
        });

        if (m_RocrailService != null) {
            EditText s = findViewById(R.id.connectHost);
            s.setText(m_RocrailService.Prefs.Host);
            s = findViewById(R.id.connectPort);
            s.setText("" + m_RocrailService.Prefs.Port);
            TextView tv = findViewById(R.id.connectTitle);
            tv.setText(m_RocrailService.Prefs.Title);
        }

    }

    @Override
    public void SystemDisconnected() {
        Reconnect = true;
        Finish = true;
        m_RocrailService.removeListener(this);
        ActConnect.this.connectView();
        ActConnect.this.setVisible(true);
    }

    @Override
    public void SystemShutdown() {
        Reconnect = false;
        Finish = true;
        m_RocrailService.removeListener(this);
        ActConnect.this.connectView();
        ActConnect.this.setVisible(true);
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View view, int position, long longID) {
        if (position > 0) {
            Button button = findViewById(R.id.ButtonConnect);
            button.setEnabled(false);
            RRConnection con = m_RocrailService.Prefs.conList.get(position - 1);

            EditText s = findViewById(R.id.connectHost);
            s.setText(con.HostName);
            s = findViewById(R.id.connectPort);
            s.setText("" + con.Port);
            TextView tv = findViewById(R.id.connectTitle);
            tv.setText(con.Title);

            doConnect(con.HostName, con.Port, con.ControlCode);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    @Override
    public void modelUpdate(int MODELLIST, String ID) {
    }

    @Override
    protected void onStop() {
        super.onPause();
        //if( RocrailServiceConnection != null)
        //unbindService(RocrailServiceConnection);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == MobileControl2.KEYCODE_BOTTOM_RIGHT) {
            findViewById(R.id.ButtonConnect).performClick();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}

