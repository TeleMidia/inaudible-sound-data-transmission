package com.sample.audio.calculators;

import static java.lang.System.currentTimeMillis;

import java.util.Arrays;

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

    static protected double num = 0, correct_0 = 0, correct_1 = 0, correct_2 = 0;
    static protected int fq_count[] = new int[12];
    static protected boolean empty = true;
    static protected String fq_string = new String();
    // Variables for tests
    protected short dataToRecieve[] =  { 1, 7, 5, 4, 19, 1 };
    static protected int size = 16;
    static protected int dataReceived[] = new int[size];
    static protected int receptionCount = 0;

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
        spectrumAmpFFT = new RealDoubleFFT(fftlen);
        spectrumAmpOutArray = new double[(int) Math.ceil((double) 1 / fftlen)][];

        for (int i = 0; i < spectrumAmpOutArray.length; i++) {
            spectrumAmpOutArray[i] = new double[fftlen];
        }
        wnd = new double[fftlen];
        for (int i = 0; i < wnd.length; i++) {
            wnd[i] = Math.asin(Math.sin(Math.PI * i / wnd.length)) / Math.PI * 2;
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
                    spectrumAmpInTmp[i] = spectrumAmpIn[i] * wnd[i];
                }
                spectrumAmpFFT.ft(spectrumAmpInTmp);
                fftToAmp(spectrumAmpOutTmp, spectrumAmpInTmp);
                System.arraycopy(spectrumAmpOutTmp, 0, spectrumAmpOutArray[spectrumAmpOutArrayPt], 0, spectrumAmpOutTmp.length);
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

    void bin (short v, int number[], int size) {
        Arrays.fill(number,0);
        for (; size - 1 >= 0; size--)
            number[size - 1] = (v >> (size - 1)) & 1;
    }

    public double getFreq() {
        boolean [] fq = new boolean[12];
        boolean flag = false;
        long timezero, timefull;

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

        if (spectrumAmpOutDB[116] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[0] = true;
        if (spectrumAmpOutDB[117] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[1] = true;
        if (spectrumAmpOutDB[118] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[2] = true;
        if (spectrumAmpOutDB[119] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[3] = true;
        if (spectrumAmpOutDB[120] > (spectrumAmpOutDB[113]-8) && spectrumAmpOutDB[113] > -60) fq[4] = true;
        if (spectrumAmpOutDB[121] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[5] = true;
        if (spectrumAmpOutDB[122] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[6] = true;
        if (spectrumAmpOutDB[123] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[7] = true;
        if (spectrumAmpOutDB[124] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[8] = true;
        if (spectrumAmpOutDB[125] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[9] = true;
        if (spectrumAmpOutDB[126] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[10] = true;
        if (spectrumAmpOutDB[127] > (spectrumAmpOutDB[113]-3) && spectrumAmpOutDB[113] > -60) fq[11] = true;


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
//                System.out.printf("x1:%f x2:%f x3:%f a:%f b:%f xPeak:%f\n",x1,x2,x3,a,b,xPeak);
                if (Math.abs(xPeak) < 1) {
                    maxAmpFreq += xPeak * sampleRate / fftLen;
                }
            }
        }

//        if (fq[0])
//            correct_0++;
//
//        if (!fq[1])
//            correct_1++;
//        if (fq[2])
//            correct_2++;
//
//        num++;

        int q = 1;
        for (boolean t : fq){
            if (t) {
                fq_count[q-1]++;
                flag = true;
                empty = false;
            }

            System.out.println("is: "+Boolean.toString(t)+" Freq found: "+q+" "+ spectrumAmpOutDB[115+q]+ " "+ spectrumAmpOutDB[113]);
            q++;
            t = false;
        }

        if (!flag & !empty){
            timezero = currentTimeMillis();
            q=0;
            bin(dataToRecieve[receptionCount],dataReceived,size);
            for (int j: fq_count) {
                if (dataReceived[q] <= j){
                    break;
                }
                fq_string = fq_string.concat(Integer.toString(q+1)+" "+Integer.toString(j)+" - ");
//                System.out.printf("\nNumber of times found freq %d - %d\n",q+1,j);
                q++;
            }
            Arrays.fill(fq_count,0);
            empty = true;
            System.out.printf("Number of times found freqs: %s\n",fq_string);
            System.out.flush();
            fq_string = "";
            timefull = currentTimeMillis();
//            System.out.printf("full array conv and print: %d\n",timefull-timezero);
            System.out.flush();
            if (q == 11){
                System.out.println("Changed the number!!!!!!");
                receptionCount++;
            }
        }

//        System.out.println( "Fq0 Avg & Fq1 Avg & Fq2 Avg: "+ correct_0/num + " "+correct_1/num + " "+correct_2/num);

        return maxAmpFreq;
    }
}
