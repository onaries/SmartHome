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
		$imagesrc = '/image/file.png';
		if(strcasecmp($info['ext'], 'dir') == 0)
		{
			$imagesrc = 'image/folder.png';
		}
		else if(in_array($info['ext'], $image_ext))
		{
			$imagesrc = $info['path'] . $info['name'];
		}
		echo '<div class = "list_item" id = ' . $info['name'] . '>';
		echo	'<img src = ' . $imagesrc .' alt = ' . $info['name'] . ' width = 72 height = 72 ' . '>';
		echo '<p><input type="checkbox" name="checked" value='  . $info['path'] . $info['name'] . '/>';
		echo $info['name'];
		echo '</p></div>';
	}
?>

<script>
	function change_checked($value)
	{
		alert("하이용");
		var chk = document.getElementsByName("checked[]");
		for(var i = 0; i < chk.length; i++)
		{
			if(chk[i].value == $value)
			{
				if(chk[i].checked)
				{
					chk[i].checked=false;
				}
				else
				{
					chk[i].checked=true;
				}
			}
		}

	}
</script>