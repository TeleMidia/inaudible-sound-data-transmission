package com.sample.audio.calculators;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import junit.framework.Assert;

import java.util.Arrays;

import static java.lang.System.currentTimeMillis;

public class FrequencyCalculator {

    private RealDoubleFFT spectrumAmpFFT;
    private double[] spectrumAmpOutCum;
    private double[] spectrumAmpOutTmp;
    private double[] spectrumAmpOut;
    private double[] spectrumAmpOutDB;
    private double[] spectrumAmpIn;
    private double[] spectrumAmpInTmp;
    private double[] wnd;
    private double[][] spectrumAmpOutArray;

    static private double num = 0;                    // Counter for the number of times when a freq was found
    static private int size_current = 15;              // Size of the number of bits received
    static private int fq_count[] = new int[11];       // Array of number of times where the freq was found
    static private boolean empty = true;              // If the receptivity array is empty
    static private boolean flag_new_byte = false;     // If the fq_count array is empty
    static private boolean flag_runs_printed = false; // If the results for all of the bytes received has been printed
    static private String fq_string = new String();   // String to write all fq_count values
    // Variables for tests

    static private short dataToRecieve[] =  { 1, 7, 5, 4, 19, 1 };
//    static private short dataToRecieve[] =  { 1, 17, 33, 49, 65, 81 };
    static private int size = 11;                                               // Size of the possible number to be received
    static private int dataExpected[] = new int[size];                          // A value of dataToReceive but in bits
    static private Double dataExpectedHamming[][] = null;                       // A value of dataToReceive but in bits - version for hamming codes
    static private int receptionCount = 0;                                      // Number of dataToReceive values already analyzed
    static private int receptivity[] = new int[dataToRecieve.length];           // Checking if the value of the byte is correct
    static private double receptivity_bit[] = new double[size_current];         // Sum of all the bits previously evaluated
    static private double receptivity_current[] = new double[size_current];     // Value of the current receptivity bit after evaluation
    static private long lastEmpty = currentTimeMillis();                        // Last time empty equals true
    static private double corrects[] = new double[dataToRecieve.length];        // Sum of all past values from receptivity
    static private int allruns = 1;                                             // All runs from receptivity
    static private int prog_number = 0;                                         // Number of the show is airing
    static private int prog_min = 0;                                            // Current minute of the show
    static private TextView textMinute;                                         // Object to hold id for the minute text field
    static private ImageView imageApp;                                          // Object to hold id for the app image
    static private int silence_quant = 0;                                       // Quantity of continuous silences detected
    static private boolean silence_time = false;                                // If it is during silence or not
    Double[][] matrix = null;
//    static private int syncValue = 0;                                           // If < 3 the app is not synced with the transmission
    static private boolean syncStance = false;                                  // If ref freq has a low (false) or high (true) amplitude
    private ComplexDoubleFFT spectrumComplexFFT;                                // Object for Complex FFT transform
    private double[] spectrumAmpInTmp2;                                         // Bin for double values to be stored and converted by ComplexDoubleFFT from mic input
    private double[] spectrumAmpThreshold;                                      // Bin for double values to be stored and converted by ComplexDoubleFFT from mic input and passed by a threshold
    private double[] wnd2;                                                      // Bartlett window function to be used with spectrumAmpInTmp2
    private double[] cosMod;                                                    // A cosine value to be multiplied with the spectrumAmpInTmp2
    private double fftThreshold = 0.0;                                          // Value for phase threshold usage
    private static int amostrasColetadasCount = 0;
    private static double[] amostrasColetadas;


    private int fftLen;
    private int spectrumAmpPt;
    private int spectrumAmpOutArrayPt = 0;
    private int nAnalysed = 0;


