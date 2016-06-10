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
db_pw = 'autoset'
db_name = 'smarthome'

# server 설정
port = 12346

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

		# num 은 결과값의 행의 개수
		num = cur.execute(select_sql)

		select_result = cur.fetchall()

		# 관련 리스트 초기화
		relay_no = []
		weekday = []
		start_time = []
		stop_time = []

		# 리스트 값 할당
		for i in range(num):
			relay_no.append(select_result[i][0])
			weekday.append(select_result[i][1])
			start_time.append(select_result[i][2])
			stop_time.append(select_result[i][3])

		return relay_no, weekday, start_time, stop_time

	except mdb.Error as e:
		print 'SQL error %d: %s' % (e.args[0], e.args[1])
		con.rollback()


# 스케쥴
def schedMulti(mul_conf, today):
	mul_group = [1, 2, 3]

	for m in mul_group:
		for i in range(mul_conf.__len__):
			if mul_conf[0][i] == mul_group:
				# 요일이 일치하면
				if today[6] == mul_conf[1][i]:
					# 시간이 일치하면
					# START TIME
					if today[3] == mul_conf[2][i].seconds // 3600:
						# 분이 일치하면
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							# 실행 함수
							if m == 1:
								sendBluetooth('1', m-1, 1, 'multi1_on')
							elif m == 2:
								sendBluetooth('2', m-1, 1, 'multi2_on')
							elif m == 3:
								sendBluetooth('3', m-1, 1, 'multi3_on')

					# 시간이 일치하면
					# STOP TIME
					elif today[3] == mul_conf[3][i].seconds // 3600:
						# 분이 일치하면 
						if today[4] == (mul_conf[2][i].seconds % 3600) // 60:
							if m == 1:
								sendBluetooth('4', m-1, 0, 'multi1_off')
							elif m == 2:
								sendBluetooth('5', m-1, 0, 'multi2_off')
							elif m == 3:
								sendBluetooth('6', m-1, 0, 'multi3_off')

# 실제로 노드에 블루투스로 데이터를 전송하는 함수
def sendBluetooth(data, status_num, status_val, gcm_msg):
	bluetooth.write(data)

	# bluetooth.read() 함수를 통해 값을 읽음
	if bluetooth.read() == data:

		# 전체 켜기, 전체 끄기의 경우 리스트 전체를 바꾼다.
		if status_num == 3:
			status = status_num

		# 그외의 경우
		else:
			status[status_num] = status_val

		# TCP로 응답
		client.send(data, '\n')

		# 휴대폰으로 알림
		gcmSend(gcm_msg)


