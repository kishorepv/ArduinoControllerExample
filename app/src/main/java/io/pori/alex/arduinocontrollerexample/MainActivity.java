package io.pori.alex.arduinocontrollerexample;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.Switch;
import android.view.View.OnClickListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;

public class MainActivity extends Activity {
    protected final int ENGINE_UDP_PORT = 3000;
    protected final int PINS_COUNT = 4;

    protected ActivityViews activityViews;
    protected Arduino arduino;
    protected boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activityViews = new ActivityViews();

        activityViews.d22.setOnClickListener(onClickListener);
        activityViews.d22.setOnClickListener(onClickListener);
        activityViews.d24.setOnClickListener(onClickListener);
        activityViews.d26.setOnClickListener(onClickListener);

        activityViews.d22.setChecked(arduino.getPinState(0));
        activityViews.d24.setChecked(arduino.getPinState(1));
        activityViews.d26.setChecked(arduino.getPinState(2));
        activityViews.d28.setChecked(arduino.getPinState(3));

        try {
            arduino = new Arduino();
        } catch (IOException ex) {

        }

        new Thread(new ArduinoDiscovery()).start();

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }

    private OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.d22:
                    try {
                        arduino.sendMessage("22");
                        arduino.togglePinState(0);
                    } catch (IOException e) {

                    }
                    break;
                case R.id.d24:
                try {
                    arduino.sendMessage("24");
                    arduino.togglePinState(1);
                } catch (IOException e) {

                }
                break;
                case R.id.d26:
                try {
                    arduino.sendMessage("26");
                    arduino.togglePinState(2);
                } catch (IOException e) {

                }
                break;
                case R.id.d28:
                try {
                    arduino.sendMessage("28");
                    arduino.togglePinState(3);
                } catch (IOException e) {

                }
                break;
            }
        }
    };

    protected void scan() throws UnknownHostException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("/proc/net/arp"));
        String line;

        while ((line = br.readLine()) != null) {
            String[] splitted = line.split(" +"); // Retrieve some network information from the other devices.

            if ((splitted != null) && (splitted.length >= 4)) {
                String mac = splitted[3];

                if (mac.matches("..:..:..:..:..:..")) {
                    final String ipAddress = splitted[0];

                    arduino.connect(ipAddress);

                    activityViews.d22.setEnabled(true);
                    activityViews.d24.setEnabled(true);
                    activityViews.d26.setEnabled(true);
                    activityViews.d28.setEnabled(true);
                }
            }
        }

        br.close(); // Close out the file reading.
    }

    protected class ActivityViews {
        public final Switch d22 = (Switch) findViewById(R.id.d22);
        public final Switch d24 = (Switch) findViewById(R.id.d24);
        public final Switch d26 = (Switch) findViewById(R.id.d26);
        public final Switch d28 = (Switch) findViewById(R.id.d28);
    }

     protected class Arduino {
        private String mIpAddress;
        private DatagramSocket mUDPClient;
        private boolean[] mPinStates;
        private boolean mIsConnected;

        public Arduino() throws IOException {
            mIpAddress = "";
            mUDPClient = new DatagramSocket();
            mPinStates = new boolean[PINS_COUNT];
            mIsConnected = false;

            for (int i = 0; i < PINS_COUNT; i++) {
                mPinStates[i] = false;
            }
        }

        public void connect(String ipAddress) throws UnknownHostException, IOException {
            mIpAddress = ipAddress;
            InetAddress address = InetAddress.getByName(mIpAddress);

            mUDPClient.connect(address, ENGINE_UDP_PORT);

            mIsConnected = true;
        }

        public void sendMessage(String msg) throws IOException {
            byte[] data = new byte[255];
            data = msg.getBytes();

            DatagramPacket sendPacket = new DatagramPacket(data, data.length);
            mUDPClient.send(sendPacket);
        }

        public boolean getPinState(int idx) {
            return mPinStates[idx];
        }

        public void togglePinState(int idx) {
            mPinStates[idx] = (mPinStates[idx] == true) ? false : true;
        }

        public String getIpAddress() {
            return mIpAddress;
        }

        public boolean isConnected() {
            return mIsConnected;
        }
    }

    protected class ArduinoDiscovery implements Runnable {
        public void run() {
            // Run while not all of the engines have been found.
            while (!arduino.isConnected()) {
                try {
                    scan();
                } catch (Exception ex) {

                }
            }
        }
    }
}
