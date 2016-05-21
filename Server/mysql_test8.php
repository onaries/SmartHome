<?php
	$db_host = "localhost";
	$db_user = "root";
	$db_passwd = "raspberry1";
	$db_name = "sensor";
	$conn = mysql_connect($db_host, $db_user, $db_passwd) or die("데이터베이스 연결에 실패햐였습니다");
	$result = mysql_select_db($db_name, $conn);
	$gcm = $_GET["gcm"];
	$reg_id = $_GET["reg_id"];
	$gas = $_GET["gas"];
	$humi_high = $_GET["humi_high"];
	$humi_low= $_GET["humi_low"];
	$temp_high = $_GET["temp_high"];
	$temp_low = $_GET["temp_low"];
	$update_time = $_GET["update_time"];

	if(!empty($gcm) || $gcm == "0") {
		$query = mysql_query("UPDATE reg_id SET state = ".$gcm." WHERE reg_id = '".$reg_id."' ");
		if (mysql_error($conn) != "")
			echo "Failed";
		else
			echo "Success1";
	}
	else if(!empty($gas)) {
		$query = mysql_query("UPDATE state SET value = ".$gas." WHERE name = 'gas'");
		echo "Success2";
	}
	else if(!empty($humi_high)) {
		$query = mysql_query("UPDATE state SET value = ".$humi_high." WHERE name = 'humi_high'");
		echo "Success3";
	}
	else if(!empty($humi_low)) {
		$query = mysql_query("UPDATE state SET value = ".$humi_low." WHERE name = 'humi_low'");
		echo "Success4";
	}
	else if(!empty($temp_high)) {
		$query = mysql_query("UPDATE state SET value = ".$temp_high." WHERE name = 'temp_high'");
		echo "Success5";
	}
	else if(!empty($temp_low)) {
		$query = mysql_query("UPDATE state SET value = ".$temp_low." WHERE name = 'temp_low'");
		echo "Success6";
	}
	else if(!empty($update_time)) {
		$query = mysql_query("UPDATE state SET value = ".$update_time." WHERE name = 'update_time'");
		echo "Success7";
	}
	else {
		echo "Failed";
	}

	// echo $gcm." ".$gas." ".$humi_high." ".$humi_low." ". $temp_high." ".$temp_low." ".$update_time;
	
	// for($rows = array(); $row = mysql_fetch_assoc($query); $rows[] = $row);
	// echo json_encode($rows);

	mysql_close($conn);
?>
