<html>
	<head>
		 <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		 <meta name="description" content="1번 메뉴">
		 <meta name="author" content="최건주">
		 <link href="font.css" type=text/css rel=stylesheet>
		 <title>Document</title>
	 </head>

	 <body>  	
		<?php
			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query = "SELECT * FROM bulb WHERE BULB_NO='1'";
			$result = mysqli_query($db, $query);
			$row = mysqli_fetch_row($result);

			if($row[1]==0){
			echo "<table cellspacing=0 cellpadding=0 width=100% height=100% border=0><tr><td align=center valign=middle>
			<table><tr><td><font size=6px>".$row[3]."</font></td><td>";
			echo "<a href='light_on.php'><img src='switch_off.png' width='100%'  alt='off' title='off' align='middle'></a></td></tr></table></td></tr></table>";
			}else if($row[1]==1){
			echo "<table cellspacing=0 cellpadding=0 width=100% height=100% border=0><tr><td align=center valign=middle>
			<table><tr><td><font size=6px>".$row[3]."</font></td><td>";
			echo "<a href='light_off.php'><img src='switch_on.png' width='100%'  alt='on' title='on' align='middle'></a></td></tr></table></td></tr></table>";
			}else{
			echo "<script>window.alert('전구의 상태값이 잘못 주어졌습니다.');</script>";
			}
		?>		
	</body>
</html>
