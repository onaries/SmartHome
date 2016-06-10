<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="2번 메뉴">
		<meta name="author" content="최건주">
		<link href="font.css" type=text/css rel=stylesheet>
		<title>Document</title>
	</head>

	<body>
		<table cellspacing=0 cellpadding=0 width=100% height=100% border=0>
		<tr><td align=center valign=middle>	
		<?php
		
			error_reporting(E_ALL);
			
			/* Get the port for the WWW service. */
			$service_port = 12346;
			
			/* Get the IP address for the target host. */
			$address = 'localhost';
			
			/* Create a TCP/IP socket. */
			$socket = socket_create(AF_INET, SOCK_STREAM, SOL_TCP);
			if ($socket === false) {
				echo "socket_create() failed: reason: " . socket_strerror(socket_last_error()) . "\n";
			} else {}

			$result = socket_connect($socket, $address, $service_port);
			if ($result === false) {
				echo "socket_connect() failed.\nReason: ($result) " . socket_strerror(socket_last_error($socket)) . "\n";
			} else {}

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query4 = "SELECT * FROM relay WHERE RELAY_NO = '1'";
			$query5 = "SELECT * FROM relay WHERE RELAY_NO = '2'";
			$query6 = "SELECT * FROM relay WHERE RELAY_NO = '3'";
			$result4 = mysqli_query($db, $query4);
			$result5 = mysqli_query($db, $query5);
			$result6 = mysqli_query($db, $query6);
			$row1 = mysqli_fetch_row($result4);
			$row2 = mysqli_fetch_row($result5);
			$row3 = mysqli_fetch_row($result6);

			if($row1[1]==1){}
			else{
				$query1 = "UPDATE relay SET STATE='1' WHERE RELAY_NO='1'";
				$result1 = mysqli_query($db, $query1);
				socket_write($socket,'1',strlen('1'));
				$out = socket_read($socket, 2048);
				if($out=="1\n"){
				echo "<script>window.alert('멀티탭1이 켜졌습니다.');</script>";
				}else{
				echo "<script>window.alert('데이터를 수신하지 못하였습니다.');</script>";
				}
			}
			if($row2[1]==0){}
			else
			{
				$query2 = "UPDATE relay SET STATE='0' WHERE RELAY_NO='2'";
				$result2 = mysqli_query($db, $query2);
				socket_write($socket,'5',strlen('5'));
				$out = socket_read($socket, 2048);
				if($out=="5\n"){
				echo "<script>window.alert('멀티탭2가 꺼졌습니다.');</script>";
				}else{
				echo "<script>window.alert('데이터를 수신하지 못하였습니다.');</script>";
				}
			}
			if($row3[1]==1){}
			else
			{
				$query3 = "UPDATE relay SET STATE='1' WHERE RELAY_NO='3'";
				$result3 = mysqli_query($db, $query3);
				socket_write($socket,'3',strlen('3'));
				$out = socket_read($socket, 2048);
				if($out=="3\n"){
				echo "<script>window.alert('멀티탭3이 켜졌습니다.');</script>";
				}else{
				echo "<script>window.alert('데이터를 수신하지 못하였습니다.');</script>";
				}
			}

			socket_close($socket);
			echo "<a href='multitab_all_on.php'><img src='btn_all_on.png' width='10%' alt='all_on' title='all_on' ></a>
			<a href='multitab_all_off.php'><img src='btn_all_off.png' width='10%' alt='all_off' title='all_off' ></a><br>";

			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(off,off,on).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(on,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(on,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr></table>";
		?>
		</td></tr></table>
	</body>
</html>
