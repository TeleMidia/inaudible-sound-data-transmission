package com.example.joao.bgchange;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ca.uol.aig.fftpack.RealDoubleFFT;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;
import static java.lang.System.currentTimeMillis;

public class MainActivity extends Activity implements View.OnClickListener {

    int frequency = 44100;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;

    AudioRecord audioRecord;
    private RealDoubleFFT transformer;
    int blockSize;
    Button startStopButton;
    EditText txtLimiter;
    TextView txtTimeForOk;
    TextView txtAllOks;
    boolean started = false;
    boolean CANCELLED_FLAG = false;
    boolean freqachieved = false;
    boolean freqachieved2 = false;
    boolean freqachieved3 = false;
    boolean freqachieved4 = false;


    RecordAudio recordTask;

    ConstraintLayout main;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO fix opening app while in landscape
        setRequestedOrientation(SCREEN_ORIENTATION_USER_PORTRAIT);

        getActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        CheckForRecordAudioPermission();

        this.main = (ConstraintLayout) this.findViewById(R.id.regconstraint);

        this.txtLimiter = (EditText) this.findViewById(R.id.txtLimits);

        this.txtTimeForOk = (TextView) this.findViewById(R.id.txtTimeOk);
        this.txtAllOks = (TextView) this.findViewById(R.id.txtOks);

        this.startStopButton = (Button)this.findViewById(R.id.btnststp);
        this.startStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (started == true) {
                    //started = false;
                    CANCELLED_FLAG = true;
                    //recordTask.cancel(true);
                    try{
                        audioRecord.stop();
                    }
                    catch(IllegalStateException e){
                        Log.e("Stop failed", e.toString());

                    }
                    startStopButton.setText("Start");
                }

