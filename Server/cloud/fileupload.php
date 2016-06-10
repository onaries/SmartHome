  <form enctype="multipart/form-data" action="" method="POST"> 
    <input type="hidden" name="MAX_FILE_SIZE" value="104857600" /> 
    이 파일을 전송합니다: <input name="userfile" type="file" /> 
    <input type="submit" value="업로드" /> 
 </form>
 <script type="text/javascript">
window.opener.document.location.href = window.opener.document.URL; 
</script> 

 <?php 
 require_once ('db.php');
 // uploads디렉토리에 파일을 업로드합니다. 
 $uploaddir = './cloud/'; 
 $uploadfile = '' . basename($_FILES['userfile']['name']); 
 $init=1;
 	if($_POST['MAX_FILE_SIZE'] < $_FILES['userfile']['size'])
 	{ 
    	  echo "업로드 파일이 지정된 파일크기보다 큽니다.\n"; 
 	}
 	else 
 	{ 
    	if(($_FILES['userfile']['error'] > 0) || ($_FILES['userfile']['size'] <= 0))
    	{ 
    		if($init)
    		{
       	 		$init=0;
       	 	}
       	 	else
       	 	{
       	 		echo "파일 업로드에 실패하였습니다."; 
       	 	}
    	} 
    	else 
     	{ 
        	// HTTP post로 전송된 것인지 체크합니다. 
          	if(!is_uploaded_file($_FILES['userfile']['tmp_name'])) 
          	{ 
                	echo "HTTP로 전송된 파일이 아닙니다."; 
          	}
          	else
          		{
          			if(file_exists($uploadfile))
	          		{
		          		echo "이미 파일이 존재합니다.";
          			}
        		   	else 
        		   	{ 
                		// move_uploaded_file은 임시 저장되어 있는 파일을 ./uploads 디렉토리로 이동합니다. 
                		if (move_uploaded_file($_FILES['userfile']['tmp_name'], $uploaddir . $uploadfile)) 
                		{ 
                     		echo "성공적으로 업로드 되었습니다.\n"; 
                     		$q = "INSERT INTO file_list (path, name ,size, ext) VALUES ('$uploaddir','" . $uploadfile . "' ," . $_FILES['userfile']['size'] . ", '" . substr($uploadfile,-3) . "')";
                     		$r = mysqli_query($dbc,$q);
                     		if($r)
                     		{
                     			
                     		}
                     		else
                     		{
                     			echo 'DB 갱신 실패' . mysqli_error($dbc);
                     			unlink($uploadfile);
                     		}

                		}
                		else 
                		{ 
                     		echo "파일 업로드 실패입니다.\n"; 
                		} 
          			} 
     			} 
 		}
 	}
 	mysqli_close($dbc);
 ?> 