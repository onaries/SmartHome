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

# Database Host, ID, PW, DB Name
db_host = 'localhost'
db_id = 'root'
db_pw = 'raspberry'
db_name = 'smarthome'

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

def ampSend(amp1, amp2, amp3):
	try:
		con = mdb.connect(db_host, db_id, db_pw, db_name)
		cur = con.cursor()
		insert_sql = (
			"INSERT INTO sensor_value (power1, power2, power3) "
			"VALUES (%s, %s, %s)"
		)
		data = (amp1, amp2, amp3)

		cur.execute(insert_sql, data)
		con.commit()
	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()

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
		for i range(mul_conf.__len__):
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
					else today[3] == mul_conf[3][i].seconds // 3600:
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


# ¸ÞÀÎ ÇÔ¼ö
if __name__ == "__main__":

	logger.info('ÇÁ·Î±×·¥ ½ÇÇàµÇ¾ú½À´Ï´Ù.')

	# GCM ÃÊ±âÈ­
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# »óÅÂ°ª ÃÊ±âÈ­
	status = [0, 0, 0]
	bluetooth = serial.Serial("/dev/rfcomm0", baudrate=19200)
	logger.info('ºí·çÅõ½º ¼³Á¤ ¿Ï·á µÇ¾ú½À´Ï´Ù.')

	if len(sys.argv) == 1:
		print "Option Error"
		exit(1)
	port = int(sys.argv[1])

	# sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	sock.bind( ('', port) )
	sock.listen(10)

	print "TCP Server Waiting for client on port ", port
	logger.info("TCP ¼­¹ö ½ÇÇàµÇ¾ú½À´Ï´Ù.")

	# ºí·çÅõ½º·Î ¼ö½ÅµÈ µ¥ÀÌÅÍ ÃÊ±âÈ­
	bluetooth.flushInput()

	while True:
		
		# data, addr = sock.recvfrom(64)
		client, addr = sock.accept()
		data = client.recv(64)
		print "Connected from ", addr, " ", data
		# print "received message: ", data, " ", addr

		# Â¹Ã¶¤Ô¤¾¤É¤»Â° Â°Ã¼Â·¤Ô¤¾¤É¤¶ Â¼Â³¤Ô¤¾¤É¤²Â¤

		# Àü·ù °ª ÀÐ±â
		ampere = bluetooth.read()
		ampere.split()	# Àü·ù °ª °ø¹é(Whitespace) ·Î ±¸ºÐ

		# Ã¼Â·Ã¹Â°Âª DBÂ·¤Ô¤¾¤Ê¤§ ÃºÃ¥
		ampSend(ampere[0], ampere[1], ampere[2])

		# DBÂ¿Â¡Â¼Â­ ÂµÂ¥¤Ô¤¾¤Ê¤¥¤Ô¤¾¤É¤º¤Ô¤¾¤Ê¤¦ Â¼Â³¤Ô¤¾¤É¤²Â¤Â°Âª Âº¤Ô¤¾¤Ê¤­Â·Â¯Â¿Â±Ã¢
		mul_conf_list = getMultiConf()

		# ¤Ô¤¾¤É¤¼Ã¶Ã§ Â½¤Ô¤¾¤É¤¶Â°Â£ Â±Â¸¤Ô¤¾¤É¤¼¤Ô¤¾¤Ê¤ªÂ±Ã¢
		today = getToady()

		# Â¿Â¹Â¾¤Ô¤¾¤Ë¤¢ Â±Ã¢Â´¤Ô¤¾¤É¤¾ Â¼Â³¤Ô¤¾¤É¤²Â¤
		schedMulti(mul_conf_list, today)




		# data¤Ô¤¾¤É¤¼ Â°ÂªÂ¿Â¡ ÂµÃ»Â¶Ã³ Â¸¤Ô¤¾¤Ê¤±¤Ô¤¾¤É¤»Â¼¤Ô¤¾¤É¤º¤Ô¤¾¤É¤¼ Â°Ã¼Â·¤Ô¤¾¤É¤¶ Â½¤Ô¤¾¤É¤¼¤Ô¤¾¤É¤¼¤Ô¤¾¤Ë¤¢

		# dataÂ°Â¡ 1¤Ô¤¾¤Ê¤¥Â¸Ã© 1Â¹Ã¸ Â¸¤Ô¤¾¤Ê¤±¤Ô¤¾¤É¤»Â¼¤Ô¤¾¤É¤º¤Ô¤¾¤É¤¼ ¤Ô¤¾¤É¤¸¤Ô¤¾¤Ê¤¬Â±Ã¢
		if data == '1':
			sendBluetooth('1', 0, 1, 'multi1_on')
			# bluetooth.write('1')

			# # Â¸Â¸Â¾¤Ô¤¾¤Ë¤¢ Â¸¤Ô¤¾¤Ê¤±¤Ô¤¾¤É¤»Â¼¤Ô¤¾¤É¤º¤Ô¤¾¤É¤¼Â¸Â·¤Ô¤¾¤Ê¤§ Â¹¤Ô¤¾¤Ê¤¾Âº ÂµÂ¥¤Ô¤¾¤Ê¤¥¤Ô¤¾¤É¤º¤Ô¤¾¤Ê¤¦Â°Â¡ ÂºÂ¸Â³Â½ ÂµÂ¥¤Ô¤¾¤Ê¤¥¤Ô¤¾¤É¤º¤Ô¤¾¤Ê¤¦Â¿¤Ô¤¾¤Ê¤¦ Â°Â°Â» Â°Ã¦Â¿Ã¬
			# if bluetooth.read() == data:
			# 	status1 = 1
			# 	client.send('1\n')
			# 	gcmSend('multi1_on')

		elif data == '2':
			sendBluetooth('2', 1, 1, 'multi2_on')
			# bluetooth.write('2')
			# if bluetooth.read() == data:
			# 	status2 = 1
			# 	client.send('2\n')
			# 	gcmSend('multi2_on')
		elif data == '3':
			sendBluetooth('3', 2, 1, 'multi3_on')
			# bluetooth.write('3')
			# if bluetooth.read() == data:
			# 	status3 = 1
			# 	client.send('3\n')
			# 	gcmSend('multi3_on')
		elif data == '4':
			sendBluetooth('4', 0, 0, 'multi1_off')
			# bluetooth.write('4')
			# if bluetooth.read() == data:
			# 	status1 = 0
			# 	client.send('4\n')
			# 	gcmSend('multi1_off')
		elif data == '5':
			sendBluetooth('5', 1, 0, 'multi2_off')
			# bluetooth.write('5')
			# if bluetooth.read() == data:
			# 	status2 = 0
			# 	client.send('5\n')
			# 	gcmSend('multi2_off')
		elif data == '6':
			sendBluetooth('6', 2, 0, 'multi3_off')
			# bluetooth.write('6')
			# if bluetooth.read() == data:
			# 	status3 = 0
			# 	client.send('6\n')
			# 	gcmSend('multi3_off')
		elif data == '7':
			sendBluetooth('7', 3, [1, 1, 1], 'multi_all_on' )
			# bluetooth.write('7')
			# if bluetooth.read() == data:
			# 	status1 = 1
			# 	status2 = 1
			# 	status3 = 1
			# 	client.send('7\n')
			# 	gcmSend('multi_all_on')
		elif data == '8':
			sendBluetooth('8', 3, [0, 0, 0], 'multi_all_off')
			# bluetooth.write('8')
			# if bluetooth.read() == data:
			# 	status1 = 0
			# 	status2 = 0
			# 	status3 = 0
			# 	client.send('8\n')
			# 	gcmSend('multi_all_off')
		elif data == '9':
			strStatus = status[0] + status[1] + status[2]
			print "Statue : ", strStatus
			client.send(strStatus + "\n")
		else:
			print "Error value (1 ~ 8)"


