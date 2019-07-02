all:
	g++ -g -O2 -std=gnu++11 gst-transmission.cpp `pkg-config --cflags --libs gstreamer-1.0 gstreamer-plugins-base-1.0 gstreamer-app-1.0`
	g++ -g -O2 -std=gnu++11 -o b.out gst-transmission-file.cpp `pkg-config --cflags --libs gstreamer-1.0 gstreamer-plugins-base-1.0 gstreamer-app-1.0`
clear:
	rm a.out
	rm b.out