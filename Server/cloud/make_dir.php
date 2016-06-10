<?php 

		require_once('db.php');
		$i = 1;
		$name = "new_folder";
		$pwd=$_GET['pwd'];
	
		if(is_dir($pwd . $name))
		{

			while(is_dir($pwd . $name . $i))
			{
				$i = $i + 1;
			}
			$name.=$i;
		}
		if(!mkdir($pwd . $name))
		{
			echo '<script>alert("폴더 생성 실패")</script>';
		}
		else
		{
			$q = "INSERT INTO file_list (path, name ,size, ext) VALUES ('$pwd','$name', 0, 'dir')";
			$r = mysqli_query($dbc,$q);
			if($r)
			{
			}
			else
			{
				echo '<script>alert("DB 갱신 실패")</script>';
				rmdir($pwd . $name);
			}
		}
		echo "<script> window.opener.document.location.href = window.opener.document.URL; self.close(); </script>";
	?>