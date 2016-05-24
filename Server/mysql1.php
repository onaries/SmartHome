<?php
	include_once "inc.php";	// DB 관련 설정

	if (!$mysqli){
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
		echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
		echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	}

	$query = "SELECT * FROM relay";
	if($result = $mysqli->query($query)){
		for($rows = array(); $row = $result->fetch_assoc(); $rows[] = $row);
	}

	echo json_encode($rows);

	mysqli_close($mysqli);


?>
