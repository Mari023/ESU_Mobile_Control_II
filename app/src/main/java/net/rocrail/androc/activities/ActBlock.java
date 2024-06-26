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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;

import com.example.test.R;

import net.rocrail.androc.interfaces.Mobile;
import net.rocrail.androc.objects.Block;
import net.rocrail.androc.objects.Loco;
import net.rocrail.androc.widgets.LEDButton;
import net.rocrail.androc.widgets.LocoImage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ActBlock extends ActBase implements OnItemSelectedListener {
    Block m_Block = null;
    String LocoID = null;
    List<Mobile> m_LocoList = new ArrayList<>();
    int m_iLocoSelected = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuSelection = 0;
        connectWithService();
    }

    public void connectedWithService() {
        super.connectedWithService();
        initView();
    }

    void updateLoco() {
        TextView text = findViewById(R.id.blockID);
        text.setText(m_Block.ID + ": " + (LocoID == null ? "-" : LocoID));

        LocoImage image = findViewById(R.id.blockLocoImage);

        if (LocoID == null) {
            image.setImageResource(R.drawable.noimg);
            image.ID = null;
            return;
        }

        Loco lc = m_RocrailService.m_Model.getLoco(LocoID);
        image.ID = LocoID;

        if (lc != null) {
            TextView ID = findViewById(R.id.LocoBlockID);
            ID.setText(lc.getID());
            TextView Desc = findViewById(R.id.LocoBlockDesc);
            Desc.setText(lc.getDescription());
        } else {
            TextView ID = findViewById(R.id.LocoBlockID);
            ID.setText("");
            TextView Desc = findViewById(R.id.LocoBlockDesc);
            Desc.setText("");
        }

        if (lc != null && lc.getBmp(image) != null) {
            image.setImageBitmap(lc.getBmp(null));
        } else {
            image.setImageResource(R.drawable.noimg);
        }
    }

    public void initView() {
        setContentView(R.layout.block);

        for (Mobile mobile : m_RocrailService.m_Model.m_LocoMap.values()) {
            Loco loco = (Loco) mobile;
            m_LocoList.add(loco);
        }

        Collections.sort(m_LocoList, new LocoSort(m_RocrailService.Prefs.SortByAddr));

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("id");
            m_Block = m_RocrailService.m_Model.m_BlockMap.get(id);
        }

        if (m_Block == null) return;

        if (m_Block.LocoID != null) {
            Iterator<Mobile> it = m_LocoList.iterator();
            int idx = 0;
            while (it.hasNext()) {
                Loco loco = (Loco) it.next();
                if (m_Block.LocoID.equals(loco.getID())) {
                    m_iLocoSelected = idx;
                }
                idx++;
            }
        }

        LocoID = m_Block.LocoID;
        updateTitle("Block '" + m_Block.ID + "'");


        updateLoco();
        LocoImage image = findViewById(R.id.blockLocoImage);
        image.setLongClickable(true);
        image.setOnClickListener(v -> {
            Intent intent;
            if (m_RocrailService.Prefs.LocoCatList)
                intent = new Intent(m_Activity, ActLocoExpList.class);
            else intent = new Intent(m_Activity, ActLocoList.class);
            intent.putExtra("selected", m_iLocoSelected);
            startActivityForResult(intent, 1);
        });

        image.setOnLongClickListener(v -> {
            if (m_Block.LocoID != null) {
                Intent intent = new Intent(m_Activity, ActLoco.class);
                intent.putExtra("id", m_Block.LocoID);
                intent.putExtra("blockid", m_Block.ID);
                startActivity(intent);
                finish();
            }
            return true;
        });


        final Button Loco = findViewById(R.id.blockLoco);
        Loco.setOnClickListener(v -> {
            if (m_Block.LocoID != null) {
                Intent intent = new Intent(m_Activity, ActLoco.class);
                intent.putExtra("id", m_Block.LocoID);
                intent.putExtra("blockid", m_Block.ID);
                startActivity(intent);
                finish();
            }
        });


        final Button Dispatch = findViewById(R.id.blockDispatch);
        Dispatch.setOnClickListener(v -> {
            if (m_Block.LocoID != null) {
                Loco lc = m_RocrailService.m_Model.getLoco(m_Block.LocoID);
                if (lc != null) lc.Dispatch();
                finish();
            }
        });


        final Button Free = findViewById(R.id.blockFree);
        Free.setOnClickListener(v -> {
            m_RocrailService.sendMessage("bk", String.format("<bk id=\"%s\" cmd=\"loc\" locid=\"\"/>", m_Block.ID));
            finish();
        });


        final LEDButton openBlock = findViewById(R.id.blockOpen);
        openBlock.ON = !m_Block.Closed;
        openBlock.setText(m_Block.Closed ? getText(R.string.OpenBlock) : getText(R.string.CloseBlock));
        openBlock.setOnClickListener(v -> {
            m_Block.OpenClose();
            ((LEDButton) v).ON = !m_Block.Closed;
            ((LEDButton) v).setText(m_Block.Closed ? getText(R.string.OpenBlock) : getText(R.string.CloseBlock));
            finish();
        });

        final Button acceptIdentBlock = findViewById(R.id.blockAcceptIdent);
        acceptIdentBlock.setText(getText(R.string.AcceptIdent));
        acceptIdentBlock.setOnClickListener(v -> {
            m_Block.AcceptIdent();
            finish();
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> adview, View view, int position, long longID) {
        String id = (String) adview.getSelectedItem();
        LocoID = (id.equals(getText(R.string.FreeBlock).toString()) ? null : id);
        if (LocoID != null) {
            m_RocrailService.sendMessage("lc", String.format("<lc id=\"%s\" cmd=\"block\" blockid=\"%s\"/>", LocoID, m_Block.ID));
        } else {
            m_RocrailService.sendMessage("bk", String.format("<bk id=\"%s\" cmd=\"loc\" locid=\"\"/>", m_Block.ID));
        }
        updateLoco();
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode != -1) {
            m_iLocoSelected = resultCode;
            Loco loco = (Loco) m_LocoList.get(resultCode);
            if (loco != null) {
                //LocoID = (id.equals(getText(R.string.FreeBlock).toString())?null:id);
                LocoID = loco.getID();
                if (LocoID != null) {
                    m_RocrailService.sendMessage("lc", String.format("<lc id=\"%s\" cmd=\"block\" blockid=\"%s\"/>", LocoID, m_Block.ID));
                } else {
                    m_RocrailService.sendMessage("bk", String.format("<bk id=\"%s\" cmd=\"loc\" locid=\"\"/>", m_Block.ID));
                }
            } else {
                LocoID = null;
            }
            updateLoco();
        }
    }
}
