<?php
	require_once('db.php');
	$item = $_GET['item'];
	$file_id = get_file_id($item);
	$tag = get_tag($file_id);
?>
<!DOCTYPE HTML>
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="myStyle.css">
		<script type="text/javascript" src="event.js"></script>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	</head>
	<h1>태그 편집</h1>
	<div class = "head" id = "head">
		<input type="text" id="tag_val" size="15" maxlength="10" />
		<input type="submit" id="insert" value="추가" onclick=insert_tag(<?php echo $file_id; ?>)>
		<input type="submit" id="delete" value="제거" onclick=delete_tag()>
		<font size = 2> (태그는 10자 까지 사용할 수 있습니다.)</font>
	</div>
	<div class = "content" id = "content">
		<font size = 3>현재 편집중인 파일</font>
		<p><font size = 4><B><?php echo $_GET['item'] ?></B></font></p>
		<font size = 4><B>태그 : <?php echo $tag ?></B></font>
	</div>
	<script type="text/javascript">
		window.opener.document.location.href = window.opener.document.URL; 
	</script> 
</html>
