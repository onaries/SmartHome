<?php
	$db_host = "localhost";
	$db_user = "root";
	$db_passwd = "raspberry1";
	$db_name = "sensor";
	$conn = mysql_connect($db_host, $db_user, $db_passwd) or die("데이터베이스 연결에 실패햐였습니다");
	$result = mysql_select_db($db_name, $conn);
	$dnum = $_GET["dnum"];
	$temp = $_GET["temp"];
	$humi = $_GET["humi"];
	$gas = $_GET["gas"];
	$photo = $_GET["photo"];
	$n = $_GET["n"];

	switch($n) {
		case 1:
			$query = mysql_query("SELECT * FROM (SELECT num, time, temp, humi FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
			break;
		case 2:
			$query = mysql_query("SELECT * FROM (SELECT num, time, gas FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
			break;
		case 3:
			$query = mysql_query("SELECT * FROM (SELECT num, time, photo FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
			break;
	}

/*
	if(!empty($temp) && !empty($humi)) {
		$query = mysql_query("SELECT * FROM (SELECT num, time, temp, humi FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
	}
	else if(!empty($gas)){
		$query = mysql_query("SELECT * FROM (SELECT num, time, gas FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
	}
	else if(!empty($photo)){
		$query = mysql_query("SELECT * FROM (SELECT num, time, photo FROM data ORDER BY num DESC LIMIT ".$dnum." ) AS d ORDER BY time");
	}
*/
	
	for($rows = array(); $row = mysql_fetch_assoc($query); $rows[] = $row);
	echo json_encode($rows);

	mysql_close($conn);
?>
