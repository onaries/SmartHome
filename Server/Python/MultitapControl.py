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

		# num �� ������� ���� ����
		num = cur.execute(select_sql)

		select_result = cur.fetchall()

		# ���� ����Ʈ �ʱ�ȭ
		relay_no = []
		weekday = []
		start_time = []
		stop_time = []

		# ����Ʈ �� �Ҵ�
		for i in range(num):
			relay_no.append(select_result[i][0])
			weekday.append(select_result[i][1])
			start_time.append(select_result[i][2])
			stop_time.append(select_result[i][3])

		return relay_no, weekday, start_time, stop_time

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()


# ������
def schedMulti(mul_conf, today):
	mul_group = [1, 2, 3]

	for m in mul_group:
		for i range(mul_conf.__len__):
			if mul_conf[0][i] == mul_group:
				# ������ ��ġ�ϸ�
				if today[6] == mul_conf[1][i]:
					# �ð��� ��ġ�ϸ�
					# START TIME
					if today[3] == mul_conf[2][i].seconds // 3600:
						# ���� ��ġ�ϸ�
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							# ���� �Լ�
							if m == 1:
								sendBluetooth('1', m-1, 1, 'multi1_on')
							elif m == 2:
								sendBluetooth('2', m-1, 1, 'multi2_on')
							elif m == 3:
								sendBluetooth('3', m-1, 1, 'multi3_on')

					# �ð��� ��ġ�ϸ�
					# STOP TIME
					else today[3] == mul_conf[3][i].seconds // 3600:
						# ���� ��ġ�ϸ� 
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							if m == 1:
								sendBluetooth('4', m-1, 0, 'multi1_off')
							elif m == 2:
								sendBluetooth('5', m-1, 0, 'multi2_off')
							elif m == 3:
								sendBluetooth('6', m-1, 0, 'multi3_off')

# ������ ��忡 ��������� �����͸� �����ϴ� �Լ�
def sendBluetooth(data, status_num, status_val, gcm_msg):
	bluetooth.write(data)

	# bluetooth.read() �Լ��� ���� ���� ����
	if bluetooth.read() == data:

		# ��ü �ѱ�, ��ü ������ ��� ����Ʈ ��ü�� �ٲ۴�.
		if status_num == 3:
			status = status_num

		# �׿��� ���
		else:
			status[status_num] = status_val

		# TCP�� ����
		client.send(data, '\n')

		# �޴������� �˸�
		gcmSend(gcm_msg)


# ���� �Լ�
if __name__ == "__main__":

	logger.info('���α׷� ����Ǿ����ϴ�.')

	# GCM �ʱ�ȭ
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# ���°� �ʱ�ȭ
	status = [0, 0, 0]
	bluetooth = serial.Serial("/dev/rfcomm0", baudrate=19200)
	logger.info('������� ���� �Ϸ� �Ǿ����ϴ�.')

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
	logger.info("TCP ���� ����Ǿ����ϴ�.")

	# ��������� ���ŵ� ������ �ʱ�ȭ
	bluetooth.flushInput()

	while True:
		
		# data, addr = sock.recvfrom(64)
		client, addr = sock.accept()
		data = client.recv(64)
		print "Connected from ", addr, " ", data
		# print "received message: ", data, " ", addr

		# ¹ö�Ԥ��ɤ�° °ü·�Ԥ��ɤ� ¼³�Ԥ��ɤ�¤

		# ���� �� �б�
		ampere = bluetooth.read()
		ampere.split()	# ���� �� ����(Whitespace) �� ����

		# ü·ù°ª DB·�Ԥ��ʤ� úå
		ampSend(ampere[0], ampere[1], ampere[2])

		# DB¿¡¼­ µ¥�Ԥ��ʤ��Ԥ��ɤ��Ԥ��ʤ� ¼³�Ԥ��ɤ�¤°ª º�Ԥ��ʤ�·¯¿±â
		mul_conf_list = getMultiConf()

		# �Ԥ��ɤ�öç ½�Ԥ��ɤ�°£ ±¸�Ԥ��ɤ��Ԥ��ʤ�±â
		today = getToady()

		# ¿¹¾�Ԥ��ˤ� ±â´�Ԥ��ɤ� ¼³�Ԥ��ɤ�¤
		schedMulti(mul_conf_list, today)




		# data�Ԥ��ɤ� °ª¿¡ µû¶ó ¸�Ԥ��ʤ��Ԥ��ɤ�¼�Ԥ��ɤ��Ԥ��ɤ� °ü·�Ԥ��ɤ� ½�Ԥ��ɤ��Ԥ��ɤ��Ԥ��ˤ�

		# data°¡ 1�Ԥ��ʤ�¸é 1¹ø ¸�Ԥ��ʤ��Ԥ��ɤ�¼�Ԥ��ɤ��Ԥ��ɤ� �Ԥ��ɤ��Ԥ��ʤ�±â
		if data == '1':
			sendBluetooth('1', 0, 1, 'multi1_on')
			# bluetooth.write('1')

			# # ¸¸¾�Ԥ��ˤ� ¸�Ԥ��ʤ��Ԥ��ɤ�¼�Ԥ��ɤ��Ԥ��ɤ�¸·�Ԥ��ʤ� ¹�Ԥ��ʤ�º µ¥�Ԥ��ʤ��Ԥ��ɤ��Ԥ��ʤ�°¡ º¸³½ µ¥�Ԥ��ʤ��Ԥ��ɤ��Ԥ��ʤ�¿�Ԥ��ʤ� °°» °æ¿ì
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


