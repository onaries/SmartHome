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

			socket_write($socket,'8',strlen('8'));
			$out = socket_read($socket, 2048);
			if($out=='8\n'){
			echo "<script>window.alert('멀티탭이 전부 꺼졌습니다.');</script>";
			}else{
			echo "<script>window.alert('데이터를 수신하지 못하였습니다.');</script>";
			}
			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query1 = "UPDATE relay SET STATE='0' WHERE RELAY_NO='1'";
			$query2 = "UPDATE relay SET STATE='0' WHERE RELAY_NO='2'";
			$query3 = "UPDATE relay SET STATE='0' WHERE RELAY_NO='3'";
			$query4 = "SELECT * FROM relay WHERE RELAY_NO = '1'";
			$query5 = "SELECT * FROM relay WHERE RELAY_NO = '2'";
			$query6 = "SELECT * FROM relay WHERE RELAY_NO = '3'";
			$result1 = mysqli_query($db, $query1);
			$result2 = mysqli_query($db, $query2);
			$result3 = mysqli_query($db, $query3);
			$result4 = mysqli_query($db, $query4);
			$result5 = mysqli_query($db, $query5);
			$result6 = mysqli_query($db, $query6);
			$row1 = mysqli_fetch_row($result4);
			$row2 = mysqli_fetch_row($result5);
			$row3 = mysqli_fetch_row($result6);
			
			socket_close($socket);
			echo "<a href='multitab_all_on.php'><img src='btn_all_on.png' width='10%' alt='all_on' title='all_on' ></a>
			<a href='multitab_all_off.php'><img src='btn_all_off.png' width='10%' alt='all_off' title='all_off' ></a><br>";

			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(on,off,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(off,on,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(off,off,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr></table>";
		?>
		</td></tr></table>
	</body>
</html>
