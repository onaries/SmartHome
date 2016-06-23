# -*- coding: utf-8 -*-
import serial
import socket
import sys
import thread
import threading
import time

bluetooth = serial.Serial("/dev/rfcomm1", baudrate=19200)

while True:
	print bluetooth.read()
	
