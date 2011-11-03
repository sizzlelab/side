
<?php
theme_page();
function theme_page() {
global $user;
$module_path = drupal_get_path('module', 'chart');
?>
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
$(document).ready(function() {
			$("#datepicker").datepicker({showOn: 'button', buttonImage: '<?php echo $module_path; ?>/images/calendar.gif', buttonImageOnly: true});
			get_project();
			$("#project_list").change(function() {
					draw_blood_preasure_table();
			});
    });


		var chart;
		hs.graphicsDir = 'http://highslide.com/highslide/graphics/';
function draw_chart(){
		draw_blood_preasure_table();
		//document.chart_form.submit(); 
		var module_url = $("#moduleUrl").val();
		//var data_path = module_url + "/<?php echo 'handle_data.php?type=2&start='.$date_now.'&proid='.$_POST['project'].'&perid='.$_POST['person'];?>";
		
		var perid=document.getElementById('person_list').value;
		var proid=document.getElementById('project_list').value;
		var start_str=document.getElementById('datepicker').value;
	    var start_arr=new Array();
			start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		var url="<?=url('chart/handle_data'); ?>?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
			$.getJSON(url, function(data1) {	
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
					//categories: []
					title:{
						text:''
					},
					type: 'datetime'
				},
					tooltip:{
						shared:true,
						crosshairs: true
					},
					yAxis: [{
						title: {
							text: 'Value (bpm)'
							
						},
						max:150,
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

					},{
					title: {
							text: 'Value ( mg/dl)'
							
						},
						opposite:true,
						max:150,
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
					}],
					plotOptions: {
							spline: {
			showCheckbox:true,
			selected:true,
			events: {
				checkboxClick:function(event) {
					if (this.visible) {
						this.hide();
					} else {
						this.show();
					}
				}
			},
			lineWidth:3,
			marker: {
				enabled:false,
				states: {
					hover: {
						enabled:true
					}
				}
			}
		},
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
							data:data1.observation1[0].records,
							name:data1.observation1[0].name
						 },{
							data:data1.observation2[0].records,
							name:data1.observation2[0].name	
						 }
						 
						 ]

			};	
				//alert(data1.observations[0].records);
				var chart = new Highcharts.Chart(options);
				
});
//});
}
		</script> 
	<script>
	
	function get_project(){
		$.getJSON('http://jimu.cs.hut.fi/side/researcher/projects/json',function(results){
			var outputs='<option selected="selected">--Choose project--</option>';
		for(x in results){
			outputs+="<option value='"+results[x]['nid']+"'>"+results[x]['title']+"</option>";
			}
		$('#project_list').html(outputs);
		})
	
	}
	
	function get_person(){
		var project_id=document.getElementById('project_list').value;
		//alert(project_id);
		$.getJSON('http://jimu.cs.hut.fi/side/researcher/projects/persons/json/'+project_id,function(results){
		
		var outputs='<option selected="selected">--Choose person--</option>';
	for(x in results){
		outputs+="<option value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";
		}
    $('#person_list').empty().html(outputs);

		})
		
		//draw_chart();
			}
function get_blood_presure(proid, start, end ){
		var perid=<?=$user->uid ?>;//side/researcher/observations/data/json?type=3
        $.getJSON('http://jimu.cs.hut.fi/side/person/observations/get/json?type=3&proid='+proid+'&end='+end+'&start='+start,function(results){	
		//console.debug(results);
var htm="<table>";        
  var obs = results.observations;
             
htm +="<tr><td><b>"+obs[0]['name']+"</b></td><td></td><td></td><td></td></tr>";
htm += "<tr><td></td><td>Time</td><td>Systolic</td><td>Diastolic</td></tr>";
              for(y in obs[0]['records'])
              {
                    htm += "<tr><td>";
                   
                    htm += y;
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['time'];
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['systolic'];
                    htm += "</td><td>";   //result.observation3[0].records['systolic'],
                    htm += obs[0]['records'][y]['diastolic'];
                    htm += "</td></tr>";
              }
        //  }
          
          
htm = htm+"</table>";
$('#bloodpresure').html(htm);
        })
  }

   function draw_blood_preasure_table(){
		var start_str=document.getElementById('datepicker').value;
		var start_arr=new Array();
		start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		get_blood_presure($("#project_list option:selected").val(), start, end );
  }
</script>	

<input type="hidden" id="moduleUrl" value="<?php echo $module_path; ?>" />
<div style="text-align:center;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" onchange='get_person()' name="project"><option  selected='selected' value="--Choose project--"  >--Choose project--</option></select>
	<select id="person_list" onchange='draw_blood_preasure_table()' name="person"><option  selected='selected' value="--Choose person--" >--Choose person--</option></select>

</div>
<div style"=margin:20px;" id="bloodpresure">

</div>
<div  style="text-align:center;font-size:15px">
<span style="position: relative;left:-60px" onclick='date_change_prev()'> << Previous day</span>
<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?php echo date('m/d/Y');?>' style="background: yellow; margin:0 auto">
<span id="next_day" style="position: relative;right:-60px" onclick='date_change_next()'>Next day >></span>
 </div>
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
