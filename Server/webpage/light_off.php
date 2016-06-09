<html>
	 <head>
		 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		 <meta name="description" content="switchoff">
		 <meta name="author" content="ÃÖ°ÇÁÖ">
		 <link href="font.css" type=text/css rel=stylesheet>
		 <title>Document</title>
	 </head>

	 <body>
		 <?php

			error_reporting(E_ALL);
			
			/* Get the port for the WWW service. */
			$service_port = 12345;
			
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

			socket_write($socket,'2',strlen('2'));
			
			$out = socket_read($socket, 64);
			echo $out;
			

			socket_close($socket);

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query1 = "UPDATE bulb SET STATE= '0' WHERE BULB_NO='1'";
			$query2 = "SELECT * FROM bulb";
			$result1 = mysqli_query($db, $query1);
			$result2 = mysqli_query($db, $query2);
			$row = mysqli_fetch_row($result2);

			echo "<table cellspacing=0 cellpadding=0 width=100% height=100% border=0><tr><td align=center valign=middle>
			<table><tr><td>
			<font size=6px>".$row[3]."</font></td><td>";
			echo "<a href='light_on.php'><img src='switch_off.png' width='100%'  alt='off' title='off' align='middle'></a>
			</td></tr></table>
			</td></tr></table>";
		?>
	</body>
</html>