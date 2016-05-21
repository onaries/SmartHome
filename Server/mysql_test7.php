<?php
	$db_host = "localhost";
	$db_user = "root";
	$db_passwd = "raspberry1";
	$db_name = "sensor";
	$conn = mysql_connect($db_host, $db_user, $db_passwd) or die("데이터베이스 연결에 실패햐였습니다");
	$result = mysql_select_db($db_name, $conn);
	$reg_id = $_GET["reg_id"];
	$state = $_GET["state"];
	if(!empty($reg_id)){
		$query = mysql_query("INSERT INTO reg_id (reg_id, state) VALUES (".$reg_id.", ".$state.")");
		if (mysql_error($conn) != "")
			echo "Failed";
		else
			echo "Success";
	}
	
	mysql_close($conn);
?>
