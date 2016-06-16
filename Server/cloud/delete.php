<?php
	$fullname = $_GET['item'];
	require("db.php");
	$name = substr(strrchr($fullname, "/"),1);
	$q = "DELETE FROM file_list WHERE " . "name='$name'";
	if(is_dir($fullname))
	{
		if(rmdir($fullname))
		{
			$q = "DELETE FROM file_list WHERE " . "name='$name'";
			$r = mysqli_query($dbc, $q);
			if($r)
			{
			}
			else
			{
				echo '<script>alert("DB 갱신 실패");</script>';
			}
		}

		else
		{
			echo '<script>alert("삭제 실패");</script>';
		}
	}
	else if(is_file($fullname))
	{
		if(unlink($fullname))
		{
			$q = "DELETE FROM file_list WHERE " . "name='$name'";
			$r = mysqli_query($dbc, $q);
			if($r)
			{
			}
			else
			{
				echo '<script>alert("DB 갱신 실패");</script>';
			}
		}
		else
		{
			echo '<script>alert("삭제 실패");</script>';
		}
	}
	//echo "<script> window.opener.document.location.href = window.opener.document.URL;</script>";
	echo "<script>this.close();</script>";
?>