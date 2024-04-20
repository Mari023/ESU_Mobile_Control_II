package eu.esu.mobilecontrol2.sdk;

import static eu.esu.mobilecontrol2.sdk.InputServices.MSG_REGISTER_CLIENT;
import static eu.esu.mobilecontrol2.sdk.InputServices.MSG_UNREGISTER_CLIENT;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;

import java.lang.ref.WeakReference;


public class Controller {
    /**
     * Key event used to wake up the device.
     * <p>
     * Since {@link android.view.KeyEvent#KEYCODE_WAKEUP} is not available before api level 20 (KITKAT),
     * {@link android.view.KeyEvent#KEYCODE_BUTTON_16} is used. To avoid unexpected input events ignore this key in your
     * activity.
     * </p>
     * <p>
     * Example:
     * <pre> {@code
     * public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
     *     if (keyEvent == ThrottleFragment.KEYCODE_THROTTLE_WAKEUP) {
     *         return true;
     *     }
     *     return super.onKeyDown(keyCode, keyEvent);
     * }}
     * </pre>
     */
    public static final int KEYCODE_THROTTLE_WAKEUP = KeyEvent.KEYCODE_BUTTON_16;
    /**
     * Message to change the throttle position. Set {@link Message#arg1} to the position. Range: 0 - 255.
     */
    private static final int MSG_MOVE_TO = 3;
    /**
     * Message to set the zero position of the throttle. Set {@link Message#arg1} to the position. Range: 0 - 255.
     */
    private static final int MSG_SET_ZERO_POSITION = 4;
    /**
     * Callback message when the position has changed by user input,
     * {@link Message#arg1} contains the new throttle position. Range: 0 - 126
     */
    private static final int MSG_POSITION_CHANGED = 5;
    /**
     * Callback message when the button is pressed.
     */
    private static final int MSG_BUTTON_DOWN = 6;
    /**
     * Callback message when the button is released.
     */
    private static final int MSG_BUTTON_UP = 7;
    private static final Intent INTENT = new Intent("eu.esu.mobilecontrol2.input.THROTTLE_SERVICE").setPackage(InputServices.SERVICE_PACKAGE);
    private Messenger mSender;
    private Messenger mReceiver;
    private boolean mServiceBound;
    private int mZeroPosition;
    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSender = new Messenger(service);
            final Message register = Message.obtain(null, MSG_REGISTER_CLIENT);
            register.replyTo = mReceiver;
            sendMessage(register);

            mServiceBound = true;
            setZeroPosition(mZeroPosition);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }
    };
    private int mLastPosition;
    private OnThrottleListener mOnThrottleListener;

    private static int checkPosition(int position) {
        if (position < 0 || position > 255) {
            throw new IllegalArgumentException("position must be >= 0 and <= 255");
        }

        return position;
    }

    public void onCreate(Bundle savedInstanceState) {
        mReceiver = new Messenger(new IncomingMessageHandler(new WeakReference<>(this)));
        getActivity().bindService(INTENT, mConnection, Context.BIND_AUTO_CREATE);
    }

    public void onDestroy() {
        if (mServiceBound) {
            final Message message = Message.obtain(null, MSG_UNREGISTER_CLIENT);
            message.replyTo = mReceiver;
            sendMessage(message);

            getActivity().unbindService(mConnection);
        }
    }

    protected boolean isServiceBound() {
        return mServiceBound;
    }

    /**
     * Returns the last known position.
     *
     * @return The last known position.
     */
    public int getLastPosition() {
        return mLastPosition;
    }

    /**
     * Moves the throttle.
     *
     * @param position The new throttle position, range 0 - 255.
     * @throws java.lang.IllegalArgumentException "position" is out of range.
     */
    public void moveThrottle(int position) {
        if (isServiceBound()) {
            final Message msg = Message.obtain(null, MSG_MOVE_TO, checkPosition(position), 0);
            sendMessage(msg);
            mLastPosition = position;
        }
    }

    /**
     * Sets the listener to receive callbacks from the fragment.
     *
     * @param listener The listener.
     */
    public void setOnThrottleListener(OnThrottleListener listener) {
        mOnThrottleListener = listener;
    }

    /**
     * Return the current zero position.
     *
     * @return The current zero position.
     */
    public int getZeroPosition() {
        return mZeroPosition;
    }

    /**
     * Sets the current zero position.
     *
     * @param position The new zero position.
     */
    public void setZeroPosition(int position) {
        mZeroPosition = checkPosition(position);
        if (isServiceBound()) {
            sendMessage(Message.obtain(null, MSG_SET_ZERO_POSITION, position, 0));
        }
    }

    /**
     * Sends a message to the service.
     *
     * @param message The message.
     */
    protected void sendMessage(Message message) {
        try {
            mSender.send(message);
        } catch (final RemoteException ex) {
            Log.e("EsuInputServices", "Failed to send message", ex);
        }
    }

    private void onButtonDown() {
        if (mOnThrottleListener != null) {
            mOnThrottleListener.onButtonDown();
        }
    }

    private void onButtonUp() {
        if (mOnThrottleListener != null) {
            mOnThrottleListener.onButtonUp();
        }
    }

    private void onPositionChanged(int position) {
        if (mOnThrottleListener != null) {
            mLastPosition = position;
            mOnThrottleListener.onPositionChanged(position);
        }
    }

    private void onMessageReceived(Message message) {
        if (mOnThrottleListener != null) {
            switch (message.what) {
                case MSG_BUTTON_DOWN:
                    onButtonDown();
                    break;
                case MSG_BUTTON_UP:
                    onButtonUp();
                    break;
                case MSG_POSITION_CHANGED:
                    onPositionChanged(message.arg1);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Listener interface for throttle callbacks.
     */
    public interface OnThrottleListener {

        /**
         * Invoked after the button has been pressed.
         */
        void onButtonDown();

        /**
         * Invoked after the button has been released.
         */
        void onButtonUp();

        /**
         * Invoked after the throttle position has changed.
         *
         * @param position The new position.
         */
        void onPositionChanged(int position);
    }

    private static class IncomingMessageHandler extends Handler {
        private final WeakReference<Controller> mParent;

        public IncomingMessageHandler(WeakReference<Controller> parent) {
            mParent = parent;
        }

        @Override
        public void handleMessage(Message msg) {
            Controller parent = mParent.get();
            if (parent == null) {
                throw new AssertionError("parent is null");
            }

            parent.onMessageReceived(msg);
        }
    }
}
