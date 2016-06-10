<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="4번 메뉴">
		<meta name="author" content="최건주">	
		<link href="font.css" type=text/css rel=stylesheet>
		<title>Document</title>
		 
		<style>
			div{
			font-size:26px;
			}
		</style>
	</head>

	<body>
		<script>
			//var ip=prompt("IP address");
			//'http://"+ip+":8080/stream.html'
			var ip = location.host;
			document.write("<table cellspacing=0 cellpadding=0 width=100% height=80% border=0><tr><td align=center valign=bottom><iframe src='http://"+ ip + ":8080/stream_simple.html' width='100%' height='100%' border=0 onload=scrolldown()>아이프레임을 지원하지 않는 브라우저이거나 최신 버전이 아닙니다.</iframe></td></tr></table>");

			setInterval("dpTime()",1000);
			function dpTime(){
				var now = new Date();
				year = now.getFullYear();
				month = (now.getMonth()+1);
				date = now.getDate();
				hours = now.getHours();
				minutes = now.getMinutes();
				seconds = now.getSeconds();
				
				if(hours< 10){
					hours= "0"+hours;
				}
				if(minutes < 10){
					minutes = "0" + minutes;
				}
				if(seconds<10)
				{
					seconds="0" + seconds;
				}
				document.getElementById("dpTime").innerHTML = year+"/" + month+"/" +date+" "+  hours +":" + minutes + ":" + seconds;
			}
		</script>
	<div id ="dpTime" align="center"></div>
	</body>
</html>
