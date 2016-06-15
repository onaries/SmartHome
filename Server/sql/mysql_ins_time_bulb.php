<?php
	include_once "inc.php";	// DB Information

	if (!$mysqli){
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
		echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
		echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	}

	$node = $_GET['node'];
	$weekday = $_GET['weekday'];
	$t1 = $_GET['t1'];
	$t2 = $_GET['t2'];

	$query = "INSERT INTO bulb_conf (BULB_NO, WEEKDAY, START_TIME, STOP_TIME) VALUES (".$node.", ".$weekday.", '".$t1."', '".$t2."')";
	if($result = $mysqli->query($query)){
		for($rows = array(); $row = $result->fetch_assoc(); $rows[] = $row);
	}
	echo json_encode($rows);

	mysqli_close($mysqli);
?>
