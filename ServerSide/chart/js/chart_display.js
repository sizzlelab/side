var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

 $(document).ready(function(){
   // $("#datepicker").datepicker({showOn: 'button', buttonImage: Drupal.settings.chart.module_path+'/images/calendar.gif', buttonImageOnly: true});
    get_project();
    $("#person_list").change(function() {
	$(".process_bar").show();
	draw_chart();
	draw_glucose_chart();
	draw_bloodpresure_chart();
    });
	
		var parm=getUrlVars()['project'];	
			if(parm){
				//alert(parm);
				get_person();
			}
});
  function checkInput(input)
  {

    re = /^\d{1,2}\-\d{1,2}\-\d{4}$/;
    if(input.value != '' && !input.value.match(re)) {
		$("#error_message").html("Check the date");
		return false;
    }else{
		$("#error_message").html("&nbsp;");
		draw_chart(); draw_bloodpresure_chart();draw_glucose_chart();
	$('.process_bar').show();
	}
    return true;
  } 
       
 function update_input_date(value){
		if(value=="all"){
		var from_date="01-06-2011";
		var to_date=showdate(0);
		}else{
		var from_date=showdate(value);
		var to_date=showdate(0);
		}
		$("#to_date").val(to_date);
		$("#from_date").val(from_date);
		
		$(".process_bar").show();
		draw_bloodpresure_chart();
		draw_chart();
		draw_glucose_chart();
 } 
function showdate(n) 
{ 
var uom = new Date(new Date()-0+n*86400000); 
uom = uom.getDate()+ "-" + (uom.getMonth()+1)+'-'+uom.getFullYear(); 
uom=uom.replace(/\b(\w)\b/g, '0$1');
return uom; 
} 
   
function remove_loader() {       
      }
