<?php
	$db_host = "localhost";
	$db_user = "root";
	$db_passwd = "raspberry1";
	$db_name = "sensor";
	$conn = mysql_connect($db_host, $db_user, $db_passwd) or die("데이터베이스 연결에 실패햐였습니다");
	$result = mysql_select_db($db_name, $conn);
	

	$query = mysql_query("SELECT * from data order by num desc LIMIT 1 ");
	
	for($rows = array(); $row = mysql_fetch_assoc($query); $rows[] = $row);
	echo json_encode($rows);

	mysql_close($conn);
?>
