<?php
require_once('db.php');
$file_id = $_GET['file_id'];
$tag = $_GET['tag'];
$q = "SELECT * FROM tag_list WHERE tag='$tag'";
$r = mysqli_query($dbc,$q);
if(!$r)
{
	echo "<script>alert('이미 추가된 태그입니다.');</script>";
	echo "<script>location.href = document.referrer;</script>";
}
else
{
	$q = "INSERT INTO tag_list (file_id, tag) VALUES ($file_id, '$tag')";
	$r = mysqli_query($dbc,$q);
	if(!$r)
	{
		echo "<script>alert('DB 갱신 실패');</script>";
	}
	echo "<script>location.href = document.referrer;</script>";
}

?>