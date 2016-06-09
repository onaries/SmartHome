<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="로그인 처리">
		<meta name="author" content="최건주">	
	</head>

	<body>
		<?php
			$id = trim($_POST["idx"]);
			$password = trim($_POST["passwordx"]);

			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query = "SELECT * FROM clients";
			$result = mysqli_query($db, $query);

			$row = mysqli_fetch_row($result);

			if(($id == $row[0]) && ($password == $row[1])){
			echo "<script>location.href='main.html';</script>";
			}else
			{
			echo "<script>window.alert('잘못된 아이디 또는 비밀번호입니다.');</script>";
			echo "<script>location.href='index.html';</script>";
			}
		?>
	</body>
</html>