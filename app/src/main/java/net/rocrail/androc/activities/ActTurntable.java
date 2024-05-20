package net.rocrail.androc.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.example.test.R;

import net.rocrail.androc.objects.Turntable;
import net.rocrail.androc.widgets.LEDButton;

public class ActTurntable extends ActBase implements OnItemSelectedListener {
    public int GotoTrack = 0;
    Turntable m_Turntable = null;

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


    public void initView() {
        setContentView(R.layout.fiddleyard);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String id = extras.getString("id");
            m_Turntable = m_RocrailService.m_Model.m_TurntableMap.get(id);
        }

        if (m_Turntable == null) return;

        final LEDButton fyNext = findViewById(R.id.fyNext);
        fyNext.ON = false;
        fyNext.setOnClickListener(v -> m_RocrailService.sendMessage("tt", String.format("<tt id=\"%s\" cmd=\"next\"/>", m_Turntable.ID)));

        final LEDButton fyPrev = findViewById(R.id.fyPrev);
        fyPrev.ON = false;
        fyPrev.setOnClickListener(v -> m_RocrailService.sendMessage("tt", String.format("<tt id=\"%s\" cmd=\"prev\"/>", m_Turntable.ID)));

        final Button fyTrack = findViewById(R.id.fyGotoTrack);
        fyTrack.setOnClickListener(v -> m_RocrailService.sendMessage("tt", String.format("<tt id=\"%s\" cmd=\"%s\"/>", m_Turntable.ID, GotoTrack)));

        final LEDButton fyOpen = findViewById(R.id.fyOpen);
        fyOpen.ON = m_Turntable.Closed;
        fyOpen.setMinWidth(200);
        fyOpen.setOnClickListener(v -> {
            m_Turntable.OpenClose();
            ((LEDButton) v).ON = !m_Turntable.Closed;
            v.invalidate();

        });


        // Track spinner
        Spinner s = findViewById(R.id.fyTracks);
        s.setPrompt("Select Track");

        ArrayAdapter<String> m_adapterForSpinner = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        m_adapterForSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(m_adapterForSpinner);


        for (Turntable.TTTrack track : m_Turntable.Tracks) {
            if (!track.Description.isEmpty()) m_adapterForSpinner.add(track.Description);
            else m_adapterForSpinner.add("" + track.Nr);
        }

        s.setOnItemSelectedListener(this);


        updateTitle(getText(R.string.Turntable) + " '" + m_Turntable.ID + "'");

    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View view, int position, long longid) {
        Spinner s = findViewById(R.id.fyTracks);
        String trackNr = (String) s.getSelectedItem();
        try {
            GotoTrack = Integer.parseInt(trackNr);
        } catch (Exception e) {
            // Description
            GotoTrack = m_Turntable.getTrackNr(trackNr);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }

}
