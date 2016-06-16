# -*- coding: utf-8 -*-
import serial
import socket
import sys
import thread
import threading
import MySQLdb as mdb
import datetime
import time
import commands
import logging

from gcm import *

db_host = 'localhost'
db_id = 'root'
db_pw = 'autoset'
db_name = 'smarthome'

# server 설정
port = 12345

# Create Logging
logger = logging.getLogger('smarthome')
logger.setLevel(logging.DEBUG)

# create console handler and set level to debug
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)

# create formatter
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# add formatter to ch
ch.setFormatter(formatter)

# add ch to logger
logger.addHandler(ch)

# GCM Send Function
def gcmSend(data_id):
	try:
		con = mdb.connect(db_host, db_id, db_pw, db_name)
		cur = con.cursor()
		reg_ids = []
		select_sql = ("SELECT reg_id FROM reg_id WHERE state = 1")
		cur.execute(select_sql)
		reg_idss = cur.fetchall()
		for r in reg_idss:
			reg_ids.append(r[0])
		print reg_ids
		con.commit()

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()

	msg = {'message': data_id }
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

def getToady():
	date = time.localtime()
	return date
# date[0] = year
# date[1] = month
# date[2] = day
# date[3] = hour
# date[4] = minute
# date[5] = second
# date[6] = weekday
# date[7] = yearday
# date[8] = isdst

def getBulbConf():
	try:
		con = mdb.connect(db_host, db_id, db_pw, db_name)
		cur = con.cursor()
		select_sql = (
			"SELECT BULB_NO, WEEKDAY, START_TIME, STOP_TIME FROM bulb_conf"
			)

		# num 은 결과값의 행의 개수
		num = cur.execute(select_sql)

		select_result = cur.fetchall()

		# 관련 리스트 초기화
		bulb_no = []
		weekday = []
		start_time = []
		stop_time = []

		# ¸®½ºÆ® °ª ÇÒ´ç
		for i in range(num):
			bulb_no.append(select_result[i][0])
			weekday.append(select_result[i][1])
			start_time.append(select_result[i][2])
			stop_time.append(select_result[i][3])

		return bulb_no, weekday, start_time, stop_time

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()


# 스케쥴
def schedBulb(bulb_conf, today):
	bulb_group = [1]

	for b in bulb_group:
		for i in range(bulb_conf[0].__len__):
			if bulb_conf[0][i] == b:
				# 요일이 일치하면
				if today[6] == bulb_conf[1][i]:
					# 시간이 일치하면
					# START TIME
					if today[3] == bulb_conf[2][i].seconds // 3600:
						# 분이 일치하면
						if today[4] == (bulb_conf[2][i].seconds % 3600) // 60:
							# 0초일 경우에만 실행
							if today[5] == 0:
								# 실행 함수
								if m == 1:
									sendBluetooth('1', m-1, 1, 'light1_on', 1)

						#시간이 일치할 경우
						elif today[4] == (bulb_conf[3][i].seconds % 3600) // 60:
							# 0초일 경우에만 실행
							if today[5] == 0:
								if m == 1:
									sendBluetooth('2', m-1, 0, 'light1_off', 1)

					# 시간이 일치하면
					# STOP TIME
					elif today[3] == bulb_conf[3][i].seconds // 3600:
						# 분이 일치하면
						if today[4] == (bulb_conf[3][i].seconds % 3600) // 60:
							# 0초일 경우에만 실행
							if today[5] == 0:
								if m == 1:
									sendBluetooth('2', m-1, 0, 'light1_off', 1)

						# 시간이 일치할 경우
						elif today[4] == (bulb_conf[2][i].seconds % 3600) // 60:
							# 0초일 경우에만 실행
							if today[5] == 0:
								# 실행 함수
								if m == 1:
									sendBluetooth('1', m-1, 1, 'light1_on', 1)

# 실제로 노드에 블루투스로 데이터를 전송하는 함수
def sendBluetooth(data, status_num, status_val, gcm_msg):
	bluetooth.write(data)

	# bluetooth.read() ÇÔ¼ö¸¦ ÅëÇØ °ªÀ» ÀÐÀ½


	# ÀüÃ¼ ÄÑ±â, ÀüÃ¼ ²ô±âÀÇ °æ¿ì ¸®½ºÆ® ÀüÃ¼¸¦ ¹Ù²Û´Ù.
	if status_num == 3:
		status = status_num

	# 그외의 경우
	else:
		status[status_num] = status_val

	# TCP·Î ÀÀ´ä
	client.send(data, '\n')

	# 예약에 의해 켜진 경우라면
	if reserved == 1:
		if status_num == 1:
			if status_val == 1:
				logger.info("전등 1이 예약에 의해 켜졌습니다")
			elif status_val == 0:
				logger.info("전등 1이 예약에 의해 꺼졌습니다")

	else:
		if status_num == 1:
			if status_val == 1:
				logger.info("전등 1이 켜졌습니다")
			elif status_val == 0:
				logger.info("전등 1이 꺼졌습니다")

	# ÈÞ´ëÆùÀ¸·Î ¾Ë¸²
	gcmSend(gcm_msg)

def updateBulbState(bulb_no, state):
	try:
		con = mdb.connect(db_host, db_id, db_pw, db_name)
		cur = con.cursor()
		update_sql = (
			"UPDATE bulb SET state = %s WHERE bulb_no = %s"
		)
		# num 은 결과값의 행의 개수
		data = (state, bulb_no)
		num = cur.execute(update_sql, (state, bulb_no))

		con.commit()

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()

if __name__ == "__main__":

	logger.info('프로그램 실행되었습니다.')

	# GCM ÃÊ±âÈ­
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# »óÅÂ°ª ÃÊ±âÈ­
	status = [0]
	bluetooth = serial.Serial("/dev/rfcomm1", baudrate=115200)
	logger.info('블루투스 설정 완료 되었습니다.')

	# if len(sys.argv) == 1:
	# 	print "Option Error, Default Port : 12345"
	# 	exit(1)
	# port = int(sys.argv[1])

	# sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	sock.bind( ('', port) )
	sock.listen(10)


	print "TCP Server Waiting for client on port ", port
	logger.info("TCP 서버 실행되었습니다.")

	# ºí·çÅõ½º·Î ¼ö½ÅµÈ µ¥ÀÌÅÍ ÃÊ±âÈ­
	bluetooth.flushInput()



	while True:

		# data, addr = sock.recvfrom(64)
		client, addr = sock.accept()
		data = client.recv(64)
		print "Connected from ", addr, " ", data

		bulb_conf_list = getBulbConf()

		today = getToday()

		schedBulb(bulb_conf_list, today)

		if data == '1':
			sendBluetooth(data, 0, 1, 'light1_on', 0)
			updateBulbState(1, 1)

		elif data == '2':
			sendBluetooth(data, 0, 0, 'light1_off', 0)
			updateBulbState(1, 0)

		# 현재 정보 데이터 전송
		elif data == '3':
			strStatus = str(status[0])
			print "Statue : ", strStatus
			client.send(strStatus + "\n")
			logger.info("현재 상태 정보가 전송되었습니다")

		else:
			print "Error value (1 ~ 3)"
			logger.error("할당되지 않는 번호입니다")


