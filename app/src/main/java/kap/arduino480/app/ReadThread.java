package kap.arduino480.app;

import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by michaelk18 on 4/23/14.
 */
public class ReadThread implements Runnable
{
    private Thread thread;
    private InputStream inputStream;
    private TextView    textview;
    private MainActivity activity;
    private int          bytes;

    public ReadThread(InputStream inputStream, TextView textview, MainActivity activity)
    {
        this.inputStream = inputStream;
        this.textview   = textview;
        this.activity   = activity;
        this.bytes      = 0;
    }
    @Override
    public void run()
    {
        // Keep listening to the InputStream until an exception occurs
        while (true)
        {
            try
            {
                if(this.inputStream.available() > 0)
                {
                    final byte[] buffer = new byte[1024];  // buffer store for the stream

                    // Read from the InputStream
                    this.bytes = this.inputStream.read(buffer);        // Get number of bytes and message in "buffer"
                    this.activity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            textview.setText("Temperature reading: " + new String(buffer));
                        }
                    });
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    public void block()
    {
        try {
            thread.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
