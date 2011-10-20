
<?php
theme_page();
function theme_page() {
$module_path = drupal_get_path('module', 'chart');
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
	
<!--	
		<script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.2/jquery.min.js"></script> 
		<script type="text/javascript" src="<?php echo $module_path; ?>/js/highcharts.js"></script> 
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/jquery-1.3.1.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/ui/ui.core.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/calendar/ui/ui.datepicker.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/js/modules/exporting.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/js/themes/grid.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/highslide/highslide.config.js"></script>
		<script type="text/javascript" src="<?php echo $module_path; ?>/highslide/highslide-full.min.js"></script>
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/demos/shadow.css" rel="stylesheet" />
		<link type="text/css" href="<?php echo $module_path; ?>/highslide/highslide.css" rel="stylesheet" />
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/themes/base/ui.all.css" rel="stylesheet" />
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/themes/base/ui.all.css" rel="stylesheet" />
		<link type="text/css" href="<?php echo $module_path; ?>/calendar/demos/demos.css" rel="stylesheet" />
		-->
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
		function date_change_next(){
				var current_date=document.getElementById('datepicker').value;
				
				//var next_day=current_date_arr[2]+'-'+current_date_arr[0]+'-'+(parseInt(current_date_arr[1],10)+1);				
				var current_date_arr=new Array();
				 current_date_arr=current_date.split('/');
				//var next_day=current_date_arr[0]+'/'+(parseInt(current_date_arr[1],10)+1)+'/'+current_date_arr[2];
				
				var date_obj=new Date(current_date_arr[2],current_date_arr[0],(parseInt(current_date_arr[1],10)+1));
				document.getElementById('datepicker').value=date_obj.getMonth()+'/'+(parseInt(current_date_arr[1],10)+1)+'/2011';
				draw_chart();
		}
		function date_change_prev(){
				var current_date=document.getElementById('datepicker').value;
				
				//var next_day=current_date_arr[2]+'-'+current_date_arr[0]+'-'+(parseInt(current_date_arr[1],10)+1);				
				var current_date_arr=new Array();
				 current_date_arr=current_date.split('/');
				//var next_day=current_date_arr[0]+'/'+(parseInt(current_date_arr[1],10)+1)+'/'+current_date_arr[2];
				
				var date_obj=new Date(current_date_arr[2],current_date_arr[0],(parseInt(current_date_arr[1],10)-1));
				document.getElementById('datepicker').value=date_obj.getMonth()+'/'+(parseInt(current_date_arr[1],10)-1)+'/2011';
				draw_chart();
		}
</script>		
<script>
$(document).ready(function() {
			get_project();
			
    });


		var chart;
		hs.graphicsDir = 'http://highslide.com/highslide/graphics/';
function draw_chart(){
		var module_url = $("#moduleUrl").val();		
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
							from: 60,
							to: 100,
							color: 'rgba(0, 255, 0, 0.3)',
							label: {
								text: 'Normal',
								align:'left',
								x:-45,
								style: {
									color: 'rgba(0, 255, 0, 0.4)'
								}
							}
						}, { //High range
							from: 100,
							to: 150,
							color: 'rgba(255, 0, 0, 0.5)',
							label: {
								text: 'Higher',
								align:'left',
								x:-45,
								style: {
									color: '#FF0000'
								}
							}
						},{ //Low range
							from: 30,
							to: 60,
							color: '#FFA500',
							label: {
								text: 'Lower',
								align:'left',
								x:-35,
								style: {
									color: '#FFA500'
								}
							}
						}
						]

					},
					plotOptions: {
					series: {
						cursor: 'pointer',
						point: {
							events: {
								click: function() {
									hs.htmlExpand(null, {
										pageOrigin: {
											x: this.pageX, 
											y: this.pageY
										},
										headingText: this.series.name,
										maincontentText: 'Time: '+Highcharts.dateFormat('%H:%M ', this.x) +'<br/> '+ 
											'Data: '+this.y +' bmp',
										width: 200
									});
								}
							}
						},
						marker: {
							lineWidth: 1
						}
					}
				},
					tooltip:{
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
		$.getJSON('http://jimu.cs.hut.fi/side/person/projects/get/json',function(results){
			var outputs='<option selected="selected">--Choose project--</option>';
		for(x in results){
			outputs+="<option value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";
			}
		$('#project_list').html(outputs);
		})
	
	}
</script>	

 
<form action="" method="post" name="chart_form">
<input type="hidden" id="moduleUrl" value="<?php echo $module_path; ?>" />
<div style="text-align:left;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" name="project"><option  selected='selected' value="--Choose project--"  >--Choose project--</option></select>

</div>
<div  style="text-align:center;font-size:15px">
<span style="position: relative;left:-60px" onclick='date_change_prev()'> << Previous day</span>
<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?php echo date('m/d/Y');?>' style="background: yellow; margin:0 auto">
<span id="next_day" style="position: relative;right:-60px" onclick='date_change_next()'>Next day >></span>
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
				
	
<?php
//exit;
}
?>
