<!DOCTYPE HTML>
<?php
	DEFINE ('DB_USER', 'root');
	DEFINE ('DB_PASSWORD', 'autoset');
	DEFINE ('DB_HOST','localhost');
	DEFINE ('DB_NAME', 'test');
	

	$dbc = @mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME) OR die ('Could not connect to MySQL: ' . mysqil_connect_error() );
	function get_file_list($path)
	{
		$q = "SELECT * FROM file_list WHERE path='$path'";
		$r = mysql_query($dbc,$q);
		return $r;
	}

	function add_list_item($info)
	{
		$image_ext = array('jpg','jpeg','gif','png');
		$fullpath = $info['path'] . $info['name'];
		$imagesrc = './image/file.png';
		$title = "경로 : $fullpath";
		$tag = "태그 : ".get_tag(get_file_id($fullpath));
		if(strcasecmp($info['ext'], 'dir') == 0)
		{
			$imagesrc = './image/folder.png';
		}
		else if(in_array($info['ext'], $image_ext))
		{
			$imagesrc = $info['path'] . $info['name'];
		}
		echo '<div class = "list_item" id = ' . $info['path'] . $info['name'] . ' title="'. $title .'&#10;' . $tag.'" onclick="change_checked(this)" ondblclick="item_dbclick(this)">';
		echo	'<img src = ' . $imagesrc .' alt = ' . $info['name'] . ' width = 72 height = 72 ' . '>';
		echo '<p><input type="checkbox" name="checked" value='  . $info['path'] . $info['name'] . ' onclick="event.stopPropagation()" >';
		echo '<span class = "item_name">'.$info['name'].'</span>';
		echo '<a href='. $fullpath . ' id=d_'.$fullpath.' download style="display:none;"></a>';
		echo '</p></div>';

	}

	function make_upper()
	{
		echo '<div class = "list_item" id = upper' . ' onclick="event.stopPropagation()" ondblclick="go_back()" title="뒤로 가기"">';
		echo	'<img src = ./image/folder.png' . ' alt = 뒤로가기' . ' width = 72 height = 72 ' . '>';
		echo '<p><input type="checkbox" name="checked" value=upper' . ' onclick="cancle(this)" >';
		echo "..";
		echo '</p></div>';
	}

	function get_tag($file_id)
	{
		$dbc = @mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME) OR die ('Could not connect to MySQL: ' . mysqil_connect_error() );
		$tag = "";
		$q = "SELECT tag FROM tag_list WHERE file_id = '$file_id' ORDER BY tag ASC";
		$r = mysqli_query($dbc,$q);
		$n = mysqli_num_rows($r);
		for($i = 0; $i < $n; $i++)
		{
			if($row = mysqli_fetch_array($r))
			{
				$tag .= $row['tag'];
			}
			if($i+1 != $n)
			{
				$tag .= ", ";
			}
		}
		return $tag;
	}
	function get_file_id($fullpath)
	{
		$dbc = @mysqli_connect(DB_HOST, DB_USER, DB_PASSWORD, DB_NAME) OR die ('Could not connect to MySQL: ' . mysqil_connect_error() );
		$name = substr(strrchr($fullpath, "/"),1);
		$path = substr($fullpath,0,strlen($fullpath)-strlen($name));
		$q = "SELECT file_id FROM file_list WHERE path = '$path' AND name='$name'";
		$r = mysqli_query($dbc,$q);
		$row = mysqli_fetch_row($r);
		$file_id = $row[0];
		return $file_id;
	}

?>
<script type="text/javascript" src="event.js"></script>
