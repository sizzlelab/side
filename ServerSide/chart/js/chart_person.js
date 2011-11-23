var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

$(document).ready(function() {
    $("#datepicker").datepicker({showOn: 'button', buttonImage: Drupal.settings.chart.module_path+'/images/calendar.gif', buttonImageOnly: true});
    get_project();
    $("#project_list").change(function() {
	draw_chart();
    });
	remove_bloodpresure_loading();
	remove_glucose_loader();
	remove_chart_loader();
});



 function remove_bloodpresure_loading() {       
         var targelem = document.getElementById('bloodpresure_loader');
         targelem.style.display='none';
         targelem.style.visibility='hidden';
      }
 function remove_glucose_loader() {       
         var targelem = document.getElementById('glucose');
         targelem.style.display='none';
         targelem.style.visibility='hidden';
      }	 
 function remove_chart_loader() {       
         var targelem = document.getElementById('chart');
         targelem.style.display='none';
         targelem.style.visibility='hidden';
      }	  
function draw_chart(){
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				});

    draw_tables();
    var module_url = Drupal.settings.chart.module_path;
    var perid=Drupal.settings.chart.current_user;
    var proid=document.getElementById('project_list').value;
    var start_str=document.getElementById('datepicker').value;
    var start_arr=new Array();
    start_arr=start_str.split('/');
    var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
    var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
    var url=Drupal.settings.chart.handle_heart_beat_data+"?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
    $.getJSON(url, function(data1) {
	//var test=data1.observations[0].records;			
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

		}],
					plotOptions: {
					spline: {			
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
								return Highcharts.dateFormat('%H:%M ', this.x) +"<br> "+ this.y;
						}
						
				},

				series: [{
							data:data1.observations[0].records,
							name:data1.observations[0].name
						 }
						 
						 ]

			};	
				//alert(data1.observations[0].records);
				var chart = new Highcharts.Chart(options);
				
});
//});
}

	
function get_project(){
    $.getJSON(Drupal.settings.chart.getpersonprojects,function(results){
			var outputs='<option selected="selected">--Choose project--</option>';
		for(x in results){
			outputs+="<option value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";
			}
		$('#project_list').html(outputs);
		})
	
}

function get_blood_presure(proid, start, end ){
	var perid=Drupal.settings.chart.current_user;//side/researcher/observations/data/json?type=3
        $.getJSON(Drupal.settings.chart.getpersondata+'?type=3&proid='+proid+'&end='+end+'&start='+start,function(results){
	
	    //console.debug(results);
	    var htm="<table>";        
	    var obs = results.observations;
	    //for(x in obs){
        var row_id;     
	    htm +="<tr><td><b>"+obs[0]['name']+"</b></td><td></td><td></td><td></td></tr>";
	    htm += "<tr><td></td><td>Time</td><td>Systolic</td><td>Diastolic</td></tr>";
	    for(y in obs[0]['records']){
                    htm += "<tr><td>";
                    row_id=parseInt(y)+1;
                    htm += row_id;
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
  
 function get_glucose(proid, start, end ){
	var perid=Drupal.settings.chart.current_user;//side/researcher/observations/data/json?type=3
        $.getJSON(Drupal.settings.chart.getpersondata+'?type=2&proid='+proid+'&end='+end+'&start='+start,function(results){
	
	    //console.debug(results);
	    var htm="<table>";        
	    var obs = results.observations;
	    //for(x in obs){
        var row_id;     
	    htm +="<tr><td><b>"+obs[0]['name']+"</b></td><td></td><td></td></tr>";
	    htm += "<tr><td></td><td>Time</td><td>Glucose</td></tr>";
	    for(y in obs[0]['records']){
                    htm += "<tr><td>";
                    row_id=parseInt(y)+1;
                    htm += row_id;
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['time'];
                    htm += "</td><td>";
                    htm += obs[0]['records'][y]['glucose'];
                    htm += "</td></tr>";   //result.observation3[0].records['systolic'],                                     
	    }
	    //  }
          
	    htm = htm+"</table>";
	    $('#glucose').html(htm);
        })
  } 
  function draw_tables(){
	draw_blood_preasure_table();
	draw_glucose_table();
  }
  function draw_blood_preasure_table(){
		var start_str=document.getElementById('datepicker').value;
		var start_arr=new Array();
		start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		get_blood_presure($("#project_list option:selected").val(), start, end );
  }
   function draw_glucose_table(){
		var start_str=document.getElementById('datepicker').value;
		var start_arr=new Array();
		start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		get_glucose($("#project_list option:selected").val(), start, end );
  } 