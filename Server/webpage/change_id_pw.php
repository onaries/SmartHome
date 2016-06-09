<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="아이디 비번 변경">
		<meta name="author" content="최건주">	
	</head>

	<body>
		<?php
			$id = trim($_POST["change_id"]);
			$password = trim($_POST["change_password"]);

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query_id = "UPDATE clients SET ID='$id'";
			$result_id = mysqli_query($db, $query_id);

			$query_password = "UPDATE clients SET PASSWORD='$password'";
			$result_password = mysqli_query($db, $query_password);

			echo "<script>window.alert('변경되었습니다.');</script>";
			echo "<script>location.href='main_setting.php';</script>";
		?>
	</body>
</html>