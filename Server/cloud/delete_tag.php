<?php
require_once('db.php');
$tag = $_GET['tag'];
$q = "DELETE FROM tag_list WHERE tag = '$tag'";
$r = mysqli_query($dbc,$q);
if(!$r)
{
	echo "<script>alert(".$q.");</script>";
	echo "<script>alert('DB 갱신 실패');</script>";
}
echo "<script>location.href = document.referrer;</script>";
?>