package net.rocrail.androc.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.test.R;

import net.rocrail.androc.objects.Loco;
import net.rocrail.androc.widgets.LocoImage;

public class ActLocoConsist extends ActBase {
    Loco m_Loco = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MenuSelection = 0;
        Finish = true;
        connectWithService();
    }

    public void connectedWithService() {
        super.connectedWithService();
        initView();
    }


    public void initView() {
        setContentView(R.layout.consist);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("id");
            m_Loco = m_RocrailService.m_Model.getLoco(id);
        } else {
            m_Loco = (Loco) m_RocrailService.SelectedLoco;
        }

        if (m_Loco == null) return;

        setTitle("Consist " + m_Loco.getID());

        LocoImage image = findViewById(R.id.locoImage);

        if (m_Loco.getBmp(null) != null) {
            if (image != null) {
                image.setImageBitmap(m_Loco.getBmp(null));
            }
        }

        Button ViewConsist = findViewById(R.id.consistView);
        ViewConsist.setOnClickListener(v -> {
            if (m_Loco != null) {
                Intent intent = new Intent(m_Activity, ActLocoList.class);
                intent.putExtra("consist", m_Loco.Consist);
                startActivityForResult(intent, 3);
            }
        });

        Button AddMember = findViewById(R.id.consistAdd);
        AddMember.setOnClickListener(v -> {
            if (m_Loco != null) {
                Intent intent = new Intent(m_Activity, ActLocoList.class);
                intent.putExtra("exclude", m_Loco.getID() + "," + m_Loco.Consist);
                startActivityForResult(intent, 1);
            }
        });


        Button RemoveMember = findViewById(R.id.consistDelete);
        RemoveMember.setOnClickListener(v -> {
            if (m_Loco != null) {
                Intent intent = new Intent(m_Activity, ActLocoList.class);
                intent.putExtra("consist", m_Loco.Consist);
                startActivityForResult(intent, 2);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1 || data == null) {
            return;
        }
        if (requestCode == 1) {
            String ID = data.getCharSequenceExtra("selectedID").toString();
            m_Loco.addConsistMember(ID);
        } else if (requestCode == 2) {
            String ID = data.getCharSequenceExtra("selectedID").toString();
            m_Loco.removeConsistMember(ID);
        } else if (requestCode == 3) {
            String ID = data.getCharSequenceExtra("selectedID").toString();
            Loco l_Loco = m_RocrailService.m_Model.getLoco(ID);

            if (l_Loco != null) {
                Intent intent = new Intent(m_Activity, net.rocrail.androc.activities.ActLoco.class);
                intent.putExtra("id", l_Loco.getID());
                startActivity(intent);
            }
        }
    }
}