    private void init(int fftlen) {
        fftLen = fftlen;
        spectrumAmpOutCum = new double[fftlen];
        spectrumAmpOutTmp = new double[fftlen];
        spectrumAmpOut = new double[fftlen];
        spectrumAmpOutDB = new double[fftlen];
        spectrumAmpIn = new double[fftlen];
        spectrumAmpInTmp = new double[fftlen];
        spectrumAmpInTmp2 = new double[fftlen];
        spectrumAmpThreshold = new double[fftlen];
        spectrumAmpFFT = new RealDoubleFFT(fftlen);
        spectrumComplexFFT = new ComplexDoubleFFT(fftlen/2);
        spectrumAmpOutArray = new double[(int) Math.ceil((double) 1 / fftlen)][];

        double freqS[] = new double[size_current];
        int i = 0;
        cosMod = new double[fftlen];

//        amostrasColetadas = new double[fftlen];

//        while (i < size_current ){
//            freqS[i] = 19500 + 150 * i;
//            i++;
//        }

        double time[] = new double[fftlen];
        for (i = 0;  i < fftlen ; i++) {
            time[i] = 1.0/22050 * i;
        }

        for (i = 0; i < spectrumAmpOutArray.length; i++) {
            spectrumAmpOutArray[i] = new double[fftlen];
        }
        wnd = new double[fftlen];
        wnd2 = new double[2*fftlen];
        for (i = 0; i < wnd.length; i++) {
            wnd[i] = Math.asin(Math.sin(Math.PI * i / wnd.length)) / Math.PI * 2;
        }
        for (i = 0; i < wnd2.length; i++) {
            wnd2[i] = Math.asin(Math.sin(Math.PI * i / wnd.length)) / Math.PI * 2;
        }


            for (i = 0; i < cosMod.length; i++) {
                cosMod[i] = Math.cos(2 * Math.PI * 18500 * time[i]);
            }

    }

    public FrequencyCalculator(int fftlen) { init(fftlen); }

    private short getShortFromBytes(int index) {
        index *= 2;
        short buff = bytes[index + 1];
        short buff2 = bytes[index];

        buff = (short) ((buff & 0xFF) << 8);
        buff2 = (short) (buff2 & 0xFF);

        return (short) (buff | buff2);
    }

    private byte[] bytes;

    public void feedData(byte[] ds, int dsLen) {
        bytes = ds;
        int dsPt = 0;
        while (dsPt < dsLen) {
            while (spectrumAmpPt < fftLen && dsPt < dsLen) {
                double s = getShortFromBytes(dsPt++) / 32768.0;
                spectrumAmpIn[spectrumAmpPt++] = s;
            }
            if (spectrumAmpPt == fftLen) {
                for (int i = 0; i < fftLen; i++) {
//                    spectrumAmpInTmp[i] = spectrumAmpIn[i] * wnd[i] ;
                    spectrumAmpInTmp[i] = spectrumAmpIn[i];
//                    Log.d("@@@", "index "+ i + " - Value: "+spectrumAmpInTmp[i]);
                }
//                if (amostrasColetadasCount < 4000){
//                    for (int i = 0; i < spectrumAmpInTmp.length; i++) {
//                        amostrasColetadas[i] += spectrumAmpInTmp[i];
//                    }
//                    amostrasColetadasCount++;
//                }
//                else {
//                    Log.d("@@@","amostrasCount: "+amostrasColetadasCount);
//                    for (int i = 0; i < amostrasColetadas.length; i++) {
//                        Log.d("@@@", "index " + i + " - Value: " + amostrasColetadas[i]);
//                    }
//                }
//                for (int i = 0; i < fftLen; i++) {
////                    spectrumAmpInTmp2[i] = spectrumAmpIn[i];
//                    spectrumAmpInTmp2[i] = spectrumAmpIn[i] * wnd2[i];
////                    System.out.println("FFT: " + Integer.toString(i) + " " + Double.toString(spectrumAmpInTmp2[i]));
////                    System.out.flush();
//                }
//                spectrumComplexFFT.ft(spectrumAmpInTmp2);
                spectrumAmpFFT.ft(spectrumAmpInTmp);
                fftToAmp(spectrumAmpOutTmp, spectrumAmpInTmp);
                System.arraycopy(spectrumAmpOutTmp, 0, spectrumAmpOutArray[spectrumAmpOutArrayPt], 0, spectrumAmpOutTmp.length);
                System.arraycopy(spectrumAmpInTmp2, 0, spectrumAmpThreshold, 0, spectrumAmpInTmp2.length);
                spectrumAmpOutArrayPt = (spectrumAmpOutArrayPt + 1) % spectrumAmpOutArray.length;
                for (int i = 0; i < fftLen; i++) {
                    spectrumAmpOutCum[i] += spectrumAmpOutTmp[i];
                }
                nAnalysed++;
                int n2 = spectrumAmpIn.length / 2;
                System.arraycopy(spectrumAmpIn, n2, spectrumAmpIn, 0, n2);
                spectrumAmpPt = n2;
            }
        }
    }

