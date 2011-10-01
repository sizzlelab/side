
<?php
theme_page();
function theme_page() {
$module_path = drupal_get_path('module', 'chart');
?>
<?php
$date=str_replace("/","-",$_POST['date']);
$arr=Array();
$arr=explode("-",$date);
$arr[1]=$arr[1]-1;
$prev_time_str=$arr[2]."-".$arr[0]."-".$arr[1];
$prev_day= date("m/d/Y",strtotime($prev_time_str));
$arr[1]=$arr[1]+2;
$next_time_str=$arr[2]."-".$arr[0]."-".$arr[1];
$next_day=date("m/d/Y",strtotime($next_time_str));

?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html> 
	<head> 
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/demos/shadow.css" rel="stylesheet" />
		<!-- 1. Add these JavaScript inclusions in the head of your page --> 
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script> 
		<script type="text/javascript" src="<?php echo $module_path; ?>/js/highcharts.js"></script> 
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/themes/base/ui.all.css" rel="stylesheet" />
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/jquery-1.3.1.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/ui/ui.core.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/ui/ui.datepicker.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/js/modules/exporting.js"></script>
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/demos/demos.css" rel="stylesheet" />
		<script type="text/javascript">
			$(function() {
				$("#datepicker").datepicker({showOn: 'button', buttonImage: '<?php echo $module_path; 
				?>/images/calendar.gif', buttonImageOnly: true});
			});
			function millisecondsStrToDate(str){
        var   startyear   =   1970; 
        var   startmonth   =   1; 
        var   startday   =   1; 
        var   d,   s; 
        var   sep   =   ":"; 
        d   =   new   Date(); 
        d.setFullYear(startyear,   startmonth,   startday); 
        d.setTime(0); 
        d.setMilliseconds(str); 
        s   =   d.getHours()   +   ":"   +   d.getMinutes()   +   ":"   +   d.getSeconds(); 
        //return d.toLocaleString();
		return s;
}
		</script> 
<script>
		function date_change(){
				var current_date=document.getElementById('datepicker').value;
				var current_date_arr=new Array();
				 current_date_arr=current_date.split('/');
				var next_day=current_date_arr[0]+'/'+(parseInt(current_date_arr[1],10)+1)+'/'+current_date_arr[2];
				var date_obj=new Date(next_day);
				document.getElementById('datepicker').value=date_obj.getMonth();
		}

</script>		
<script>
$(document).ready(function() {
			get_project();
    });


		var chart;
function draw_chart(){
		var module_url = $("#moduleUrl").val();		
		var perid=document.getElementById('person_list').value;
		var proid=document.getElementById('project_list').value;
		var start_str=document.getElementById('datepicker').value;
	    var start_arr=new Array();
			start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		var url=module_url+"/handle_data.php?start="+start+'&end='+end+'&proid='+proid;
			$.getJSON(url, function(data1) {
					var test=data1.observations[0].records;
					//var test =replace(test,'"','');
					//document.write(data1.observations[0].records);
					var options = {

				chart: {
					renderTo: 'container',
					defaultSeriesType: 'spline'
				},
				title: {
					text: ''
				},
				credits:{
						enabled:false
					},
				xAxis: {
					title:{
						text:''
					},
					type: 'datetime'
				},
					yAxis: {
						title: {
							text: 'Value (beat/minute)'
						},
						
						plotLines: [{
							value: 0,
							width: 1,
							color: '#808080'
						}],
						min: 0,
						minorGridLineWidth: 0, 
						gridLineWidth: 1,
						alternateGridColor: null,
						plotBands: [ { //Normal range
							from: 60.3,
							to: 70.5,
							color: 'rgba(68, 170, 213, 0.1)',
							label: {
								text: 'Normal range',
								style: {
									color: '#606060'
								}
							}
						}]

					},tooltip:{
						style:{
							fontSize:'7pt'
						
						},
						formatter:function(){
								return millisecondsStrToDate(this.x) +"<br> "+ this.y;
						}
						
				},

				series: [{
							data:data1.observations[0].records,
							name:data1.observations[0].name
								}]

			};	
				//alert(data1.observations[0].records);
				var chart = new Highcharts.Chart(options);
				
});
//});
}
		</script> 
	<script>
	
	function get_project(){
		$.getJSON('http://jimu.cs.hut.fi/side/person/projects',function(results){
			var outputs='<option selected="selected">--Choose project--</option>';
		for(x in results){
			outputs+="<option value='"+results[x]['nid']+"'>"+results[x]['title']+"</option>";
			}
		$('#project_list').html(outputs);
		})
	
	}
</script>	
</head> 
<body> 
<form action="" method="post" name="chart_form">
<input type="hidden" id="moduleUrl" value="<?php echo $module_path; ?>" />
<div style="text-align:left;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" name="project"><option  selected='selected' value="--Choose project--"  >--Choose project--</option></select>

</div>
<div  style="text-align:center;font-size:15px">
<span style="position: relative;left:-60px"> << Previous day</span>
<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='' style="background: yellow; margin:0 auto">
<span id="next_day" style="position: relative;right:-60px" >Next day >></span>
 </div>
</form>
<div id="shadow-container"> 
<div class="shadow1"> 

<div class="shadow2"> 

<div class="shadow3"> 
<!-- 3. Add the container --> 
<div id="container" >
</div> 
</div></div></div></div>
				
	</body> 
</html> 
<?php
//exit;
}
?>
