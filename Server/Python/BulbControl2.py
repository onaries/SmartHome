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


def getMultiConf():
	try:
		con = mdb.connect(db_host, db_id, db_pw, db_name)
		cur = con.cursor()
		select_sql = (
			"SELECT RELAY_NO, WEEKDAY, START_TIME, STOP_TIME FROM relay_conf"
			)

		# num Àº °á°ú°ªÀÇ ÇàÀÇ °³¼ö
		num = cur.execute(select_sql)

		select_result = cur.fetchall()

		# °ü·Ã ¸®½ºÆ® ÃÊ±âÈ­
		relay_no = []
		weekday = []
		start_time = []
		stop_time = []

		# ¸®½ºÆ® °ª ÇÒ´ç
		for i in range(num):
			relay_no.append(select_result[i][0])
			weekday.append(select_result[i][1])
			start_time.append(select_result[i][2])
			stop_time.append(select_result[i][3])

		return relay_no, weekday, start_time, stop_time

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()



# ½ºÄÉÁì
def schedMulti(mul_conf, today):
	mul_group = [1, 2, 3]

	for m in mul_group:
		for i in range(mul_conf.__len__):
			if mul_conf[0][i] == mul_group:
				# ¿äÀÏÀÌ ÀÏÄ¡ÇÏ¸é
				if today[6] == mul_conf[1][i]:
					# ½Ã°£ÀÌ ÀÏÄ¡ÇÏ¸é
					# START TIME
					if today[3] == mul_conf[2][i].seconds // 3600:
						# ºÐÀÌ ÀÏÄ¡ÇÏ¸é
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							# ½ÇÇà ÇÔ¼ö
							if m == 1:
								sendBluetooth('1', m-1, 1, 'multi1_on')
							elif m == 2:
								sendBluetooth('2', m-1, 1, 'multi2_on')
							elif m == 3:
								sendBluetooth('3', m-1, 1, 'multi3_on')

					# ½Ã°£ÀÌ ÀÏÄ¡ÇÏ¸é
					# STOP TIME
					elif today[3] == mul_conf[3][i].seconds // 3600:
						# ºÐÀÌ ÀÏÄ¡ÇÏ¸é 
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							if m == 1:
								sendBluetooth('4', m-1, 0, 'multi1_off')
							elif m == 2:
								sendBluetooth('5', m-1, 0, 'multi2_off')
							elif m == 3:
								sendBluetooth('6', m-1, 0, 'multi3_off')

# ½ÇÁ¦·Î ³ëµå¿¡ ºí·çÅõ½º·Î µ¥ÀÌÅÍ¸¦ Àü¼ÛÇÏ´Â ÇÔ¼ö
def sendBluetooth(data, status_num, status_val, gcm_msg):
	bluetooth.write(data)

	# bluetooth.read() ÇÔ¼ö¸¦ ÅëÇØ °ªÀ» ÀÐÀ½
	if bluetooth.read() == data:

		# ÀüÃ¼ ÄÑ±â, ÀüÃ¼ ²ô±âÀÇ °æ¿ì ¸®½ºÆ® ÀüÃ¼¸¦ ¹Ù²Û´Ù.
		if status_num == 3:
			status = status_num

		# ±×¿ÜÀÇ °æ¿ì
		else:
			status[status_num] = status_val

		# TCP·Î ÀÀ´ä
		client.send(data, '\n')

		# ÈÞ´ëÆùÀ¸·Î ¾Ë¸²
		gcmSend(gcm_msg)


if __name__ == "__main__":

	logger.info('프로그램 실행되었습니다.')

	# GCM ÃÊ±âÈ­
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# »óÅÂ°ª ÃÊ±âÈ­
	status = [0, 0, 0]
	bluetooth = serial.Serial("/dev/rfcomm1", baudrate=19200)
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

		if data == '1':
			#sendBluetooth(data, 0, 1, 'light1_on')
			bluetooth.write('1')
			if bluetooth.read() == data:
			 	status1 = 0
			 	client.send('1\n')
			 	gcmSend('multi1_on')
			logger.info("전등 1이 켜졌습니다")

		elif data == '2':
			#sendBluetooth(data, 0, 0, 'light1_off')
			bluetooth.write('2')
			if bluetooth.read() == data:
			 	status1 = 0
			 	client.send('2\n')
			 	gcmSend('multi1_off')
			logger.info("전등 1이 꺼졌습니다")

		# 현재 정보 데이터 전송
		elif data == '3':
			strStatus = str(status[0])
			print "Statue : ", strStatus
			client.send(strStatus + "\n")
			logger.info("현재 상태 정보가 전송되었습니다")

		else:
			print "Error value (1 ~ 3)"
			logger.error("할당되지 않는 번호입니다")