                else {
                    started = true;
                    CANCELLED_FLAG = false;
                    startStopButton.setText("Stop");
                    recordTask = new RecordAudio();
                    recordTask.execute();
                }

            }
        });


        blockSize = 1024;
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

    private class RecordAudio extends AsyncTask<Void, double[], Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {

            int bufferSize = AudioRecord.getMinBufferSize(frequency,
                    channelConfiguration, audioEncoding);
            audioRecord = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, frequency,
                    channelConfiguration, audioEncoding, bufferSize);
            int bufferReadResult;
            short[] buffer = new short[blockSize];
            double[] toTransform = new double[blockSize];
            try {
                audioRecord.startRecording();
            } catch (IllegalStateException e) {
                Log.e("Recording failed", e.toString());

            }
            while (started) {

                if (isCancelled() || (CANCELLED_FLAG == true)) {

                    started = false;
                    Log.d("doInBackground", "Cancelling the RecordTask");
                    break;
                } else {
                    bufferReadResult= audioRecord.read(buffer, 0, blockSize);

                    for (int i = 0; i < blockSize && i < bufferReadResult; i++) {
                        toTransform[i] = (double) buffer[i] / 32768.0; // signed 16 bit
                    }

                    transformer.ft(toTransform);

                    publishProgress(toTransform);

                }

            }
            return true;
        }

        int counterAll = 0;
        int counterOK = 0;
        boolean changeGranted = false, timeLoopDone = false;
        long timeZero = 0, timeOK;


        @Override
        protected void onProgressUpdate(double[]...progress) {
            String strlimit = txtLimiter.getText().toString();
            double limit;
            double intensityAverage = 0;
            double intensityTotal = 0;
            boolean okfound = false;
            int intensityCount = 0;
            if (strlimit.isEmpty() || strlimit.equals(".")){
                limit = 1;
            }
            else {
                limit = Double.valueOf(strlimit);
            }

            Log.e("RecordingProgress", "Displaying in progress"); // Print for each iteration beginning


            if (counterAll >= 500){
                counterAll = 0;
                counterOK = 0;
                timeLoopDone = false;
            }

            for (int i = 0; i < progress[0].length; i++) {
                int x = i;
               // Log.d("Posx - Posy", Integer.toString(x) +" "+  Double.toString((Double)progress[0][i]));
                // --- for progression acknowledgment inside the array
                if (counterAll == 0){
                    timeZero = currentTimeMillis();
                }

                if ((x >= 787) && (x <= 870)){
                    intensityCount++;
                    intensityTotal += progress[0][i];
                    intensityAverage = intensityTotal / intensityCount;
                }

                if (((x >= 809) && (x <= 816)) && (((int)progress[0][i] > 2) || ((int)progress[0][i] < -2))){
                 //  Freq 17500
                    freqachieved = true;
                }
               else if (((x >= 787) && (x <= 791)) && (((int)progress[0][i] > 2) || ((int)progress[0][i] < -2))){
                    //   Freq 17000
                    freqachieved2 = true;
                }
               else if (((x >= 855) && (x <= 862)) && ((progress[0][i] >  2) || (progress[0][i] < -2))){
                    //  Freq 18500
                    freqachieved3 = true;
                }
               else if (((x >= 880) && (x <= 884)) && ((progress[0][i] > (intensityAverage + limit)) || (progress[0][i] < -(intensityAverage + limit)))){
                    //   Freq 19000
                    freqachieved4 = true;
                    if (!okfound) {
                        counterOK++;
                        okfound = true;
                    }
                }
            }
            counterAll++;
            txtAllOks.setText("Ok: " + Integer.toString(counterOK) + "------ All: " + Integer.toString(counterAll));



           // Log.d("ALL:", Integer.toString(counterAll)); --- Number of cycles of fft already analyzed
           // Log.d("OK:", Integer.toString(counterOK));  --- Number of correct frequencies found in each cycle for 19000 hz
           // Log.d("Intensity:", Double.toString(intensityAverage));  --- Average of frequencies threshold between 17000ish hz to 18990ish hz

            if (counterOK >= 100){
                changeGranted = true;
                if (!timeLoopDone){
                    timeOK = currentTimeMillis();
                    txtTimeForOk.setText("Time for Ok: " + Long.toString(timeOK - timeZero));
                    timeLoopDone = true;
                }

            }
            else if(counterOK <100 && counterAll >= 250){
                changeGranted = false;
            }

            if (freqachieved){
                main.setBackgroundColor(Color.argb(255,0,255,0));
               //  Green

            }
            else if (freqachieved2){
                main.setBackgroundColor(Color.argb(255,0,0,255));
                // Blue
            }
            else if (freqachieved3){
                main.setBackgroundColor(Color.argb(255,255,0,0));
                // Red
            }
            else if (changeGranted){ //   else if (changeGranted && freqachieved4){   --- for a real time freq identifier
                main.setBackgroundColor(Color.argb(255,255,255,0));
                // Yellow
            }
            else {
                main.setBackgroundColor(Color.argb(255,255,255,255));
                // White
            }
            //---------------------------------

            freqachieved = false;
            freqachieved2 = false;
            freqachieved3 = false;
            freqachieved4 = false;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }
            main.setBackgroundColor(Color.argb(255,255,255,255));
        }
    }

    protected void onCancelled(Boolean result){

        try{
            audioRecord.stop();
        }
        catch(IllegalStateException e){
            Log.e("Stop failed", e.toString());

        }
    }

    public void onWindowFocusChanged(boolean hasFocus)  {
        super.onWindowFocusChanged(hasFocus);

        if (!hasFocus) {

            if (started == true) {
                CANCELLED_FLAG = true;
                try{
                    audioRecord.stop();
                }
                catch(IllegalStateException e){
                    Log.e("Stop failed", e.toString());

                }
                startStopButton.setText("Start");

            }

            if (audioRecord != null){

                try{
                    audioRecord.stop();
                }
                catch(IllegalStateException e){
                    Log.e("Stop failed", e.toString());

                }
                recordTask.cancel(true);
            }
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    public void onClick(View v) {
        if (started == true) {
            CANCELLED_FLAG = true;
            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }
            startStopButton.setText("Start");


        }

        else {
            started = true;
            CANCELLED_FLAG = false;
            startStopButton.setText("Stop");
            recordTask = new RecordAudio();
            recordTask.execute();
        }

    }

    public void onStop() {

        super.onStop();

        if(recordTask != null)
            recordTask.cancel(true);

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void onStart(){

        super.onStart();
        startStopButton.setText("Start");
        transformer = new RealDoubleFFT(blockSize);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (audioRecord != null){

            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }
            recordTask.cancel(true);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (audioRecord != null){

            try{
                audioRecord.stop();
            }
            catch(IllegalStateException e){
                Log.e("Stop failed", e.toString());

            }
            recordTask.cancel(true);
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}