#include "gst-transmission-file.h"

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

GstFileSigGen::GstFileSigGen (char *filepath)
{ // gst-launch-1.0 filesrc location=test.wav ! wavparse ! audioconvert !
  // volume volume=1  ! autoaudiosink
  GstBus *bus;
  gulong ret;

  _pipeline = nullptr;
  _audio.src = nullptr;
  _audio.decoder = nullptr;
  _audio.convert = nullptr;
  _audio.tee = nullptr;
  _audio.audioQueue = nullptr;
  _audio.audioLevel = nullptr;
  _audio.audioSink = nullptr;

  strcpy (_audio.filelocation, filepath);
  g_assert_nonnull (_audio.filelocation);

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
  _audio.src = gst_element_factory_make ("filesrc", "filesource");
  g_assert_nonnull (_audio.src);
  _audio.decoder = gst_element_factory_make ("wavparse", "audiowavdecoder");
  g_assert_nonnull (_audio.decoder);
  _audio.convert
      = gst_element_factory_make ("audioconvert", "audioconvert");
  g_assert_nonnull (_audio.convert);
  _audio.tee = gst_element_factory_make ("tee", "teesplit");
  g_assert_nonnull (_audio.tee);

  // Audio thread
  _audio.audioQueue = gst_element_factory_make ("queue", "audioqueue");
  g_assert_nonnull (_audio.audioQueue);
  _audio.audioLevel = gst_element_factory_make ("volume", "audiolevel");
  g_assert_nonnull (_audio.audioLevel);
  // Try to use ALSA if available.
  _audio.audioSink = gst_element_factory_make ("alsasink", "audio.sink");
  if (_audio.audioSink == nullptr)
    _audio.audioSink
        = gst_element_factory_make ("autoaudiosink", "audio.sink");
  g_assert_nonnull (_audio.audioSink);

  // File location set
  g_object_set (G_OBJECT (_audio.src), "location", _audio.filelocation,
                NULL);
  // Volume level set
  g_object_set (G_OBJECT (_audio.audioLevel), "volume", _prop.volume, NULL);

  // Pipeline add
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.src));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.decoder));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.convert));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.tee));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.audioQueue));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.audioLevel));
  g_assert (gst_bin_add (GST_BIN (_pipeline), _audio.audioSink));

  // Pipeline common link
  g_assert (gst_element_link (_audio.src, _audio.decoder));
  g_assert (gst_element_link (_audio.decoder, _audio.convert));
  g_assert (gst_element_link (_audio.convert, _audio.tee));

  // Pipeline audio link
  g_assert (gst_element_link (_audio.audioQueue, _audio.audioLevel));
  g_assert (gst_element_link (_audio.audioLevel, _audio.audioSink));

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

GstFileSigGen::~GstFileSigGen ()
{
}

void
GstFileSigGen::start ()
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

  ret = gst_element_set_state (_pipeline, GST_STATE_PLAYING);

  // printf ("started");
}

void
GstFileSigGen::stop ()
{
  printf ("stopping");
  gstx_element_set_state_sync (_pipeline, GST_STATE_NULL);
  gst_object_unref (_pipeline);
}

void
GstFileSigGen::setVolume (const string &value)
{
  _prop.volume = xstrtodorpercent (value, nullptr);
  g_object_set (_audio.audioLevel, "volume", _prop.volume, nullptr);
}

gboolean
GstFileSigGen::cb_Bus (GstBus *bus, GstMessage *msg, GstFileSigGen *player)
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
  GstFileSigGen player1 = GstFileSigGen ((char *) "Audio Track-.wav"),
                player2 = GstFileSigGen ((char *) "Audio Track-2.wav"),
                player3 = GstFileSigGen ((char *) "Audio Track-3.wav"),
                player4 = GstFileSigGen ((char *) "Audio Track-4.wav"),
                player5 = GstFileSigGen ((char *) "Audio Track-5.wav"),
                player6 = GstFileSigGen ((char *) "Audio Track-6.wav"),
                player7 = GstFileSigGen ((char *) "Audio Track-7.wav"),
                player8 = GstFileSigGen ((char *) "Audio Track-8.wav"),
                player9 = GstFileSigGen ((char *) "Audio Track-9.wav"),
                player10 = GstFileSigGen ((char *) "Audio Track-10.wav"),
                player11 = GstFileSigGen ((char *) "Audio Track-11.wav"),
                player12 = GstFileSigGen ((char *) "Audio Track-12.wav"),
                player13 = GstFileSigGen ((char *) "Audio Track-13.wav");

  int const size = 16;

  int number[size];

  player13.setVolume ("1");
  player13.start ();

  player1.setVolume ("0.3");
  player1.start ();

  player2.setVolume ("0.3");
  player2.start ();

  player3.setVolume ("0.3");
  player3.start ();

  player4.setVolume ("0.3");
  player4.start ();

  player5.setVolume ("0.3");
  player5.start ();

  player6.setVolume ("0.3");
  player6.start ();

  player7.setVolume ("0.3");
  player7.start ();

  player8.setVolume ("0.3");
  player8.start ();

  player9.setVolume ("0.3");
  player9.start ();

  player10.setVolume ("0.3");
  player10.start ();

  player11.setVolume ("0.3");
  player11.start ();

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

      std::this_thread::sleep_for (std::chrono::milliseconds (5));

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

      std::this_thread::sleep_for (std::chrono::nanoseconds (970));
    }

  auto end = std::chrono::system_clock::now ();
  std::chrono::duration<double, std::milli> elapsed_seconds = end - start;

  std::cout << "elapsed time: " << elapsed_seconds.count () << "ms\n";

  while (true)
    ;
}