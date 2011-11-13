var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

$(function() {
    $("#datepicker").datepicker({showOn: 'button', buttonImage: Drupal.settings.chart.module_path+'/images/calendar.gif', buttonImageOnly: true});
    get_project();
    $("#person_list").change(function() {
	//draw_blood_preasure_table();
	draw_chart();
    });
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

function draw_chart(){
    draw_blood_preasure_table();
    //document.chart_form.submit(); 
    var module_url = Drupal.settings.chart.module_path;
    //var data_path = module_url + "/<?php echo 'handle_data.php?type=2&start='.$date_now.'&proid='.$_POST['project'].'&perid='.$_POST['person'];?>";
    var perid=document.getElementById('person_list').value;
    var proid=document.getElementById('project_list').value;
    var start_str=document.getElementById('datepicker').value;
    var start_arr=new Array();
    start_arr=start_str.split('/');
    var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
    var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
    var url=Drupal.settings.chart.handle_data+"?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
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

function get_project(){
    $.getJSON(Drupal.settings.chart.getprojects,function(results){
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
    $.getJSON(Drupal.settings.chart.getprojectpersons+project_id,function(results){
	var outputs='<option selected="selected">--Choose person--</option>';
	for(x in results){
	    outputs+="<option value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";
	}
        $('#person_list').empty().html(outputs);
    })
    //draw_chart();
}

function get_blood_presure(proid, perid,start, end ){
    $.getJSON(Drupal.settings.chart.getpersonobservation+'?type=3&proid='+proid+'&perid='+perid+'&end='+end+'&start='+start,function(results){	
    var htm="<table>";        
    var obs = results.observations;
    htm +="<tr><td><b>"+obs[0]['name']+"</b></td><td></td><td></td><td></td></tr>";
    htm += "<tr><td></td><td>Time</td><td>Systolic</td><td>Diastolic</td></tr>";
    for(y in obs[0]['records']){
                    htm += "<tr><td>";
                   
                    htm += parseInt(y+1);
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['time'];
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['systolic'];
                    htm += "</td><td>";   //result.observation3[0].records['systolic'],
                    htm += obs[0]['records'][y]['diastolic'];
                    htm += "</td></tr>";
    }
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
		get_blood_presure($("#project_list option:selected").val(),$("#person_list option:selected").val(), start, end );
  }