
#include <gst/app/gstappsink.h>
#include <gst/gst.h>
#include <gst/video/video.h>
#include <cstring>
#include <stdio.h>
#include <set>
//sleep thread
#include <chrono>
#include <thread>
//measure time
#include <iostream>
#include <ctime>

using namespace std;

class GstFileSigGen
{
public:

  GstFileSigGen (gchar *filepath);
  ~GstFileSigGen ();
  void start ();
  void stop ();
  void setFreq (const string &value);
  void setVolume (const string &value);
  struct
  {                               // audio pipeline
    GstElement *src;              // file Src format
    GstElement *decoder;          // Audio decoder format
    GstElement *convert;          // convert audio format
    GstElement *tee;              // splits pipeline
    GstElement *audioQueue;       // links audio pipeline side
    GstElement *audioLevel;       // audio sound level    
    GstElement *audioSink;        // audio sink

    gchar filelocation[256];      // file location string 

    GstPad *teeAudioPad;          // tee audio pad (output)
    GstPad *queueAudioPad;        // queue audio pad (input)
  } _audio;
  GstElement *_pipeline;          // pipeline
  int _sample_flag;               // true if new sample is available
  GstAppSinkCallbacks _callbacks; // video app-sink callback data

  struct
  {
    double location;       // file path
    double volume = 1;     // sound level
  } _prop;

  // GStreamer callbacks.
  static gboolean cb_Bus (GstBus *, GstMessage *, GstFileSigGen *);
};