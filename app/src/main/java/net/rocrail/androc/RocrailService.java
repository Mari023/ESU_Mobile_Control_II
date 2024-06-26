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
package net.rocrail.androc;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import com.example.test.R;

import net.rocrail.androc.activities.ActLevel;
import net.rocrail.androc.activities.ActSystem;
import net.rocrail.androc.interfaces.MessageListener;
import net.rocrail.androc.interfaces.Mobile;
import net.rocrail.androc.interfaces.PoMListener;
import net.rocrail.androc.interfaces.SystemListener;
import net.rocrail.androc.objects.Item;

import org.xml.sax.Attributes;

import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import eu.esu.mobilecontrol2.sdk.MobileControl2;

public class RocrailService extends Service {
    private static final int NOTIFICATION_POWER = 1;
    private final List<SystemListener> m_Listeners = new ArrayList<>();
    private final List<PoMListener> m_PoMListeners = new ArrayList<>();
    private final IBinder rocrailBinder = new RocrailLocalBinder();
    public Preferences Prefs = null;
    public Model m_Model = null;
    public Mobile SelectedLoco = null;
    public ActLevel LevelView = null;
    public boolean Power = false;
    public boolean EBrake = false;
    public boolean AutoMode = false;
    public boolean AutoStart = false;
    public boolean Connected = false;
    public int ThrottleNr = 1;
    public List<String> MessageList = new ArrayList<>();
    Socket m_Socket = null;
    Connection m_Connection = null;
    MessageListener messageListener = null;


    public RocrailService() {
    }

    @Override
    public void onCreate() {
        m_Model = new Model(this);
        Prefs = new Preferences(this);
        Prefs.restore();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return rocrailBinder;
    }

    public void addListener(SystemListener listener) {
        m_Listeners.add(listener);
    }

    public void addPoMListener(PoMListener listener) {
        m_PoMListeners.add(listener);
    }

    public void removeListener(SystemListener listener) {
        m_Listeners.remove(listener);
    }

