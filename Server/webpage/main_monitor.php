<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
		<meta name="description" content="그래프">
		<meta name="author" content="최건주">	

		<title>전력값 그래프 </title>

		<link href="font.css" type=text/css rel=stylesheet>
		<!-- Plotly.js -->
		<script src="https://cdn.plot.ly/plotly-latest.min.js">
		</script>
	</head>

	<body>
		<table cellspacing=0 cellpadding=0 width=100% height=100% border=0>
		<tr>
		<td align=center valign=middle>
		<div id="myDiv" style="width: 70%; height: 80%;"><!-- Plotly chart will be drawn inside this DIV --></div>
		</td>
		</tr>
		</table>
		<script>
			var x_axis = [];
			var y_axis_p1 = [];
			var y_axis_p2 = [];
			var y_axis_p3 = [];
		</script>
		<?php
			@ $db = mysqli_connect("localhost", "root", "autoset","smarthome");

			$cnt=0;

			if(mysqli_connect_errno()) {
			echo 'Error: Could not connect to database. Please try again later.<br>';
			exit;
			}

			$query_count_row = "SELECT * FROM sensor_value";
			$result_count_row = mysqli_query($db, $query_count_row);
			while($count_row = mysqli_fetch_assoc($result_count_row)){$cnt++;};

			for($i=0;$i<1000;$i++){
				$index=$cnt;
				$query="SELECT * FROM sensor_value WHERE NO ='$index'";
				$result= mysqli_query($db,$query);
				$row = mysqli_fetch_row($result);
				$cnt--;
				echo "<script>
				x_axis[$i] = '$row[1]';
				y_axis_p1[$i] = $row[2];
				y_axis_p2[$i] = $row[3];
				y_axis_p3[$i] = $row[4];
				</script>";
				
				if($cnt==0){
					break;
				}
			};
		?>
		<script>
			var trace1 = {
			 x: x_axis,
			 y: y_axis_p1,
			 mode: 'lines+markers',
			 name: '전력 센서1'
			};

			var trace2 = {
			  x: x_axis,
			  y: y_axis_p2,
			  mode: 'lines+markers',
			  name: '전력 센서2'
			};

			var trace3 = {
			  x: x_axis,
			  y: y_axis_p3,
			  mode: 'lines+markers',
			  name: '전력 센서3'
			};
			var data = [trace1, trace2, trace3];

			var layout = {
			 title: 'Power Graph',
			 xaxis: {
			 title: 'Time'
			},
			 yaxis: {
			 title: 'Power'
			 }
			};

			Plotly.newPlot('myDiv', data, layout);	
		</script>
	</body>
</html>