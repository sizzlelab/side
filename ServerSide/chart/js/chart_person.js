var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

$(document).ready(function() {
    get_project();
    $("#project_list").change(function() {
	$("#bloodpresure_loader").css('display','block');
	//$("#glucose_loader").css('display','block');
	$("#chart_loader").css('display','block');
	
			
	//draw_chart();
    });	
	var parm=getUrlVars()['project'];	
			if(parm){
				//alert(parm);
				draw_chart(); 
				draw_bloodpresure_chart();
				draw_glucose_chart();
			}
});
function draw_glucose_chart(){
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				});
    var module_url = Drupal.settings.chart.module_path;
    var perid=Drupal.settings.chart.current_user;
    var proid=document.getElementById('project_list').value;
    var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
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
					type: 'datetime'
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
				remove_loader();
});
//});
}  
function remove_loader() {       
         $('.process_bar').css('display','none');
      }
 function update_input_date(value){
   var start_date=document.getElementById('from_date').value;
   var date_arr=start_date.split("-",3);
   var date_month=date_arr[1];
   var date_day=date_arr[0];
   var date_year=date_arr[2];
   var d=new Date();
   var current_day=d.getDate();
   var current_month=d.getMonth()+1;
   var current_year= d.getUTCFullYear();
    
   switch (value){
      case 1:
         if (date_month>11){
            date_year=parseInt(date_year)+1;
            date_month="01";
         }else{
            date_month=parseInt(date_month,10)+1;}
          date_month=validate_month(date_month);  
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_bloodpresure_chart();
		  draw_chart();
		  draw_glucose_chart();
          break;
    
     case 3:
         if (date_month>9){
            date_month=parseInt(date_month,10)+3-12;
            date_year=parseInt(date_year)+1;
         }else{
            date_month=parseInt(date_month,10)+3;}
          date_month=validate_month(date_month);
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_bloodpresure_chart();
		  draw_chart();
		  draw_glucose_chart();
          break;     
     
     case 6:
          if (date_month>6){
            date_month=parseInt(date_month,10)+6-12;
            date_year=parseInt(date_year)+1;
         }else{
            date_month=parseInt(date_month,10)+6;}
          date_month=validate_month(date_month,10);  
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_bloodpresure_chart();
		  draw_chart();
		  draw_glucose_chart();
          break; 
          
     case 12:        
          date_year=parseInt(date_year)+1
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_bloodpresure_chart();
		  draw_chart();
		  draw_glucose_chart();
          break;
          
      case 13:
          var date_string= "01-06-2011";
          $("#from_date").val(date_string);
          
          current_month=validate_month(current_month);
          var date_string= current_day+"-"+current_month+"-"+current_year;
          $("#to_date").val(date_string);
		  draw_bloodpresure_chart();
		  draw_chart();
		  draw_glucose_chart();
          break;
   }         
   }
  function validate_month(number){
      switch(number)
		{
			case 1:number="01";break;case 2:number="02";break;case 3:number="03";break;
			case 4:number="04";break;case 5:number="05";break;case 6:number="06";break;
			case 7:number="07";break;case 8:number="08";break;case 9:number="09";break;
  }
   return number;
   }
function draw_chart(){
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				});

    //draw_tables();
    var module_url = Drupal.settings.chart.module_path;
    var perid=Drupal.settings.chart.current_user;
    var proid=document.getElementById('project_list').value;
    var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
    var url=Drupal.settings.chart.handle_heart_beat_data+"?start="+start+'&end='+end+'&perid='+perid+'&proid='+proid;
    $.getJSON(url, function(data1) {
	//var test=data1.observations[0].records;			
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b %H:%M ', this.x) +'<br/> '+ 
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
});
//});

}

function draw_bloodpresure_chart(){
	//draw_tables();
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				}); 
    var module_url = Drupal.settings.chart.module_path;
	var perid=Drupal.settings.chart.current_user;
    var proid=document.getElementById('project_list').value;
	var start=document.getElementById('from_date').value;
	var end=document.getElementById('to_date').value;
    var start_arr=new Array();
    start_arr=start.split('-');
    start=start_arr[2]+'-'+start_arr[1]+'-'+start_arr[0];
	var end_arr=new Array();
    end_arr=end.split('-');
    end=end_arr[2]+'-'+end_arr[1]+'-'+end_arr[0];
   // var start_str=document.getElementById('datepicker').value;
    //var start_arr=new Array();
    //start_arr=start_str.split('/');
    //var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
    //var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
	//$.getJSON(Drupal.settings.chart.getpersondata+'?type=3&proid='+proid+'&end='+end+'&start='+start,function(results){
    var url=Drupal.settings.chart.handle_blood_pressure_data+'?type=3&proid='+proid+'&end='+end+'&start='+start+'&perid='+perid;
    $.getJSON(url, function(data1) {
					var temp=data1["observations"][0]['records'];
					//alert (obj["observations"][0]['records'][3]['systolic'][1]);//Date.UTC(2011, 10, 6, 13
					var length=temp.length;var x;var i=0;
					//var dia_arr=new Array();var sys_arr=new Array();
					var dia_str="";var sys_str="";
					for(i=0;i<length;i++){
							var obj= temp[i];
							for(x in obj)
								//document.write(obj[x]);  Date.UTC(2011, 11, 8, 12, 14),146
								if(x=="diastolic"){
									//dia_arr[obj[x][0]]=obj[x][1];
									dia_str=dia_str+ "["+obj[x][0]+","+obj[x][1]+"],";
								}else{
									//sys_arr[obj[x][0]]=obj[x][1];
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
					type: 'datetime'
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
				remove_loader();
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
    $.getJSON(Drupal.settings.chart.getpersonprojects,function(results){
			var parm=getUrlVars()['project'];	
			if(parm)
				var outputs='';
			else	
				var outputs='<option selected="selected">---Choose project---</option>';
		for(x in results){
			if(parm){
				if(results[x]['id']==parm)
					outputs+="<option selected='selected' value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";
				else
					outputs+="<option value='"+results[x]['id']+"'>"+results[x]['name']+"</option>";	
			}else
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
		remove_loader();
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