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

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query1 = "SELECT * FROM relay WHERE RELAY_NO = '1'";
			$query2 = "SELECT * FROM relay WHERE RELAY_NO = '2'";
			$query3 = "SELECT * FROM relay WHERE RELAY_NO = '3'";
			$result1 = mysqli_query($db, $query1);
			$result2 = mysqli_query($db, $query2);
			$result3 = mysqli_query($db, $query3);
			$row1 = mysqli_fetch_row($result1);
			$row2 = mysqli_fetch_row($result2);
			$row3 = mysqli_fetch_row($result3);

			echo "<a href='multitab(on,on,on).php'><img src='btn_all_on.png' width='10%' alt='all_on' title='all_on' ></a>
			<a href='multitab(off,off,off).php'><img src='btn_all_off.png' width='10%' alt='all_off' title='all_off' ></a><br>";

			if(($row1[1]==0) && ($row2[1]==0) && ($row3[1]==0)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(on,off,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(off,on,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(off,off,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==0) && ($row2[1]==0) && ($row3[1]==1)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(on,off,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(off,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(off,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==0) && ($row2[1]==1) && ($row3[1]==0)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(on,on,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(off,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(off,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==1) && ($row2[1]==0) && ($row3[1]==0)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(off,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(on,on,off).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(on,off,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==0) && ($row2[1]==1) && ($row3[1]==1)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(on,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(off,off,on).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(off,on,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==1) && ($row2[1]==0) && ($row3[1]==1)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(off,off,on).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(on,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(on,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==1) && ($row2[1]==1) && ($row3[1]==0)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(off,on,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(on,off,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(on,on,on).php'><img src='switch_off.png' width='100%' alt='off' title='off' align='middle'></a></td></tr></table>";
			}else if(($row1[1]==1) && ($row2[1]==1) && ($row3[1]==1)){
			echo "<table><tr><td><font size=6px>".$row1[2]."</font></td><td>";
			echo "<a href='multitab(off,on,on).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row2[2]."</font></td><td>";
			echo "<a href='multitab(on,off,on).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr>";
			echo "<tr><td><font size=6px>".$row3[2]."</font></td><td>";
			echo "<a href='multitab(on,on,off).php'><img src='switch_on.png' width='100%' alt='on' title='on' align='middle'></a></td></tr></table>";
			}else {
			echo "<script>window.alert('멀티탭의 상태값이 잘못 주어졌습니다.');</script>";
			}
		?>
		</td></tr></table>
	 </body>
</html>
