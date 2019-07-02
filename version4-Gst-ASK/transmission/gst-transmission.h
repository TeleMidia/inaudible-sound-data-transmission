
#include <gst/app/gstappsink.h>
#include <gst/gst.h>
#include <gst/video/video.h>
#include <string>
#include <stdio.h>
#include <set>
//sleep thread
#include <chrono>
#include <thread>
//measure time
#include <iostream>
#include <ctime>
//transmission matrix
#include <math.h>
#include <stdlib.h> 
#include <stdlib.h>

using namespace std;

class GstSigGen
{
public:
  GstSigGen ();
  ~GstSigGen ();
  void start ();
  void stop ();
  void setFreq (const string &value);
  void setVolume (const string &value);
  struct
  {                         // audio pipeline
    GstElement *src;        // Audio Test Src format
    GstElement *convert;    // convert audio format
    GstElement *tee;        // splits pipeline
    GstElement *audioQueue; // links audio pipeline side
    GstElement *audioSink;  // audio sink

    GstPad *teeAudioPad;   // tee audio pad (output)
    GstPad *queueAudioPad; // queue audio pad (input)
  } _audio;
  GstElement *_pipeline;          // pipeline
  int _sample_flag;               // true if new sample is available
  GstAppSinkCallbacks _callbacks; // video app-sink callback data

  struct
  {
    double freq;   // frequency
    double volume; // sound level
  } _prop;

  // GStreamer callbacks.
  static gboolean cb_Bus (GstBus *, GstMessage *, GstSigGen *);
};

int main_message();