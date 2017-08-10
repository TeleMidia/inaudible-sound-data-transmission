Author: João Victor Girard
Telemidia - Puc Rio - 09/08/2017

This is a Readme file for the android app project BgChange.
The app is based to run on a minimum sdk version of 18 (4.3) and the 
targeted version is 25 (7.1).

The purpose of the app is to recognize different frequencies of the user surroundings
with the microfone of the device. The optimal frequency to be analyzed is 19000 hz 
and the possible other frequencies are 17000, 17500 and 18500 hz. Each one of the 
frequencies have a diffrent color to show its recognition. The colors are blue, green, 
red and yellow in ascending order of frequencies and the lack of detection is related 
to white. The colors are shown in the background and is updated in real time (most of 
them).

All but 19000 hz function in the same way. If is detected higher then 2 or less then 
-2 threshold, the app is triggered and the color changes.

With 19000 hz there is a counter for 500 cycles of detection and if the frequency is 
detected at least 100 times then the color is changed in the background. There are 
text indicators in the screen exclusively for this frequency. This includes a counter 
on the left of the number of times when it has detected 19000 hz next to a number of 
all the cycles that has been computed. On the right side there is a timer for the 
detection since the first cycle until 100 detections. In the middle a start/stop 
button for the analyzer. Lastly, on the lower end there is a TextEdit area for real 
time threshold changer. The threshold itself is defined by the given valor plus the 
average of values from 17000 hz to 18990 hz.
