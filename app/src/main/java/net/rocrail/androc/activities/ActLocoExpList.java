package net.rocrail.androc.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ExpandableListView;

import com.example.test.R;

import net.rocrail.androc.interfaces.Mobile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import eu.esu.mobilecontrol2.sdk.MobileControl2;


public class ActLocoExpList extends ActBase {
    List<Mobile> m_MobileList = new ArrayList<>();
    LocoExpListAdapter m_Adapter = null;
    ExpandableListView m_ListView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.connectWithService();
        MenuSelection = MENU_PREFERENCES;
    }

    public void connectedWithService() {
        super.connectedWithService();
        initView();
    }


    public void initView() {
        setContentView(R.layout.locoexplist);
        m_ListView = findViewById(R.id.locoExpList);

        Iterator<Mobile> it = m_RocrailService.m_Model.m_LocoMap.values().iterator();
        while (it.hasNext()) {
            Mobile loco = it.next();
            if (loco.isShow()) m_MobileList.add(loco);
        }

        it = m_RocrailService.m_Model.m_CarMap.values().iterator();
        while (it.hasNext()) {
            Mobile car = it.next();
            if (car.isShow()) m_MobileList.add(car);
        }

        Collections.sort(m_MobileList, new LocoSort(m_RocrailService.Prefs.SortByAddr));


        m_ListView.setOnChildClickListener((list, view, group, position, id) -> {
            // Set selected loco.
            System.out.println("group/position=" + group + "/" + position);
            position = m_Adapter.getRealPosition(group, position);
            System.out.println("real position=" + position);
            ActLocoExpList.this.setResult(position);
            finish();
            return false;
        });

        m_Adapter = new LocoExpListAdapter(this, m_MobileList, m_RocrailService.Prefs.Category);
        m_ListView.setAdapter(m_Adapter);

        setResult(-1);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == MobileControl2.KEYCODE_BOTTOM_RIGHT)
            return true;
        return super.onKeyDown(keyCode, event);
    }
}
