# -*- coding: utf-8 -*-
import plotly.plotly as py
from plotly.graph_objs import Scatter, Layout, Figure
import spidev
import time
import os
import sys
import Adafruit_DHT
import MySQLdb as mdb
import datetime
from gcm import *

# Plotly 설정
username = 'onaries'
api_key = 'd7p6vxkzks'
stream_token1 = '884y57pb97'
stream_token2 = 'esexv1gelf'
stream_token3 = '4xegaeps7j'
stream_token4 = '9hlkyacq2s'
DEBUG = True

# GCM 관련 설정
gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

py.sign_in(username, api_key)

# trace1
trace1 = Scatter(x=[], y=[], stream=dict(token=stream_token1))
layout1 = Layout(title = 'Temp')
fig1 = Figure(data=[trace1], layout = layout1)
py.plot(fig1, filename='RPI Temp')
stream1 = py.Stream(stream_token1)
stream1.open()

# trace2
trace2 = Scatter(x=[], y=[], stream=dict(token=stream_token2))
layout2 = Layout(title = "Humi")
fig2 = Figure(data=[trace2], layout = layout2)
py.plot(fig2, filename="RPI Humi")
stream2 = py.Stream(stream_token2)
stream2.open()

# trace3
trace3 = Scatter(x=[], y=[], stream=dict(token=stream_token3))
layout3 = Layout(title = "Gas")
fig3 = Figure(data=[trace3], layout = layout3)
py.plot(fig3, filename="RPI Gas")
stream3 = py.Stream(stream_token3)
stream3.open()

# trace4
trace4 = Scatter(x=[], y=[], stream=dict(token=stream_token4))
layout4 = Layout(title = "Photo")
fig4 = Figure(data=[trace4], layout = layout4)
py.plot(fig4, filename="RPI Photo")
stream4 = py.Stream(stream_token4)
stream4.open()

# 센서, 핀 번호
sensor = 22
pin = 18

# spi 통신
spi = spidev.SpiDev()
spi.open(0, 0)
 
def ReadAdc(chNum):
	if chNum > 7 or chNum < 0:
		return -1
	adc = spi.xfer2([1, (8+chNum) << 4, 0])
	data = ((adc[1] & 3) << 8) + adc[2]
	return data
 
def ConvertVolt(data, places):
	volt = (data * 3.3) / float(1023)
	volt = round(volt, places)
	return volt

def GcmSend(reg_ids, msg_id):
	msg = {'message': msg_id}
	response = gcm.json_request(registration_ids=reg_ids, data=msg)

	if response and 'success' in response:
		 for reg_id, success_id in response['success'].items():
			 print 'SUCCESS for reg_id %s' % reg_id

	# Handling errors
	if 'errors' in response:
		for error, reg_ids in response['errors'].items():
			# Check for errors and act accordingly
			if error in ['NotRegistered', 'InvalidRegistration']:
				# Remove reg_ids from database
				for reg_id in reg_ids:
					entity.filter(registration_id=reg_id).delete()

	if 'canonical' in response:
		for reg_id, canonical_id in response['canonical'].items():
			# Repace reg_id with canonical_id in your database
			entry = entity.filter(registration_id=reg_id)
			entry.registration_id = canonical_id
			entry.save()

# 온도 습도 
humi, temp = Adafruit_DHT.read_retry(sensor, pin)
prev_temp = temp

# ADC 채널
gas_channel = 0
photo_channel = 1
 
while True:
	
	tic = time.clock()

	# 온도 습도 
	humi, temp = Adafruit_DHT.read_retry(sensor, pin)
	
	# 가스 값
	gas_level = ReadAdc(gas_channel)
	photo_level = 1024 - ReadAdc(photo_channel)

	# 알림 설정 변수 초기화
	# pState = ()
	gas_high = 300
	humi_high = 80
	humi_low = 30
	temp_high = 30
	temp_low = 10
	update_time = 10

	try:
		if humi is not None and temp is not None:
			if abs(temp - prev_temp) <= 10:
				print 'Temp : {0:0.1f} C Humi : {1:0.1f} % Gas : {2:} Photo : {3:}'.format(temp, humi, gas_level, photo_level)
				stream1.write({'x':datetime.datetime.now().strftime("%Y-%m-%d %H:%M"), 'y':temp})
				stream2.write({'x':datetime.datetime.now().strftime("%Y-%m-%d %H:%M"), 'y':humi})
				stream3.write({'x':datetime.datetime.now().strftime("%Y-%m-%d %H:%M"), 'y':gas_level})
				stream4.write({'x':datetime.datetime.now().strftime("%Y-%m-%d %H:%M"), 'y':photo_level})
			prev_temp = temp
		else:
			print 'Failed to get reading. Try again!'
			sys.exit(1)
	except:
		pass
	# DB 연결 및 sql 구문
	try:
		con = mdb.connect('localhost', 'root', 'raspberry1', 'sensor')
		cur = con.cursor()
		reg_ids = []
		insert_sql = (
			"INSERT INTO data (temp, humi, gas, photo) "
			"VALUES (%s, %s, %s, %s)"
		)
		select_sql = ("SELECT value FROM state")
		select_sql2 = ("SELECT reg_id FROM reg_id WHERE state = 1")
		data = (temp, humi, gas_level, photo_level)
		
		cur.execute(insert_sql, data)
		cur.execute(select_sql)
		pState = cur.fetchall()
		gcm_state = pState[0][0]
		gas_high = pState[1][0]
		humi_high = pState[2][0]
		humi_low = pState[3][0]
		temp_high = pState[4][0]
		temp_low = pState[5][0]
		update_time = pState[6][0]

		cur.execute(select_sql2)
		reg_idss = cur.fetchall()
		for r in reg_idss:
			reg_ids.append(r[0])
		con.commit()

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()
		
	# 알림 기능이 켜져 있을 경우

	if gas_level >= gas_high:
		GcmSend(reg_ids, 'gas')
		
	if humi >= humi_high:
		GcmSend(reg_ids, 'humi_high')
		
	elif humi <= humi_low:
		GcmSend(reg_ids, 'humi_low')

	if temp >= temp_high:
		GcmSend(reg_ids, 'temp_high')

	elif temp <= temp_low:
		GcmSend(reg_ids, 'temp_low')


	# print("GAS : Data {} ({}V)".format(gas_level, gas_volt))
	
	# Time 계산
	delay = update_time * 60

	toc = time.clock()
	time_delay = toc - tic
	print time_delay

	time.sleep(delay - time_delay)