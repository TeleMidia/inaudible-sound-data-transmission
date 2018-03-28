#include "gst-transmission.h"

#define gstx_element_get_state(elt, st, pend, tout)                        \
  g_assert (gst_element_get_state ((elt), (st), (pend), (tout))            \
            != GST_STATE_CHANGE_FAILURE)

#define gstx_element_get_state_sync(elt, st, pend)                         \
  gstx_element_get_state ((elt), (st), (pend), GST_CLOCK_TIME_NONE)

#define gstx_element_set_state(elt, st)                                    \
  g_assert (gst_element_set_state ((elt), (st)) != GST_STATE_CHANGE_FAILURE)

#define gstx_element_set_state_sync(elt, st)                               \
  G_STMT_START                                                             \
  {                                                                        \
    gstx_element_set_state ((elt), (st));                                  \
    gstx_element_get_state_sync ((elt), nullptr, nullptr);                 \
  }                                                                        \
  G_STMT_END

gdouble
xstrtodorpercent (const string &s, bool *perc)
{
  gchar *end;
  gdouble x = g_ascii_strtod (s.c_str (), &end);
  if (*end == '%')
    {
      if (perc)
        *perc = true;
      return x / 100.;
    }
  else
    {
      if (perc)
        *perc = false;
      return x;
    }
}

// Public.

GstSigGen::GstSigGen ()
{
  GstBus *bus;
  gulong ret;

  _pipeline = nullptr;
  _audio.src = nullptr;
  _audio.convert = nullptr;
  _audio.tee = nullptr;
  _audio.audioQueue = nullptr;
  _audio.audioSink = nullptr;

  if (!gst_is_initialized ())
    {
      GError *error = nullptr;
      if (!gst_init_check (nullptr, nullptr, &error))
        {
          g_assert_nonnull (error);
          printf ("%s", error->message);
          g_error_free (error);
        }
    }

  _pipeline = gst_pipeline_new ("pipeline");
  g_assert_nonnull (_pipeline);

  bus = gst_pipeline_get_bus (GST_PIPELINE (_pipeline));
  g_assert_nonnull (bus);
  ret = gst_bus_add_watch (bus, (GstBusFunc) cb_Bus, this);
  g_assert (ret > 0);
  gst_object_unref (bus);

  // Setup audio pipeline.
  _audio.src = gst_element_factory_make ("audiotestsrc", "audio.src");
  g_assert_nonnull (_audio.src);
  _audio.convert
      = gst_element_factory_make ("audioconvert", "audioconvert");
  g_assert_nonnull (_audio.convert);
  _audio.tee = gst_element_factory_make ("tee", "teesplit");
  g_assert_nonnull (_audio.tee);

  // Audio thread
  _audio.audioQueue = gst_element_factory_make ("queue", "audioqueue");
  g_assert_nonnull (_audio.audioQueue);
  // Try to use ALSA if available.
  _audio.audioSink = gst_element_factory_make ("alsasink", "audio.sink");
  if (_audio.audioSink == nullptr)
    _audio.audioSink
        = gst_element_factory_make ("autoaudiosink", "audio.sink");
  g_assert_nonnull (_audio.audioSink);

  // Pipeline add
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.src));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.convert));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.tee));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.audioQueue));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.audioSink));

  // Pipeline common link
  g_assert (gst_element_link (_audio.src, _audio.convert));
  g_assert (gst_element_link (_audio.convert, _audio.tee));

  // Pipeline audio link
  g_assert (gst_element_link (_audio.audioQueue, _audio.audioSink));

  // Audio pad linking
  _audio.teeAudioPad = gst_element_get_request_pad (_audio.tee, "src_%u");
  g_assert_nonnull (_audio.teeAudioPad);
  _audio.queueAudioPad
      = gst_element_get_static_pad (_audio.audioQueue, "sink");
  g_assert_nonnull (_audio.queueAudioPad);

  if (gst_pad_link (_audio.teeAudioPad, _audio.queueAudioPad)
      != GST_PAD_LINK_OK)
    {
      printf ("Tee and audio queue not linked");
    }

  gst_object_unref (_audio.queueAudioPad);

  // Callbacks.
  _callbacks.new_preroll = nullptr;
}

GstSigGen::~GstSigGen ()
{
}

void
GstSigGen::start ()
{
  GstCaps *caps;
  GstStructure *st;
  GstStateChangeReturn ret;

  // printf ("starting");

  st = gst_structure_new_empty ("audio/x-raw");
  gst_structure_set (st, "format", G_TYPE_STRING, "BGRA", nullptr);

  caps = gst_caps_new_full (st, nullptr);
  g_assert_nonnull (caps);

  g_atomic_int_set (&_sample_flag, 0);

  // Initialize properties.
  g_object_set (_audio.src, "freq", _prop.freq, "volume", _prop.volume,
                nullptr);

  ret = gst_element_set_state (_pipeline, GST_STATE_PLAYING);

  // printf ("started");
}

void
GstSigGen::stop ()
{
  printf ("stopping");
  gstx_element_set_state_sync (_pipeline, GST_STATE_NULL);
  gst_object_unref (_pipeline);
}

void
GstSigGen::setFreq (const string &value)
{
  _prop.freq = xstrtodorpercent (value, nullptr);
  g_object_set (_audio.src, "freq", _prop.freq, nullptr);
}

void
GstSigGen::setVolume (const string &value)
{

  _prop.volume = xstrtodorpercent (value, nullptr);
  g_object_set (_audio.src, "volume", _prop.volume, nullptr);
}

