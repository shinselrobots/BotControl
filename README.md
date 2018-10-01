# BotControl
Android application (client) to control a robot via bluetooth.
There is a matching Arduino application (server) on the robot to receive the commands

## Hardware configuration / Overview
Android phone --> bluetooth --> Arduino --> USB Serial --> ROS on Ubuntu 
I use an Arduino with Sparkfun BlueSMiRF gold module


## Software dependencies:
The Android client talks to the Arduino server via "Amarino" library,
which has a Android and an Arduino component
  http://www.amarino-toolkit.net/


## Android Studio Project
The code in this repo is built and installed on the phone using the Arduino Studio:  
  https://developer.android.com/studio/

The code is normally placed in:  
  ~/StudioProjects/Botcontrol


## Use Case:
see the Sheldon Robot project for an example of how this is used to control a ROS robot.  In particular, see the sheldon_arduino directory in:  
  https://github.com/shinselrobots/sheldon