    private void fftToAmp(double[] dataOut, double[] data) {
        double scaler = 2.0 * 2.0 / (data.length * data.length);
        dataOut[0] = data[0] * data[0] * scaler / 4.0;
        int j = 1;
        for (int i = 1; i < data.length - 1; i += 2, j++) {
            dataOut[j] = (data[i] * data[i] + data[i + 1] * data[i + 1]) * scaler;
        }
        dataOut[j] = data[data.length - 1] * data[data.length - 1] * scaler / 4.0;
    }

    private void bin (short v, int number[], int size) {
        Arrays.fill(number,0);
        for (; size - 1 >= 0; size--)
            number[size - 1] = (v >> (size - 1)) & 1;
    }

    private void addToAllArrayIndexes (double total[], int in[]){
        for (int u = 0; u < total.length; u++){
            total[u] += in[u];
        }
    }

    static public void setViews (TextView text, ImageView image){
        textMinute = text;
        imageApp = image;
    }

    private int binaryToInteger(double[] numbers) {
        int result = 0;
        for(int i=0; i < size; i++)
            if(numbers[i]==1)
                result += Math.pow(2, i);
        return result;
    }

    public double getFreq() {
        boolean [] fq = new boolean[size_current];
        boolean flag = false;                                   // A bit was found
        boolean invalid_freq = false;                           // No 0 or 1 was detected
        String invalid_freq_string = new String();              // A string to be placed the wrong freqs
        String tmpdataExpectedHamming = new String();           // A string to store the current data for a generateMatrix()
        int silence_tmp = 0;                                    // Temporary variable for counting silences
        Double checkMatrix[][] = new Double[size_current][1];   // Matrix for the received values after being checked
        int sumCheckMatrix = 0;                                 // Sum for the checkMatrix index
        double phases[] = new double[fftLen];                   // Place to store current phases of each frequency bin

//        matrix = Hamming_Code_Gen.generateMatrix("11000000000");
//
//        System.out.println("Matrix Here: "+ Hamming_Code_Gen.doubleToIntMatrix(matrix));
//        Hamming_Code_Gen.printMatrix(Hamming_Code_Gen.checkmatrix(matrix));



        for (boolean t : fq){
            t = false;
        }

        if (nAnalysed != 0) {
            int outLen = spectrumAmpOut.length;
            double[] sAOC = spectrumAmpOutCum;
            for (int j = 0; j < outLen; j++) {
                sAOC[j] /= nAnalysed;
            }
            System.arraycopy(sAOC, 0, spectrumAmpOut, 0, outLen);
            Arrays.fill(sAOC, 0.0);
            nAnalysed = 0;
            for (int i = 0; i < outLen; i++) {
                spectrumAmpOutDB[i] = 10.0 * Math.log10(spectrumAmpOut[i]);
            }
        }

        double maxAmpDB = 20 * Math.log10(0.125 / 32768);
        double maxAmpFreq = 0;
        for (int i = 1; i < spectrumAmpOutDB.length; i++) {
            if (spectrumAmpOutDB[i] > maxAmpDB) {
                maxAmpDB = spectrumAmpOutDB[i];
                maxAmpFreq = i;
            }
        }


//        for (int i = 0; i < fftLen; i++){
//            if (fftThreshold < Math.abs(spectrumAmpThreshold[i]))
//                fftThreshold = Math.abs(spectrumAmpThreshold[i]);
//        }
//        for (int i = 0; i < fftLen; i++){
//            if (Math.abs(spectrumAmpThreshold[i]) < fftThreshold/100){
//                spectrumAmpThreshold[i] = 0.0;
//            }
//        }
//
//        phases[0] =  Math.atan2(0,spectrumAmpThreshold[0])*180/Math.PI;
//        for (int i = 2; i < fftLen; i+=2){
//            phases[i/2] = Math.atan2(spectrumAmpThreshold[i+1],spectrumAmpThreshold[i])*180/Math.PI;
//            System.out.println("Phase "+(i/2)+" "+(phases[i/2])+ " "+(spectrumAmpThreshold[i+1])+ " "+(spectrumAmpThreshold[i]));
//            System.out.flush();
//        }
//
//        if (syncStance && (spectrumAmpOutDB[113] < -40) && (syncValue == 1)){
//            syncStance = false;
//            syncValue++;
//        }
//        else if (!syncStance && (spectrumAmpOutDB[113] > -40)){
//            syncStance = true;
//            syncValue++;
//        }

//        System.out.println("Sync Value: "+Integer.toString(syncValue));

        if (spectrumAmpOutDB[113] > -60) {
            if (spectrumAmpOutDB[116] > (spectrumAmpOutDB[113] - 3) && spectrumAmpOutDB[116] > -50)
                fq[0] = true;
            else if (spectrumAmpOutDB[116] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("1 - ");
            }

            if (spectrumAmpOutDB[117] > (spectrumAmpOutDB[113] - 3) && spectrumAmpOutDB[117] > -50)
                fq[1] = true;
            else if (spectrumAmpOutDB[117] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("2 - ");
            }

            if (spectrumAmpOutDB[118] > (spectrumAmpOutDB[113] - 3) && spectrumAmpOutDB[118] > -50)
                fq[2] = true;
            else if (spectrumAmpOutDB[118] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("3 - ");
            }

            if (spectrumAmpOutDB[119] > (spectrumAmpOutDB[113] - 3) && spectrumAmpOutDB[119] > -50)
                fq[3] = true;
            else if (spectrumAmpOutDB[119] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("4 - ");
            }

            if (spectrumAmpOutDB[120] > (spectrumAmpOutDB[113] - 7) && spectrumAmpOutDB[120] > -50)
                fq[4] = true;
            else if (spectrumAmpOutDB[120] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("5 - ");
            }

            if (spectrumAmpOutDB[121] > (spectrumAmpOutDB[113] - 7) && spectrumAmpOutDB[121] > -50)
                fq[5] = true;
            else if (spectrumAmpOutDB[121] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("6 - ");
            }

            if (spectrumAmpOutDB[122] > (spectrumAmpOutDB[113] - 7) && spectrumAmpOutDB[122] > -50)
                fq[6] = true;
            else if (spectrumAmpOutDB[122] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("7 - ");
            }

            if (spectrumAmpOutDB[123] > (spectrumAmpOutDB[113] - 10) && spectrumAmpOutDB[123] > -50)
                fq[7] = true;
            else if (spectrumAmpOutDB[123] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("8 - ");
            }

            if (spectrumAmpOutDB[124] > (spectrumAmpOutDB[113] - 10) && spectrumAmpOutDB[124] > -50)
                fq[8] = true;
            else if (spectrumAmpOutDB[124] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("9 - ");
            }

            if (spectrumAmpOutDB[125] > (spectrumAmpOutDB[113] - 10) && spectrumAmpOutDB[125] > -50)
                fq[9] = true;
            else if (spectrumAmpOutDB[125] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("10 - ");
            }

            if (spectrumAmpOutDB[126] > (spectrumAmpOutDB[113] - 10) && spectrumAmpOutDB[126] > -50)
                fq[10] = true;
            else if (spectrumAmpOutDB[126] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("11 - ");
            }

            if (spectrumAmpOutDB[127] > (spectrumAmpOutDB[113] - 7) && spectrumAmpOutDB[127] > -50)
                fq[11] = true;
            else if (spectrumAmpOutDB[127] < -60){
                invalid_freq = true;
                invalid_freq_string = invalid_freq_string.concat("12");
            }

        }

        int sampleRate = 44100;
//        System.out.printf("maxampfreq: %.2f freq index: %.0f fftlen: %d amplitude: %f\n",maxAmpFreq * sampleRate / fftLen,maxAmpFreq,fftLen,spectrumAmpOutDB[(int)maxAmpFreq]);
        maxAmpFreq = maxAmpFreq * sampleRate / fftLen;
        if (sampleRate / fftLen < maxAmpFreq && maxAmpFreq < sampleRate / 2 - sampleRate / fftLen) {
            int id = (int) (Math.round(maxAmpFreq / sampleRate * fftLen));
            double x1 = spectrumAmpOutDB[id - 1];
            double x2 = spectrumAmpOutDB[id];
            double x3 = spectrumAmpOutDB[id + 1];
            double a = (x3 + x1) / 2 - x2;
            double b = (x3 - x1) / 2;
            if (a < 0) {
                double xPeak = -b / (2 * a);
                if (Math.abs(xPeak) < 1) {
                    maxAmpFreq += xPeak * sampleRate / fftLen;
                }
            }
        }

        if ((currentTimeMillis()-lastEmpty)>5000 && !flag_runs_printed){

            double percent = 0;
            double allbits = allruns*dataToRecieve.length;
            for (double k :  corrects){
                percent += k;
            }
            System.out.printf("All runs : [%.2f, %.2f, %.2f, %.2f, %.2f, %.2f] - number of runs : %d - percentage: %.4f \n", corrects[0]/(allruns),corrects[1]/(allruns),corrects[2]/(allruns),corrects[3]/(allruns),corrects[4]/(allruns),corrects[5]/(allruns),allruns,percent/(allruns*6));
            System.out.printf("All bits : [%.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f, %.4f] - number of bits : %.0f\n", receptivity_bit[0]/(allbits),receptivity_bit[1]/(allbits),receptivity_bit[2]/(allbits),receptivity_bit[3]/(allbits),receptivity_bit[4]/(allbits),receptivity_bit[5]/(allbits),receptivity_bit[6]/(allbits),receptivity_bit[7]/(allbits),receptivity_bit[8]/(allbits),receptivity_bit[9]/(allbits),receptivity_bit[10]/(allbits),receptivity_bit[11]/(allbits),allbits);

            flag_runs_printed = true;
        }

//        if (syncValue < 3){
//            return maxAmpFreq;
//        }

//        if (invalid_freq){
//            System.out.printf("An invalid frequency was found in %s !\n",invalid_freq_string);
//            return maxAmpFreq;
//        }

        int q = 1;
        for (boolean t : fq){
            if (t) {
                fq_count[q-1]++;
                flag = true;

                if (!flag_new_byte){
                    flag_new_byte = true;
                    lastEmpty = currentTimeMillis();
                    flag_runs_printed = false;
                }
            }

//            if (t == false && q <= 8)
//                silence_tmp++;

//            System.out.println("is: "+Boolean.toString(t)+" Freq found: "+q+" "+ spectrumAmpOutDB[115+q]+ " "+ spectrumAmpOutDB[113]);
            q++;
            t = false;
        }

//        if (silence_tmp >= 8 )
//            silence_quant++;
//        else
//            silence_quant = 0;

        num++;

//        if (silence_quant >= 3) {
//            silence_time = true;
//            System.out.println("Silence now");
//        }
//        else
//            System.out.println("Not in silence");

//        bin(dataToRecieve[0],dataExpected,size);
//        for (int tmpIntValue: dataExpected ) {
//            tmpdataExpectedHamming += Integer.toString(tmpIntValue);
//        }
//        System.out.println("Tmp size: "+tmpdataExpectedHamming);
//        dataExpectedHamming = Hamming_Code_Gen.generateMatrix(tmpdataExpectedHamming);
//        System.out.println("Matrix Here: "+ Hamming_Code_Gen.doubleToIntMatrix(dataExpectedHamming));
//        Hamming_Code_Gen.printMatrix(dataExpectedHamming);


        if (!flag && flag_new_byte && (currentTimeMillis()-lastEmpty)>300){      // If a bit wasn't found on this run, the array is not empty and 1 sec has been passed since the byte started to be analyzed
//            System.out.println("Empty T: "+ Long.toString(currentTimeMillis()-lastEmpty));
            System.out.flush();
            if (receptionCount == 0) {                                            // If a new set of bytes is being analyzed
                Assert.assertEquals(true,empty);
                Arrays.fill(receptivity,1);
            }
            q=0;
            bin(dataToRecieve[receptionCount],dataExpected,size);
//            for (int tmpIntValue: dataExpected ) {
//                tmpdataExpectedHamming += Integer.toString(tmpIntValue);
//            }
//            dataExpectedHamming = Hamming_Code_Gen.generateMatrix(tmpdataExpectedHamming);
//            System.out.println("Matrix Here: "+ Hamming_Code_Gen.doubleToIntMatrix(dataExpectedHamming));
//            Hamming_Code_Gen.printMatrix(dataExpectedHamming);

            for (int j: fq_count) {

//                System.out.printf("How are the numbers? %.2f < %.2f - place %d\n",(double) j/num, (double) 2/3,q+1);

//                assert dataExpectedHamming[q][0] == 1 || dataExpectedHamming[q][0] == 0;
                assert dataExpected[q] == 1 || dataExpected[q] == 0;

                if (dataExpected[q] == 1 && dataExpected[q] >  j && ((double) j/num < (double) 2/3) ){
//                    System.out.printf("Look: expected %f < received %d - percentage: %.2f - local %d - reception %d\n",dataExpectedHamming[q][0], j, (double) j/num, q+1, receptionCount+1);
                    receptivity[receptionCount] = 0;
                }
                else if (dataExpected[q] == 0 && dataExpected[q] < j && ((double) j/num >= (double) 2/3)){
//                    System.out.printf("Look: expected %f >= received %d - percentage: %.2f - local %d - reception %d\n",dataExpectedHamming[q][0], j, (double) j/num, q+1, receptionCount+1);
                    receptivity[receptionCount] = 0;
                }
                else { // Data is correct
                    receptivity_bit[q]++;
                    if (dataExpected[q] == 1)
                        receptivity_current[q] = 1;
                    else
                        receptivity_current[q] = 0;
                }

                fq_string = fq_string.concat(Integer.toString(q+1)+" "+Integer.toString(j)+" - ");
//                System.out.printf("\nNumber of times found freq %d - %d\n",q+1,j);
                q++;
            }

//            q = 0;
//            for (Double tmpDoubleValue: receptivity_current) {
//                checkMatrix[q][0] = tmpDoubleValue;
//                q++;
//            }
//
//            checkMatrix = Hamming_Code_Gen.checkmatrix(checkMatrix);
////            System.out.println("Check matrix:");
////            Hamming_Code_Gen.printMatrix(checkMatrix);
//
//            q = 0;
//            for (Double tmpSumAr[]: checkMatrix) {
//                for (Double tmpSumVal: tmpSumAr) {
//                    sumCheckMatrix += (int) (Math.pow(2,q)*tmpSumVal);
//                    q++;
//                }
//            }
////            System.out.printf("Check Value: %d - number of indexes: %d\n",sumCheckMatrix,q);
//
//            if (sumCheckMatrix > 0){
//                if (receptivity_current[sumCheckMatrix-1] == 1)
//                    receptivity_current[sumCheckMatrix-1] = 0;
//                else
//                    receptivity_current[sumCheckMatrix-1] = 1;
//            }

//            System.out.println(Arrays.toString(receptivity_current));

//            prog_min = 0;
//            prog_number = 0;
//            for (int j = 0; j < size_current; j++){
//                if (receptivity_current[j] == 1) {
//                    if (j == 2) {
//                        prog_number += Math.pow(2, j-2);
//                    }
//                    else if (j >= 4 && j <= 6 ){
//                        prog_number += Math.pow(2, j-3);
//                    }
//                    else if (j >= 8 && j <= 11) {
//                        prog_min += Math.pow(2, j-8);
//                    }
//                }
//            }

//            System.out.printf("Program Number: %d - Minute:%d\n",prog_number,prog_min);

//            if (prog_number != 0b1){
//                textMinute.post(new Runnable() {
//                    public void run() {
//                        textMinute.setVisibility(View.INVISIBLE);
//                    }
//                });
//            }
//            else if (prog_number == 0b1){
//
//                textMinute.post(new Runnable() {
//                    public void run() {
//                        textMinute.setText("Min: "+Integer.toString(prog_min));
//                        textMinute.setVisibility(View.VISIBLE);
//                    }
//                });
//
//
//                if (prog_min == 0b11){
//                    imageApp.post(new Runnable() {
//                        public void run() {
//                            imageApp.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//                else {
//                    imageApp.post(new Runnable() {
//                        public void run() {
//                            imageApp.setVisibility(View.INVISIBLE);
//                        }
//                    });
//                }
//            }
            final int currentIntMessage = binaryToInteger(receptivity_current);
            textMinute.post(new Runnable() {
                public void run() {
                    textMinute.setText(Integer.toString(currentIntMessage));
                    textMinute.setVisibility(View.VISIBLE);
                }
            });

            Arrays.fill(fq_count,0);
            flag_new_byte = false;
//            lastEmpty = currentTimeMillis();
            System.out.printf("Number of times found freqs: %s - num: %.0f\n",currentIntMessage,num);
            System.out.flush();
            fq_string = "";
            num = 0;
//            System.out.println("Changed the number!!!!!!");
//            System.out.println("Reception count: "+Integer.toString(receptionCount)+"\n");
            System.out.flush();
            receptionCount++;
            if (receptionCount >= dataToRecieve.length) {
                receptionCount = 0;
//                System.out.println("Correctness: "+Arrays.toString(receptivity)+"\n");
//                System.out.println("All bytes read!\n");
                addToAllArrayIndexes(corrects,receptivity);
                empty = true;
//                System.out.printf("All runs : [%.2f, %.2f, %.2f, %.2f, %.2f, %.2f]\n", corrects[0]/allruns,corrects[1]/allruns,corrects[2]/allruns,corrects[3]/allruns,corrects[4]/allruns,corrects[5]/allruns);
                System.out.flush();
                allruns++;
//                syncValue = 0;
                syncStance = false;
            }
        }

        if (!empty && (currentTimeMillis()-lastEmpty)>2000) {
//            System.out.println("Current array :"+Arrays.toString(fq_count)+" time: "+ Long.toString(currentTimeMillis()-lastEmpty));
            Arrays.fill(fq_count,0);
            empty = true;
            flag_new_byte = false;
            fq_string = "";
            receptionCount = 0;
            allruns++;
//            syncValue = 0;
            syncStance = false;
            System.out.println("Silence greater than 2s\n");
            System.out.flush();
        }

//        silence_time = false;



        return maxAmpFreq;
    }
}