function draw_glucose_chart(){

	$('#glucose').hide();
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				});
    var module_url = Drupal.settings.chart.module_path;
    var perid=document.getElementById('person_list').value;
    var proid=document.getElementById('project_list').value;
    var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var start_utc=eval("Date.UTC("+start_arr[2]+','+(parseInt(start_arr[1],10)-1)+','+start_arr[0]+')');
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
	var end_utc=eval("Date.UTC("+end_arr[2]+','+(parseInt(end_arr[1],10)-1)+','+end_arr[0]+')');
    var url=Drupal.settings.chart.handle_glucose_data+"?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
    $.getJSON(url, function(data1) {	

					var options = {

				chart: {
					renderTo: 'glucose',
					defaultSeriesType: 'scatter',
					zoomType:'x'
				},
				title: {
					text: 'Glucose'
				},
				credits:{
						enabled:false
					},
				xAxis: {
					//categories: []
					title:{
						text:''
					},
					type: 'datetime',
					startOnTick: true,
					min: start_utc,
					max: end_utc
				},
					tooltip:{
						shared:true,
						crosshairs: true
					},
					yAxis: {
						title: {
							text: 'Value (mg/dl)'
							
						},					
						plotLines: [{
							value: 0,
							width: 1,
							color: '#808080'
						}],
						min: 0,
						minorGridLineWidth: 0, 
						gridLineWidth: 1,
						alternateGridColor: null
					},
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b %H:%M ', this.x) +'<br/> '+ 
											'Data: '+this.y +' mg/dl ',
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
								return Highcharts.dateFormat('%e. %b %H:%M ', this.x) +"<br> "+ this.y;
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
				$('#glucose').show();
				$('.process_bar').hide();
});
//});
}	  
function draw_chart(){
	$('#shadow-container').hide();

	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				}); 
    var module_url = Drupal.settings.chart.module_path;
    var perid=document.getElementById('person_list').value;
    var proid=document.getElementById('project_list').value;
    var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var start_utc=eval("Date.UTC("+start_arr[2]+','+(parseInt(start_arr[1],10)-1)+','+start_arr[0]+')');
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
	var end_utc=eval("Date.UTC("+end_arr[2]+','+(parseInt(end_arr[1],10)-1)+','+end_arr[0]+')');
    var url=Drupal.settings.chart.handle_heart_beat_data+"?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
    $.getJSON(url, function(data1) {	
					var options = {

				chart: {
					renderTo: 'container',
					defaultSeriesType: 'scatter',
					zoomType:'x'
				},
				title: {
					text: 'Heart beat'
				},
				credits:{
						enabled:false
					},
				xAxis: {
					//categories: []
					title:{
						text:''
					},
					type: 'datetime',
					startOnTick: true,
					min: start_utc,
					max: end_utc
				},
					tooltip:{
						shared:true,
						crosshairs: true
					},
					yAxis: [{
						title: {
							text: 'Value (bpm)'
							
						},
						max:250,
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
							to: 300,
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b %H:%M', this.x) +'<br/> '+ 
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
								return Highcharts.dateFormat('%e. %b %H:%M ', this.x) +"<br> "+ this.y;
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
				remove_loader();
				$('#shadow-container').show();
				$('.process_bar').hide();
});
//});
}
function draw_bloodpresure_chart(){

	$('#bloodpresure').hide();

	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				}); 
    var module_url = Drupal.settings.chart.module_path;
    var perid=document.getElementById('person_list').value;
    var proid=document.getElementById('project_list').value;
	var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var start_utc=eval("Date.UTC("+start_arr[2]+','+(parseInt(start_arr[1],10)-1)+','+start_arr[0]+')');
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
	var end_utc=eval("Date.UTC("+end_arr[2]+','+(parseInt(end_arr[1],10)-1)+','+end_arr[0]+')');
    var url=Drupal.settings.chart.handle_blood_pressure_data+'?type=3&proid='+proid+'&perid='+perid+'&end='+end+'&start='+start;
    $.getJSON(url, function(data1) {
					var temp=data1["observations"][0]['records'];
					var length=temp ? temp.length : 0;
					
					var x;var i=0;
					var dia_str="";var sys_str="";
					for(i=0;i<length;i++){
							var obj= temp[i];
							for(x in obj)
								if(x=="diastolic"){
									dia_str=dia_str+ "["+obj[x][0]+","+obj[x][1]+"],";
								}else{
									sys_str=sys_str+ "["+obj[x][0]+","+obj[x][1]+"],";
									}
										}
					dia_str=dia_str.substr(0,dia_str.length-1);					
					dia_str="["+dia_str+"]";
					var dia_str=JSON.parse(dia_str);
					sys_str=sys_str.substr(0,sys_str.length-1);					
					sys_str="["+sys_str+"]";
					var sys_str=JSON.parse(sys_str);
				var options = {

				chart: {
					renderTo: 'bloodpresure',
					defaultSeriesType: 'scatter',
					zoomType:'x'
				},
				title: {
					text: 'Blood pressure'
				},
				credits:{
						enabled:false
					},
				xAxis: {
					//categories: []
					title:{
						text:''
					},
					type: 'datetime',
					startOnTick: true,
					min: start_utc,
					max: end_utc

				},
					tooltip:{
						shared:true,
						crosshairs: true
					},
					yAxis: [{
						title: {
							text: 'Value (mmHg)'
							
						},
						min: 0,
						minorGridLineWidth: 0, 
						gridLineWidth: 1,
						alternateGridColor: null
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b %H:%M ', this.x) +'<br/> '+ 
											'Data: '+this.y +' mmHg',
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
								return Highcharts.dateFormat('%e. %b %H:%M', this.x) +"<br> "+ this.y;
						}
						
				},

				series: [
						 {
							data:dia_str,
							name:'Diastolic'
						 },{
							data:sys_str,
							name:'Systolic'
						 }
						 ]

			};	
				//alert(data1.observations[0].records);
				var chart = new Highcharts.Chart(options);
				$('#bloodpresure').show();
				$('.process_bar').hide();
});
//});
}
function getUrlVars() {
    var vars = {};
    var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi, function(m,key,value) {
        vars[key] = value;
    });
    return vars;
}
function get_project(){
   $.getJSON(Drupal.settings.chart.getprojects,function(results){
			var parm=getUrlVars()['project'];	
			if(parm)
				var outputs='';
			else	
				var outputs='<option selected="selected">---Choose project---</option>';
		for(x in results){
			if(parm){
				if(results[x]['nid']==parm)
					outputs+="<option selected='selected' value='"+results[x]['nid']+"'>"+results[x]['title']+"</option>";
				else
					outputs+="<option value='"+results[x]['nid']+"'>"+results[x]['title']+"</option>";	
			}else
					outputs+="<option value='"+results[x]['nid']+"'>"+results[x]['title']+"</option>";
			}
		$('#project_list').html(outputs);
		});
}

var first_time = true;

function get_person(){
    
	var parm=getUrlVars()['project'];
	var project_id;	
			if(parm && first_time) {
				project_id=parm;
				first_time = false;
			} else {
				project_id = $('select#project_list option:selected').val();
			}
    
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
    htm = htm+"</table>";
    $('#bloodpresure').html(htm);
	remove_loader();
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
  
var init = function (id) {
    return "string" == typeof id ? document.getElementById(id) : id;
};

var Class = {
  create: function() {
    return function() {
      this.initialize.apply(this, arguments);
    }
  }
}

var Extend = function(destination, source) {
    for (var property in source) {
        destination[property] = source[property];
    }
    return destination;
}
