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
import android.widget.Button;
import android.widget.EditText;

import com.example.test.R;

import net.rocrail.androc.Preferences;

public class ActAccessory extends ActBase {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuSelection = 0;
        connectWithService();
    }

    public void connectedWithService() {
        super.connectedWithService();
        initView();
        updateTitle(getText(R.string.Accessory).toString());
    }

    public void initView() {
        setContentView(R.layout.accessory);

        EditText et = findViewById(R.id.accAddress);
        et.setText("" + m_RocrailService.Prefs.AccNr);

        Button bt = findViewById(R.id.accAddressing);
        bt.setText(m_RocrailService.Prefs.AccType);
        bt.requestFocus();

        updateAddress();

        bt.setOnClickListener(v -> {
            Button btType = (Button) v;
            String Type = btType.getText().toString();
            if (Type.equals(Preferences.ACCTYPE_MADA)) btType.setText(Preferences.ACCTYPE_FADA);
            else if (Type.equals(Preferences.ACCTYPE_FADA))
                btType.setText(Preferences.ACCTYPE_PADA);
            else btType.setText(Preferences.ACCTYPE_MADA);

            m_RocrailService.Prefs.AccType = btType.getText().toString();
            updateAddress();
        });


        bt = findViewById(R.id.accM);
        bt.setOnClickListener(v -> {
            EditText et1 = findViewById(R.id.accAddress);
            int addr = Integer.parseInt(et1.getText().toString());
            if (addr > 1) {
                addr -= getGroupSize();
                if (addr < 1) addr = 1;
                et1.setText("" + addr);
                updateAddress();
            }
        });

        bt = findViewById(R.id.accMM);
        bt.setOnClickListener(v -> {
            EditText et12 = findViewById(R.id.accAddress);
            int addr = Integer.parseInt(et12.getText().toString());
            if (addr > 1) {
                addr -= getGroupSize() * 4;
                if (addr < 1) addr = 1;
                et12.setText("" + addr);
                updateAddress();
            }
        });

        bt = findViewById(R.id.accP);
        bt.setOnClickListener(v -> {
            EditText et13 = findViewById(R.id.accAddress);
            int addr = Integer.parseInt(et13.getText().toString());
            if (addr < 64 * 1024) {
                addr += getGroupSize();
                if (addr > 64 * 1024) addr = 64 * 1024;
                et13.setText("" + addr);
                updateAddress();
            }
        });

        bt = findViewById(R.id.accPP);
        bt.setOnClickListener(v -> {
            EditText et14 = findViewById(R.id.accAddress);
            int addr = Integer.parseInt(et14.getText().toString());
            if (addr < 64 * 1024) {
                addr += getGroupSize() * 4;
                if (addr > 64 * 1024) addr = 64 * 1024;
                et14.setText("" + addr);
                updateAddress();
            }
        });

        bt = findViewById(R.id.acc1G);
        bt.setOnClickListener(v -> switchCmd(0, 0));
        bt = findViewById(R.id.acc1R);
        bt.setOnClickListener(v -> switchCmd(0, 1));

        bt = findViewById(R.id.acc2G);
        bt.setOnClickListener(v -> switchCmd(1, 0));
        bt = findViewById(R.id.acc2R);
        bt.setOnClickListener(v -> switchCmd(1, 1));

        bt = findViewById(R.id.acc3G);
        bt.setOnClickListener(v -> switchCmd(2, 0));
        bt = findViewById(R.id.acc3R);
        bt.setOnClickListener(v -> switchCmd(2, 1));

        bt = findViewById(R.id.acc4G);
        bt.setOnClickListener(v -> switchCmd(3, 0));
        bt = findViewById(R.id.acc4R);
        bt.setOnClickListener(v -> switchCmd(3, 1));
    }


    void switchCmd(int row, int col) {
        // send the command
        Button bt = findViewById(R.id.accAddressing);
        String type = bt.getText().toString();

        EditText et = findViewById(R.id.accAddress);
        int addr = Integer.parseInt(et.getText().toString());
        int port = row + 1;

        switch (type) {
            case Preferences.ACCTYPE_MADA -> port = row + 1;
            case Preferences.ACCTYPE_FADA -> {
                addr = addr + row * 2 + col;
                port = 0;
            }
            case Preferences.ACCTYPE_PADA -> {
                port = ((addr - 1) * 4) + row * 2;
                addr = 0;
            }
        }

        String cmd = col == 0 ? "turnout" : "straight";
        m_RocrailService.sendMessage("sw", String.format("<sw cmd=\"%s\" addr1=\"%d\" port1=\"%d\"/>", cmd, addr, port));
    }


    int getGroupSize() {
        Button bt = findViewById(R.id.accAddressing);
        String type = bt.getText().toString();
        return type.equals(Preferences.ACCTYPE_FADA) ? 8 : 1;
    }

    int makeButtonAddr(String type, int addr, int row, int col) {
        return switch (type) {
            case Preferences.ACCTYPE_MADA -> row + 1;
            case Preferences.ACCTYPE_FADA -> addr + row * 2 + col;
            case Preferences.ACCTYPE_PADA -> ((addr - 1) * 4) + row * 2;
            default -> 0;
        };
    }

    void updateAddress() {
        EditText et = findViewById(R.id.accAddress);
        int addr = Integer.parseInt(et.getText().toString());
        Button bt = findViewById(R.id.accAddressing);
        String type = bt.getText().toString();

        bt = findViewById(R.id.acc1G);
        bt.setText(makeButtonAddr(type, addr, 0, 0) + "G");
        bt = findViewById(R.id.acc1R);
        bt.setText(makeButtonAddr(type, addr, 0, 1) + "R");

        bt = findViewById(R.id.acc2G);
        bt.setText(makeButtonAddr(type, addr, 1, 0) + "G");
        bt = findViewById(R.id.acc2R);
        bt.setText(makeButtonAddr(type, addr, 1, 1) + "R");

        bt = findViewById(R.id.acc3G);
        bt.setText(makeButtonAddr(type, addr, 2, 0) + "G");
        bt = findViewById(R.id.acc3R);
        bt.setText(makeButtonAddr(type, addr, 2, 1) + "R");

        bt = findViewById(R.id.acc4G);
        bt.setText(makeButtonAddr(type, addr, 3, 0) + "G");
        bt = findViewById(R.id.acc4R);
        bt.setText(makeButtonAddr(type, addr, 3, 1) + "R");
    }


    @Override
    protected void onPause() {
        super.onPause();
        EditText et = findViewById(R.id.accAddress);
        Button bt = findViewById(R.id.accAddressing);

        m_RocrailService.Prefs.saveAccessory(bt.getText().toString(), Integer.parseInt(et.getText().toString()));
    }
}
