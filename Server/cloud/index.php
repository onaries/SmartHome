<!DOCTYPE HTML>
<html>
<head>
<link rel="stylesheet" type="text/css" href="myStyle.css">
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<?php
		require_once('db.php');
		$pwd = $_GET['pwd'];
		$srch = $_GET['srch'];
		$mode = $_GET['mode'];
		if($pwd == "")
		{
			$pwd = './cloud/';
		}
		if(!is_dir($pwd) && $srch == "")
		{
			echo "<script>alert(".$pwd." : 존재하지 않거나 디렉터리가 아닙니다.</script>";
			echo "<script>go_back()</script>";
		}
	?>
</head>
<section>
<div class="menu" id="menu">
	<!form method="post" action="index.php">
		<input type="text" id="tag_val" size="15" maxlength="10" />
		<select id="searchOpt" size="1">
			<option value="태그로">태그로</option>
			<option value="이름으로">이름으로</option>
		</select>
		<input type="button" value="검색" onclick="srch_tag()" />
		<input type="image" name="upload" title="파일업로드" src="./image/upload.png" onclick="window.open('./fileupload.php?pwd=<?php echo $pwd?>', '', 'toolbars=no')">
		<input type="image" name="download" title="파일다운로드" src="./image/download.png" onclick="download_item()">
		<input type="image" name="delete" title="파일삭제" src="./image/delete.png" onclick="delete_list_item();reload()">
		<input type="image" name="mkdir" title="폴더생성" src="./image/small_folder.png" onclick="window.open('./make_dir.php?pwd=<?php echo $pwd?>')">

		<input type="button" value="태그편집" onclick="edit_tag()" />
	<!/form>
</div>
<div class = "list" id = "list">
	<?php
		if(strcmp($pwd,'./cloud/'))
		{
			make_upper();
		}
		if($srch == "")
		{
			echo $asdf;
			$q = "SELECT * FROM file_list WHERE path='$pwd' ORDER BY name ASC";
		}
		else if ($mode == '0')
		{
			make_upper();
			$q = "SELECT * FROM file_list f INNER JOIN tag_list t ON f.file_id = t.file_id WHERE tag like '%$srch%' ORDER BY name ASC";
		}
		else if ($mode == '1')
		{
			make_upper();
			$q = "SELECT * FROM file_list WHERE name like '%$srch%' ORDER BY name ASC";
		}
		$r = @mysqli_query($dbc,$q);
		while($row = mysqli_fetch_array($r))
		{
			add_list_item($row);
		}
	?>
</div>
</section>