# 메인 함수
if __name__ == "__main__":

	logger.info('프로그램 실행되었습니다.')

	# GCM 초기화
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# 상태값 초기화
	status = [0, 0, 0]
	bluetooth = serial.Serial("/dev/rfcomm0", baudrate=19200)
	logger.info('블루투스 설정 완료 되었습니다.')

	# if len(sys.argv) == 1:
	# 	print "Option Error"
	# 	exit(1)
	# port = int(sys.argv[1])

	# sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
	sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
	sock.bind( ('', port) )
	sock.listen(10)

	print "TCP Server Waiting for client on port ", port
	logger.info("TCP 서버 실행되었습니다.")

	# 블루투스로 수신된 데이터 초기화
	bluetooth.flushInput()

	while True:
		
		# data, addr = sock.recvfrom(64)
		client, addr = sock.accept()
		data = client.recv(64)
		print "Connected from ", addr, " ", data
		# print "received message: ", data, " ", addr

		# 쨔철횈째 째체쨌횄 쩌쨀횁짚

		# 전류 값 읽기
		#ampere = bluetooth.read()
		#ampere.split()	# 전류 값 공백(Whitespace) 로 구분

		# 체쨌첫째짧 DB쨌횓 첬책
		#ampSend(ampere[0], ampere[1], ampere[2])

		# DB쩔징쩌짯 쨉짜횑횇횒 쩌쨀횁짚째짧 쨘횘쨌짱쩔짹창
		#mul_conf_list = getMultiConf()

		# 횉철챌 쩍횄째짙 짹쨍횉횕짹창
		today = getToady()

		# 쩔쨔쩐횪 짹창쨈횋 쩌쨀횁짚
		#schedMulti(mul_conf_list, today)




		# data횉 째짧쩔징 쨉청쨋처 쨍횜횈쩌횇횉 째체쨌횄 쩍횉횉횪

		# data째징 1횑쨍챕 1쨔첩 쨍횜횈쩌횇횉 횆횗짹창
		if data == '1':
			#sendBluetooth('1', 0, 1, 'multi1_on')
			bluetooth.write('1')
			logger.info("멀티탭 1의 콘센트 1번이 켜졌습니다")
			# # 쨍쨍쩐횪 쨍횜횈쩌횇횉쨍쨌횓 쨔횧쨘 쨉짜횑횇횒째징 쨘쨍쨀쩍 쨉짜횑횇횒쩔횒 째째쨩 째챈쩔챙
			if bluetooth.read() == data:
			 	status1 = 1
			 	client.send('1\n')
			 	gcmSend('multi1_on')

		elif data == '2':
			#sendBluetooth('2', 1, 1, 'multi2_on')
			logger.info("멀티탭 1의 콘센트 2번이 켜졌습니다")
			bluetooth.write('2')
			if bluetooth.read() == data:
			 	status2 = 1
			 	client.send('2\n')
			 	gcmSend('multi2_on')
		elif data == '3':
			#sendBluetooth('3', 2, 1, 'multi3_on')
			logger.info("멀티탭 1의 콘센트 3번이 켜졌습니다")
			bluetooth.write('3')
			if bluetooth.read() == data:
			 	status3 = 1
			 	client.send('3\n')
			 	gcmSend('multi3_on')
		elif data == '4':
			#sendBluetooth('4', 0, 0, 'multi1_off')
			logger.info("멀티탭 1의 콘센트 1번이 꺼졌습니다")
			bluetooth.write('4')
			if bluetooth.read() == data:
			 	status1 = 0
			 	client.send('4\n')
			 	gcmSend('multi1_off')
		elif data == '5':
			#sendBluetooth('5', 1, 0, 'multi2_off')
			logger.info("멀티탭 1의 콘센트 2번이 꺼졌습니다")
			bluetooth.write('5')
			if bluetooth.read() == data:
			 	status2 = 0
			 	client.send('5\n')
			 	gcmSend('multi2_off')
		elif data == '6':
			#sendBluetooth('6', 2, 0, 'multi3_off')
			logger.info("멀티탭 1의 콘센트 3번이 꺼졌습니다")
			bluetooth.write('6')
			if bluetooth.read() == data:
			 	status3 = 0
			 	client.send('6\n')
			 	gcmSend('multi3_off')
		elif data == '7':
			#sendBluetooth('7', 3, [1, 1, 1], 'multi_all_on' )
			logger.info("멀티탭 1의 콘센트 전부가 켜졌습니다")
			bluetooth.write('7')
			if bluetooth.read() == data:
			 	status1 = 1
			 	status2 = 1
			 	status3 = 1
			 	client.send('7\n')
			 	gcmSend('multi_all_on')
		elif data == '8':
			#sendBluetooth('8', 3, [0, 0, 0], 'multi_all_off')
			logger.info("멀티탭 1의 콘센트 전부가 꺼졌습니다")
			bluetooth.write('8')
			if bluetooth.read() == data:
			 	status1 = 0
			 	status2 = 0
			 	status3 = 0
			 	client.send('8\n')
			 	gcmSend('multi_all_off')
		elif data == '9':

			strStatus = str(status[0]) + str(status[1]) + str(status[2])
			print "Statue : ", strStatus
			client.send(strStatus + "\n")
			logger.info("현재 상태 정보가 전송되었습니다")
		else:
			print "Error value (1 ~ 8)"
			logger.error("할당되지 않는 번호입니다")


