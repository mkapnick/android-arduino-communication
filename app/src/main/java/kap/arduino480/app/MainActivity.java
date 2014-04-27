package kap.arduino480.app;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    private Button                      button;
    private BluetoothAdapter            mBluetoothAdapter;
    private final int                   REQUEST_ENABLE_BT = 1;
    private BluetoothSocket             mmSocket;
    private OutputStream                mmOutputStream;
    private InputStream                 mmInputStream;
    private BluetoothDevice             device;
    private boolean                     isOn;
    private ReadThread                  readThread;
    private TextView                    textview;
    private  int                         bytes = 0;


    public int getBytes()
    {
        return bytes;
    }

    public void setBytes(int bytes)
    {
        this.bytes = bytes;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button      = (Button) findViewById(R.id.powerLED);
        textview    = (TextView) findViewById(R.id.temperature);
        isOn = false;
        System.out.println("Setting up bluetooth");
        setUpBlueTooth();
        pairDevices();
        setUpInputAndOutputStream();
        readThread  = new ReadThread(mmInputStream,textview, this);
        readThread.start();
    }


    /**
     *
     * @param view
     */
    public void buttonClick(View view)
    {
        byte [] bytes;
        switch(view.getId())
        {
            case R.id.powerLED:
                if(!isOn) {
                    bytes = "a".getBytes();
                    try {
                        mmOutputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isOn = true;
                    button.setText("Power LED Off");
                    break;
                }
                else
                {
                    bytes = "d".getBytes();
                    try {
                        mmOutputStream.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isOn = false;
                    button.setText("Power LED On");

                    break;
                }
        }
    }

    private void pairDevices()
    {
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if(pairedDevices.size() > 0)
        {
            for(BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("SeeedBTSlave")) //Note, you will need to change this to match the name of your device
                {
                    System.out.println("Found the device");
                    this.device = device;
                    break;
                }
            }
        }
    }



    private void setUpInputAndOutputStream()
    {
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID

        System.out.println(this.device.getAddress());
        BluetoothDevice localDevice = mBluetoothAdapter.getRemoteDevice(this.device.getAddress());
        mBluetoothAdapter.cancelDiscovery();
        try {
            mmSocket = localDevice.createRfcommSocketToServiceRecord(uuid);
            mmSocket.connect();
            mmOutputStream  = mmSocket.getOutputStream();
            mmInputStream   = mmSocket.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    private void setUpBlueTooth()
    {
        Intent intent;
        mBluetoothAdapter   = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null)
        {
            System.out.println("Device does not support bluetooth");
        }

        if(!mBluetoothAdapter.isEnabled())
        {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        }
    }

    /**
     *
     */
    private void enableDiscoverability()
    {
        Intent intent;
        intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);

        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(intent);
    }

    /**
     *
     */
    private void discoverDevices()
    {
        System.out.println("In discover devices");
        IntentFilter filter;

        // Create a BroadcastReceiver for ACTION_FOUND
        BroadcastReceiver mReceiver = new BroadcastReceiver()
        {

            public void onReceive(Context context, Intent intent)
            {
                System.out.println("In on recieve");

                String          action;
                BluetoothDevice device;

                action = intent.getAction();

                // When discovery finds a device
                if (BluetoothDevice.ACTION_FOUND.equals(action))
                {
                    System.out.println("Found a device!");
                    // Get the BluetoothDevice object from the Intent
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    //this.device = device;
                }
            }
        };

        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        super.registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
    }

    /**
     *
     */
    private void connectAsClient()
    {
        //cThread = new ConnectThread(device);
        //cThread.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT)
        {
            if (resultCode == RESULT_OK)
            {
                System.out.println("Bluetooth is enabled");
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    /********************************************************************************************
     *
     *
     *
     *******************************************************************************************/
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device)
        {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try
            {
                tmp = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"));
            }
            catch (IOException e)
            { }

            mmSocket = tmp;
        }

        public void run()
        {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();

            try
            {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            }
            catch (IOException connectException)
            {
                // Unable to connect; close the socket and get out
                try
                {
                    mmSocket.close();
                }
                catch (IOException closeException)
                { }
                return;
            }

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e) { }
        }

        public void write(byte[] bytes)
        {
            new ConnectedSocket(mmSocket).write(bytes);
        }
    }

    /********************************************************************************************
     *
     *
     *
     *******************************************************************************************/
    private class ConnectedSocket extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedSocket(BluetoothSocket socket)
        {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true)
            {
                try
                {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    // Send the obtained bytes to the UI activity
                   // mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                   //         .sendToTarget();
                }
                catch (IOException e)
                {
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes)
        {
            try
            {
                mmOutStream.write(bytes);
            }
            catch (IOException e)
            { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            { }
        }
    }

}
