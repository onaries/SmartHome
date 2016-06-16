<?php
	include_once "inc.php";	// DB Information

	if (!$mysqli){
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
		echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
		echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	}

	$reg_id = $_GET['reg_id'];
	$state = $_GET['state'];

	$query = "INSERT INTO reg_id (REG_ID, STATE) VALUES (".$reg_id.", ".$state.")";
	if($result = $mysqli->query($query)){
		for($rows = array(); $row = $result->fetch_assoc(); $rows[] = $row);
	}
	echo json_encode($rows);

	mysqli_close($mysqli);
?>
