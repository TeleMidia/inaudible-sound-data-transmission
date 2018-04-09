package com.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
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
            Log.d("Timecheck: ", "Time Average:"+ Double.toString(average/num));

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
