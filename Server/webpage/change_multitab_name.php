<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="전구 이름 변경">
		<meta name="author" content="최건주">	
	</head>

	<body>
		<?php
			$change_multitab_name1 = trim($_POST["change_multitab_name1"]);
			$change_multitab_name2 = trim($_POST["change_multitab_name2"]);
			$change_multitab_name3 = trim($_POST["change_multitab_name3"]);

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query_multitab_name1 = "UPDATE relay SET RELAY_NAME='$change_multitab_name1' WHERE RELAY_NO='1'";
			$result_multitab_name1 = mysqli_query($db,$query_multitab_name1);
			$query_multitab_name2 = "UPDATE relay SET RELAY_NAME='$change_multitab_name2' WHERE RELAY_NO='2'";
			$result_multitab_name2 = mysqli_query($db,$query_multitab_name2);
			$query_multitab_name3 = "UPDATE relay SET RELAY_NAME='$change_multitab_name3' WHERE RELAY_NO='3'";
			$result_multitab_name3 = mysqli_query($db,$query_multitab_name3);

			echo "<script>window.alert('변경되었습니다.');</script>";
			echo "<script>location.href='main_setting.php';</script>";
		?>
	</body>
</html>