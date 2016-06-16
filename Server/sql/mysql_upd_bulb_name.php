<?php
	include_once "inc.php";	// DB Information

	if (!$mysqli){
		echo "Error: Unable to connect to MySQL." . PHP_EOL;
		echo "Debugging errno: " . mysqli_connect_errno() . PHP_EOL;
		echo "Debugging error: " . mysqli_connect_error() . PHP_EOL;
	}

	$bulb_no = $_GET['bulb_no'];
	$bulb_name = $_GET['bulb_name'];

	$query = "UPDATE bulb SET BULB_NAME = '".$bulb_name."' WHERE BULB_NO = ".$bulb_no;
	if($result = $mysqli->query($query)){
		for($rows = array(); $row = $result->fetch_assoc(); $rows[] = $row);
	}
	echo json_encode($rows);

	mysqli_close($mysqli);
?>
