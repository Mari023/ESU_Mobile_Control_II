package net.rocrail.androc.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

import com.example.test.R;

public class ActGuestLoco extends ActBase {

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
        setContentView(R.layout.guestloco);
        setTitle(R.string.GuestLoco);

        Button AddMember = findViewById(R.id.guestAdd);
        AddMember.setOnClickListener(v -> {
            EditText et = findViewById(R.id.guestAddress);
            EditText id = findViewById(R.id.guestShortID);
            RadioButton step14 = findViewById(R.id.Speedsteps14);
            RadioButton step28 = findViewById(R.id.Speedsteps28);
            RadioButton step128 = findViewById(R.id.Speedsteps128);

            RadioButton dcc = findViewById(R.id.ProtocolDCC);
            RadioButton mm = findViewById(R.id.ProtocolMM);

            int addr = Integer.parseInt(et.getText().toString());

            int steps = 128;
            if (step28.isChecked()) steps = 28;
            else if (step14.isChecked()) steps = 14;

            String prot = "P";
            if (mm.isChecked()) prot = "M";
            if (dcc.isChecked()) prot = ((addr > 127) ? "L" : "N");

            if (addr > 0) {
                m_RocrailService.sendMessage("lc", String.format("<lc id=\"%d\" shortid=\"%s\" spcnt=\"%d\" prot=\"%s\" V=\"0\"/>", addr, id.getText().toString(), steps, prot));
            }
            finish();
        });
    }
}
