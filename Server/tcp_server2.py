# -*- coding: utf-8 -*-
import serial
import socket
import sys
import thread
import threading
import MySQLdb as mdb
from gcm import *

def gcmSend(data_id):
	try:
		con = mdb.connect('localhost', 'root', 'raspberry1', 'sensor')
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

if __name__ == "__main__":

	# GCM ����
	gcm = GCM("AIzaSyAmk7Kau-6e6z7ByozHXTZlzBtjvhxuUEU")

	# ��Ƽ�� ���� ����
	status1 = 0
	status2 = 0
	status3 = 0
	bluetooth = serial.Serial("/dev/rfcomm0", baudrate=19200)
	
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

	# ������� ������ �� �б�����
	bluetooth.flushInput()

	while True:
		# data, addr = sock.recvfrom(64)
		client, addr = sock.accept()
		data = client.recv(64)
		print "Connected from ", addr, " ", data
		# print "received message: ", data, " ", addr

		# ��ư ���� ����
		
				
		# data�� ���� ���� ��Ƽ�� ���� ����

		# data�� 1�̸� 1�� ��Ƽ�� �ѱ�
		if data == '1':
			bluetooth.write('1')
			
			# ���� ��Ƽ������ ���� �����Ͱ� ���� �����Ϳ� ���� ���
			if bluetooth.read() == data:
				status1 = 1
				client.send('1\n')
				gcmSend('multi1_on')
			
		elif data == '2':
			bluetooth.write('2')
			if bluetooth.read() == data:
				status2 = 1
				client.send('2\n')
				gcmSend('multi2_on')
		elif data == '3':
			bluetooth.write('3')
			if bluetooth.read() == data:
				status3 = 1
				client.send('3\n')
				gcmSend('multi3_on')
		elif data == '4':
			bluetooth.write('4')
			if bluetooth.read() == data:
				status1 = 0
				client.send('4\n')
				gcmSend('multi1_off')
		elif data == '5':
			bluetooth.write('5')
			if bluetooth.read() == data:
				status2 = 0
				client.send('5\n')
				gcmSend('multi2_off')
		elif data == '6':
			bluetooth.write('6')
			if bluetooth.read() == data:
				status3 = 0
				client.send('6\n')
				gcmSend('multi3_off')
		elif data == '7':
			bluetooth.write('7')
			if bluetooth.read() == data:
				status1 = 1
				status2 = 1
				status3 = 1
				client.send('7\n')
				gcmSend('multi_all_on')
		elif data == '8':
			bluetooth.write('8')
			if bluetooth.read() == data:
				status1 = 0
				status2 = 0
				status3 = 0
				client.send('8\n')
				gcmSend('multi_all_off')
		elif data == '9':
			strStatus = str(status1) + str(status2) + str(status3)
			print "Statue : ", strStatus
			client.send(strStatus + "\n")
		else:
			print "Error value (1 ~ 8)"
			