gboolean
GstSigGen::cb_Bus (GstBus *bus, GstMessage *msg, GstSigGen *player)
{
  g_assert_nonnull (bus);
  g_assert_nonnull (msg);
  g_assert_nonnull (player);

  switch (GST_MESSAGE_TYPE (msg))
    {
    case GST_MESSAGE_EOS:
      {
        printf ("EOS");
        break;
      }
    case GST_MESSAGE_ERROR:
    case GST_MESSAGE_WARNING:
      {
        GstObject *obj = nullptr;
        GError *error = nullptr;

        obj = GST_MESSAGE_SRC (msg);
        g_assert_nonnull (obj);

        if (GST_MESSAGE_TYPE (msg) == GST_MESSAGE_ERROR)
          {
            gst_message_parse_error (msg, &error, nullptr);
            g_assert_nonnull (error);
            printf ("%s", error->message);
          }
        else
          {
            gst_message_parse_warning (msg, &error, nullptr);
            g_assert_nonnull (error);
            printf ("%s", error->message);
          }
        g_error_free (error);
        break;
      }
    default:
      break;
    }
  return TRUE;
}

void
bin (unsigned short v, int number[], int size)
{
  for (; size - 1 >= 0; size--)
    number[size - 1] = (v >> (size - 1)) & 1;
}

int
main ()
{
  GstSigGen player1, player2, player3, player4, player5, player6, player7,
      player8, player9, player10, player11, player12, player13;

  int const size = 16;

  int number[size];

  player13.setFreq ("19500");
  player13.setVolume ("0.5");
  player13.start ();

  player1.setFreq ("20000");
  player1.setVolume ("0.3");
  player1.start ();

  player2.setFreq ("20150");
  player2.setVolume ("0.3");
  player2.start ();

  player3.setFreq ("20300");
  player3.setVolume ("0.3");
  player3.start ();

  player4.setFreq ("20450");
  player4.setVolume ("0.3");
  player4.start ();

  player5.setFreq ("20600");
  player5.setVolume ("0.3");
  player5.start ();

  player6.setFreq ("20750");
  player6.setVolume ("0.3");
  player6.start ();

  player7.setFreq ("20900");
  player7.setVolume ("0.3");
  player7.start ();

  player8.setFreq ("21050");
  player8.setVolume ("0.3");
  player8.start ();

  player9.setFreq ("21200");
  player9.setVolume ("0.3");
  player9.start ();

  player10.setFreq ("21350");
  player10.setVolume ("0.3");
  player10.start ();

  player11.setFreq ("21500");
  player11.setVolume ("0.3");
  player11.start ();

  player12.setFreq ("21650");
  player12.setVolume ("0.3");
  player12.start ();

  auto start = std::chrono::system_clock::now ();

  short dataToSend[]
      = { 1, 7, 5, 4, 19, 1 }; // highest number should be 2^13-1 = 4095

  for (int t = 0; t < size; t++)
    {
      number[t] = 0;
    }

  for (int i = 0; i < sizeof (dataToSend) / sizeof (dataToSend[0]); i++)
    {
      printf ("number: %d\n", dataToSend[i]);
      bin (dataToSend[i], number, size);

      for (int t = 0; t < size; t++)
        {
          printf ("%d", number[t]);
        }

      printf ("\n");

      if (number[0])
        player1.setVolume ("0.5");
      else
        player1.setVolume ("0.3");
      if (number[1])
        player2.setVolume ("0.5");
      else
        player2.setVolume ("0.3");
      if (number[2])
        player3.setVolume ("0.5");
      else
        player3.setVolume ("0.3");
      if (number[3])
        player4.setVolume ("0.5");
      else
        player4.setVolume ("0.3");
      if (number[4])
        player5.setVolume ("0.5");
      else
        player5.setVolume ("0.3");
      if (number[5])
        player6.setVolume ("0.5");
      else
        player6.setVolume ("0.3");
      if (number[6])
        player7.setVolume ("0.5");
      else
        player7.setVolume ("0.3");
      if (number[7])
        player8.setVolume ("0.5");
      else
        player8.setVolume ("0.3");
      if (number[8])
        player9.setVolume ("0.5");
      else
        player9.setVolume ("0.3");
      if (number[9])
        player10.setVolume ("0.5");
      else
        player10.setVolume ("0.3");
      if (number[10])
        player11.setVolume ("0.5");
      else
        player11.setVolume ("0.3");
      if (number[11])
        player12.setVolume ("0.5");
      else
        player12.setVolume ("0.3");

      std::this_thread::sleep_for(std::chrono::milliseconds(5));

      for (int t = 0; t < size; t++)
        {
          number[t] = 0;
        }

      player1.setVolume ("0.3");
      player2.setVolume ("0.3");
      player3.setVolume ("0.3");
      player4.setVolume ("0.3");
      player5.setVolume ("0.3");
      player6.setVolume ("0.3");
      player7.setVolume ("0.3");
      player8.setVolume ("0.3");
      player9.setVolume ("0.3");
      player10.setVolume ("0.3");
      player11.setVolume ("0.3");
      player12.setVolume ("0.3");

      std::this_thread::sleep_for(std::chrono::nanoseconds(970));
    }

  auto end = std::chrono::system_clock::now ();
  std::chrono::duration<double, std::milli> elapsed_seconds = end - start;

  std::cout << "elapsed time: " << elapsed_seconds.count () << "ms\n";

  while (true)
    ;
}