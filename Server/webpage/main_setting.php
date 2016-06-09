<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="6번 메뉴">
		<meta name="author" content="최건주">	
		<title>Document</title>
		<link href="font.css" type=text/css rel=stylesheet>
		<style>
			fieldset{
			margin-left: 38%;
			margin-right: 38%;
			}
			legend{
			font-size: 120%;
			}
		</style>
	</head>

	<body>
		<table cellspacing=6% cellpadding=0 width=100% height=100% border=0><tr><td align=center valign=middle>
		<?php
			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}
			$query_id_pw = "SELECT * FROM clients";
			$result_id_pw = mysqli_query($db, $query_id_pw);
			$row_id_pw = mysqli_fetch_row($result_id_pw);

			$query_bulb_name = "SELECT * FROM bulb WHERE BULB_NO='1'";
			$result_bulb_name = mysqli_query($db, $query_bulb_name);
			$row_bulb_name = mysqli_fetch_row($result_bulb_name);

			$query_multitab_name1 = "SELECT * FROM relay WHERE RELAY_NO='1'";
			$result_multitab_name1 = mysqli_query($db, $query_multitab_name1);
			$row_multitab_name1 = mysqli_fetch_row($result_multitab_name1);

			$query_multitab_name2 = "SELECT * FROM relay WHERE RELAY_NO='2'";
			$result_multitab_name2 = mysqli_query($db, $query_multitab_name2);
			$row_multitab_name2 = mysqli_fetch_row($result_multitab_name2);

			$query_multitab_name3 = "SELECT * FROM relay WHERE RELAY_NO='3'";
			$result_multitab_name3 = mysqli_query($db, $query_multitab_name3);
			$row_multitab_name3 = mysqli_fetch_row($result_multitab_name3);

			echo "<form method='POST' action='change_id_pw.php'>
			<fieldset><legend>ID / PASSWORD 변경</legend>
			아이디&nbsp&nbsp&nbsp&nbsp<input type='text' name='change_id' size='15%' maxlength='13' value=$row_id_pw[0]><br>
			비밀번호&nbsp<input type='password' name='change_password' size='15%' maxlength='13' value=$row_id_pw[1]><br><br>
			<div align='right'><input type='submit' value='변경'></div></fieldset></form>";

			echo "<form method='POST' action='change_bulb_name.php'>
			<fieldset><legend>전구 이름 설정</legend>
			전구 이름&nbsp<input type='text' name='change_bulb_name' size='15%' maxlength='7' value=$row_bulb_name[3]><br><br>
			<div align='right'><input type='submit' value='변경'></div></fieldset></form>";

			echo "<form method='POST' action='change_multitab_name.php'>
			<fieldset><legend>멀티탭 이름 설정</legend>
			콘센트 이름1&nbsp<input type='text' name='change_multitab_name1' size='15%' maxlength='7' value=$row_multitab_name1[2]><br>
			콘센트 이름2&nbsp<input type='text' name='change_multitab_name2' size='15%' maxlength='7' value=$row_multitab_name2[2]><br>
			콘센트 이름3&nbsp<input type='text' name='change_multitab_name3' size='15%' maxlength='7' value=$row_multitab_name3[2]><br><br>
			<div align='right'><input type='submit' value='변경'></div></fieldset></form>";
		?>

		</td></tr>
		<tr><td align=right valign=bottom vspace="3%"><a href="https://play.google.com/store/apps/details?id=com.onaries.smarthome" target="_blank">Google Play</a></td><td align=right valign=bottom ><a href="https://github.com/onaries/SmartHome" target="_blank">GitHub</a></td>
		</tr></table>
	</body>
</html>
