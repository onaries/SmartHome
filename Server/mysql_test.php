<?php
	$db_host = "localhost";
	$db_user = "root";
	$db_passwd = "raspberry";
	$db_name = "sensor";
	$conn = mysql_connect($db_host, $db_user, $db_passwd) or die("�����ͺ��̽� ���ῡ �����ῴ���ϴ�");
	$result = mysql_select_db($db_name, $conn);
	

	$query = mysql_query("SELECT time, temp, humi, gas, photo from data");
	
	for($rows = array(); $row = mysql_fetch_assoc($query); $rows[] = $row);
	echo json_encode($rows);

	mysql_close($conn);
?>
<script type="text/javascript"