function change_checked(sender)
{
	var value = sender.id;
	var chk = document.getElementsByName("checked");

	for(var i = 0; i < chk.length; i++)
	{

		if(chk[i].value == value)
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

function delete_list_item()
{
	
	var chk = document.getElementsByName("checked");
	for(var i = 0; i < chk.length; i++)
	{
		if(chk[i].checked)
		{
			window.open("./delete.php?item=" + chk[i].value);
		}

	}
}

function item_dbclick(sender)
{
	var path = sender.id;
	var file_check = /\S\.\S/
	if(file_check.test(path.substring(path.lastIndexOf("/")+1)))
	{
		download_item2(path);
	}
	else
	{
		location.href = "/cloud/index.php?pwd=" + path + "/";
	}
}

function download_item()
{
	var chk = document.getElementsByName("checked");
	for(var i = 0; i < chk.length; i++)
	{
		if(chk[i].checked)
		{

			var evt = document.createEvent('MouseEvents');

			evt.initMouseEvent('click', true, false);
			document.getElementById('d_'+chk[i].value).dispatchEvent(evt);
		}
	}
}

function download_item2(item)
{
	var evt = document.createEvent('MouseEvents');
	evt.initMouseEvent('click', true, false);
	document.getElementById('d_'+item).dispatchEvent(evt);
}

function reload()
{

	setTimeout(document.location.reload(),1500);
}

function go_back()
{
	history.back();
}

function cancle(sender)
{
	var value = sender.id;
	var chk = document.getElementsByName("checked");

	for(var i = 0; i < chk.length; i++)
	{
		chk[i].checked=false;
	}
}

function edit_tag()
{
	var chk = document.getElementsByName("checked");
	for(var i = 0; !chk[i].checked; i++)
	{
		
	}
	window.open("./tag_edit.php?item="+chk[i].value);
}

function get_tag_val()
{
	var result = document.getElementById("tag_val");
	var input_tag = result.value;
	result.value = "";
	return input_tag;
}

function insert_tag(file_id)
{	
	var tag_val = get_tag_val();
	if(tag_val == "")
	{
		alert("태그를 입력하세요");
	}
	else
	{
		location.href = "insert_tag.php?file_id=" + file_id + "&tag=" + tag_val;
	}
}

function delete_tag()
{
	var tag_val = get_tag_val();
	if(tag_val == "")
	{
		alert("태그를 입력하세요");
	}
	else
	{
		location.href = "delete_tag.php?tag=" + tag_val;
	}
}

function srch_tag()
{
	var tag_val = get_tag_val();
	if(tag_val == "")
	{
		alert("태그를 입력하세요");
	}
	else
	{
		var s_index = document.getElementById("searchOpt").selectedIndex;
		if(s_index == 0)
		{
			location.href = "index.php?srch=" + tag_val + "&mode=" + s_index;
		}
		else if(s_index == 1)
		{
			location.href = "index.php?srch=" + tag_val + "&mode=" + s_index;
		}
	}
}