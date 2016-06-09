<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="전구 이름 변경">
		<meta name="author" content="최건주">	
	</head>

	<body>
		<?php
			$change_bulb_name = trim($_POST["change_bulb_name"]);

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query_bulb_name = "UPDATE bulb SET BULB_NAME='$change_bulb_name' WHERE BULB_NO='1'";
			$result_bulb_name = mysqli_query($db,$query_bulb_name);

			echo "<script>window.alert('변경되었습니다.');</script>";
			echo "<script>location.href='main_setting.php';</script>";
		?>
	</body>
</html>