    public void connect(boolean reconnect) throws Exception {
        // TODO: clean up all activities and model

        try {
            if (m_Connection != null) {
                m_Connection.stopReading();
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        m_Socket = new Socket(Prefs.Host, Prefs.Port);

        if (!reconnect) {
            sendMessage("model", String.format("<model cmd=\"plan\" controlcode=\"%s\" disablemonitor=\"%s\"/>", Prefs.CtrlCode, Prefs.Monitoring ? "false" : "true"));
        }

        if (m_Connection == null) {
            m_Connection = new Connection(this, m_Model, m_Socket);
            m_Connection.start();
        }
        m_Connection.startReading();
        Connected = true;
    }

    public void onDestroy() {
        disConnect(true);
    }

    public void disConnect(boolean stop) {
        try {
            m_Connection.stopReading();
            if (stop) m_Connection.stopRunning();
            Thread.sleep(500);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (m_Socket != null) {
                m_Socket.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        Connected = false;
        m_Socket = null;

    }

    public String getDeviceName() {
        return Build.DEVICE;
    }

    public synchronized void sendMessage(String name, String msg) {
        if (m_Socket != null && m_Socket.isConnected() && !m_Socket.isClosed() && !m_Socket.isOutputShutdown() && !m_Socket.isInputShutdown()) {
            try {
                int msgLen = msg.getBytes(Charset.forName("UTF-8")).length;
                String stringToSend = String.format("<xmlh><xml size=\"%d\" name=\"%s\"/></xmlh>%s", msgLen, name, msg);
                byte[] msgToSend = stringToSend.getBytes(Charset.forName("UTF-8"));
                m_Socket.getOutputStream().write(msgToSend);
            } catch (Exception e) {
                e.printStackTrace();
                informListeners(SystemListener.EVENT_DISCONNECTED);
            }
        }
    }

    public void informPoMListeners(int addr, int cv, int value) {
        for (PoMListener listener : m_PoMListeners) {
            listener.ReadResponse(addr, cv, value);
        }
    }

    public void informListeners(String event) {
        if (SystemListener.EVENT_SHUTDOWN.equals(event)) {
            disConnect(false);
            // TODO: remove all views and clean up the model...

            // set the connection view active again
            for (SystemListener listener : m_Listeners) {
                listener.SystemShutdown();
            }
        } else if (SystemListener.EVENT_DISCONNECTED.equals(event)) {
            m_Socket = null;
            Connected = false;

            // set the connection view active again
            for (SystemListener listener : m_Listeners) {
                listener.SystemDisconnected();
            }
        }
    }

    public synchronized void event(String itemtype, Attributes atts) {
        if (itemtype.equals("sys")) {
            informListeners(Item.getAttrValue(atts, "cmd", ""));
            return;
        }

        if (itemtype.equals("program")) {
            String cmd = Item.getAttrValue(atts, "cmd", "");
            int cv = Item.getAttrValue(atts, "cv", 0);
            int val = Item.getAttrValue(atts, "value", 0);
            if (cmd.equals("7") || cmd.equals("8")) {
                informPoMListeners(0, cv, val);
            }
            return;
        }

        if (itemtype.equals("state")) {
            boolean l_Power = Item.getAttrValue(atts, "power", Power);
            if (Power && !l_Power) {

                int icon = R.drawable.power_notify;        // icon from resources
                CharSequence tickerText = "Power Down";              // ticker-text
                long when = System.currentTimeMillis();         // notification time
                Context context = getApplicationContext();      // application Context
                CharSequence contentTitle = "Rocrail Event";  // expanded message title
                CharSequence contentText = "Global Power is down.";      // expanded message text

                Intent notificationIntent = new Intent(this, ActSystem.class);
                PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

                // the next two lines initialize the Notification, using the configurations above
                Notification notification = new Notification.Builder(context).setSmallIcon(icon).setTicker(tickerText).setWhen(when).setContentTitle(contentTitle).setContentText(contentText).setContentIntent(contentIntent).build();
                notification.flags = notification.flags | Notification.FLAG_AUTO_CANCEL;

                NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.notify(NOTIFICATION_POWER, notification);
            }

            Power = l_Power;

            //set LED based on power and reset EBrake
            if (Power) {
                EBrake = false;
                MobileControl2.setLedState(MobileControl2.LED_GREEN, true);
                MobileControl2.setLedState(MobileControl2.LED_RED, false);
            } else {
                MobileControl2.setLedState(MobileControl2.LED_GREEN, false);
                MobileControl2.setLedState(MobileControl2.LED_RED, true);
            }
            return;
        }
        if (itemtype.equals("auto")) {
            String cmd = Item.getAttrValue(atts, "cmd", "");
            if (cmd.equals("on") || cmd.equals("off")) AutoMode = cmd.equals("on");
            return;
        }
        if (itemtype.equals("exception")) {
            String text = Item.getAttrValue(atts, "text", null);
            if (text != null && !text.isEmpty()) {
                synchronized (MessageList) {
                    MessageList.add(0, text);
                    if (MessageList.size() > 50) {
                        MessageList.remove(MessageList.size() - 1);
                    }
                    if (messageListener != null) messageListener.newMessages();
                }
            }
        }
    }

    public synchronized void setMessageListener(MessageListener listener) {
        messageListener = listener;
    }

    public void EBrake() {
        EBrake = true;
        if (Prefs.PowerOff4EBreak)
            sendMessage("sys", "<sys cmd=\"stop\" informall=\"true\"/>");
        else {
            sendMessage("sys", "<sys cmd=\"ebreak\" informall=\"true\"/>");
            MobileControl2.setLedState(MobileControl2.LED_GREEN, true);
            MobileControl2.setLedState(MobileControl2.LED_RED, true);
        }
    }

    public class RocrailLocalBinder extends Binder {
        public RocrailService getService() {
            return RocrailService.this;
        }

        public Model getModel() {
            return m_Model;
        }
    }
}
