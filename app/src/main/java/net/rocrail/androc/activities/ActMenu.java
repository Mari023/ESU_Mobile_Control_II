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

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.test.R;

import net.rocrail.androc.interfaces.ServiceListener;

public class ActMenu extends ListActivity implements ServiceListener {//FIXME Buttons don't work here
    ActBase m_Base = null;
    String[] m_Items = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        m_Base = new ActBase(this, this);
        m_Base.MenuSelection = 0;
        m_Base.connectWithService();
    }

    public void connectedWithService() {
        m_Base.connectedWithService();
        initView();
        m_Base.updateTitle(getText(R.string.Menu).toString());
    }


    public void initView() {
        m_Items = new String[6];
        m_Items[0] = getText(R.string.Info).toString();
        m_Items[1] = getText(R.string.Routes).toString();
        m_Items[2] = getText(R.string.Actions).toString();
        m_Items[3] = getText(R.string.Switches).toString();
        m_Items[4] = getText(R.string.Outputs).toString();
        m_Items[5] = getText(R.string.GuestLoco).toString();

        setListAdapter(new ArrayAdapter<>(this, R.layout.menuitem, m_Items));

        ListView lv = getListView();
        lv.setTextFilterEnabled(true);

        lv.setOnItemClickListener((parent, view, position, id) -> {
            // When clicked, show a toast with the TextView text
            Toast.makeText(getApplicationContext(), ((TextView) view).getText(),
                    Toast.LENGTH_SHORT).show();
            switch (position) {
                case 0: {
                    Intent intent = new Intent(ActMenu.this, ActInfo.class);
                    startActivity(intent);
                }
                break;
                case 1: {
                    Intent intent = new Intent(ActMenu.this, ActRoutes.class);
                    startActivity(intent);
                }
                break;
                case 2: {
                    Intent intent = new Intent(ActMenu.this, ActActions.class);
                    startActivity(intent);
                }
                break;
                case 3: {
                    Intent intent = new Intent(ActMenu.this, ActSwitches.class);
                    startActivity(intent);
                }
                break;
                case 4: {
                    Intent intent = new Intent(ActMenu.this, ActOutputs.class);
                    startActivity(intent);
                }
                break;
                case 5: {
                    Intent intent = new Intent(ActMenu.this, ActGuestLoco.class);
                    startActivity(intent);
                    finish();
                }
                break;
            }
        });
    }

    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        return m_Base.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        return m_Base.onOptionsItemSelected(item);
    }
}
