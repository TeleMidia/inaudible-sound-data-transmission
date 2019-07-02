package com.sample;

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.TextView;

import com.sample.audio.calculators.AudioCalculator;
import com.sample.audio.core.Callback;
import com.sample.audio.core.Recorder;

import static java.lang.System.currentTimeMillis;

public class MainActivity extends Activity {

    private Recorder recorder;
    private AudioCalculator audioCalculator;
    private Handler handler;

    private TextView textAmplitude;
    private TextView textDecibel;
    private TextView textFrequency;

    double average=0, num=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        CheckForRecordAudioPermission();

        long timezero, timefull;

        timezero = currentTimeMillis();
        recorder = new Recorder(callback);
        audioCalculator = new AudioCalculator();
        handler = new Handler(Looper.getMainLooper());
        timefull = currentTimeMillis();

        Log.d("Timecheck: ", "Time Create:"+ Long.toString(timefull-timezero));

        textAmplitude = (TextView) findViewById(R.id.textAmplitude);
        textDecibel = (TextView) findViewById(R.id.textDecibel);
        textFrequency = (TextView) findViewById(R.id.textFrequency);
    }

    private void CheckForRecordAudioPermission() {
        if (android.os.Build.VERSION.SDK_INT >= 23)
        {
            // ANDROID 6.0 AND UP!
            boolean accessRecordAudio = false;
            try
            {
                // Invoke checkSelfPermission method from Android 6 (API 23 and UP)
                java.lang.reflect.Method methodCheckPermission = Activity.class.getMethod("checkSelfPermission", java.lang.String.class);
                Object resultObj = methodCheckPermission.invoke(this, Manifest.permission.RECORD_AUDIO);
                int result = Integer.parseInt(resultObj.toString());
                if (result == PackageManager.PERMISSION_GRANTED)
                {
                    accessRecordAudio = true;
                }
            }
            catch (Exception ex)
            {
            }
            if (accessRecordAudio)
            {
                return;
            }
            try
            {
                // We have to invoke the method "void requestPermissions (Activity activity, String[] permissions, int requestCode) "
                // from android 6
                java.lang.reflect.Method methodRequestPermission = Activity.class.getMethod("requestPermissions", java.lang.String[].class, int.class);
                methodRequestPermission.invoke(this, new String[]
                        {
                                Manifest.permission.RECORD_AUDIO
                        }, 0x12345);
            }
            catch (Exception ex)
            {
            }
        }
    }

    private Callback callback = new Callback() {

        @Override
        public void onBufferAvailable(byte[] buffer) {
            long time1,time1a,time2,time3,time4;
            time1 = currentTimeMillis();
            audioCalculator.setBytes(buffer);
            time1a = currentTimeMillis();
//            int amplitude = audioCalculator.getAmplitude();
            time2 = currentTimeMillis();
//            double decibel = audioCalculator.getDecibel();
            time3 = currentTimeMillis();
            double frequency = audioCalculator.getFrequency();
            time4 = currentTimeMillis();

//            Log.d("Timecheck: ", "Time Base:"+ Long.toString(time4-time1) + " Calculator:"+ Long.toString(time1a-time1) +" Amplitude:"+ Long.toString(time2-time1a) + " Decibel:"+ Long.toString(time3-time2) + " Frequency:"+ Long.toString(time4-time3));
            num++;
            average+=time4-time1;
//            Log.d("Timecheck: ", "Time Average:"+ Double.toString(average/num));

//            final String amp = String.valueOf(amplitude + " Amp");
//            final String db = String.valueOf(decibel + " db");
            final String hz = String.valueOf(frequency + " Hz");

            handler.post(new Runnable() {
                @Override
                public void run() {
//                    textAmplitude.setText(amp);
//                    textDecibel.setText(db);
                    textFrequency.setText(hz);
                }
            });
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        recorder.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        recorder.stop();
    }
}
