var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

 $(document).ready(function(){
   // $("#datepicker").datepicker({showOn: 'button', buttonImage: Drupal.settings.chart.module_path+'/images/calendar.gif', buttonImageOnly: true});
    get_project();
    $("#person_list").change(function() {
	$("#bloodpresure_loader").css('display','block');
	$("#glucose_loader").css('display','block');
	$("#chart_loader").css('display','block');
	//draw_chart();
    });
});
 
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
		  draw_chart();
          break;
    
     case 3:
         if (date_month>10){
            date_month=parseInt(date_month,10)+3-12;
            date_year=parseInt(date_year)+1;
         }else{
            date_month=parseInt(date_month,10)+3;}
          date_month=validate_month(date_month);
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_chart();
		  draw_bloodpresure_chart();
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
		  draw_chart();
          break; 
          
     case 12:        
          date_year=parseInt(date_year)+1
          var date_string= date_day+"-"+date_month+"-"+date_year;
          $("#to_date").val(date_string);
		  draw_chart();
          break;
          
      case 13:
          var date_string= "01-06-2011";
          $("#from_date").val(date_string);
          
          current_month=validate_month(current_month);
          var date_string= current_day+"-"+current_month+"-"+current_year;
          $("#to_date").val(date_string);
		  draw_chart();
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
function remove_loader() {       
         $('.process_bar').css('display','none');
      }
function draw_chart(){
	//draw_bloodpresure_chart();
	Highcharts.setOptions({
    global: {
        useUTC: false
			}
				});
    //draw_blood_preasure_table();
    //document.chart_form.submit(); 
    var module_url = Drupal.settings.chart.module_path;
    //var data_path = module_url + "/<?php echo 'handle_data.php?type=2&start='.$date_now.'&proid='.$_POST['project'].'&perid='.$_POST['person'];?>";
    var perid=document.getElementById('person_list').value;
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b: %H:%M ', this.x) +'<br/> '+ 
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
								return Highcharts.dateFormat('%e. %b: %H:%M ', this.x) +"<br> "+ this.y;
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
    var perid=document.getElementById('person_list').value;
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
    var url=Drupal.settings.chart.handle_blood_pressure_data+'?type=3&proid='+proid+'&perid='+perid+'&end='+end+'&start='+start;
    $.getJSON(url, function(data1) {
					var temp=data1["observations"][0]['records'];
					//alert (obj["observations"][0]['records'][3]['systolic'][1]);//Date.UTC(2011, 10, 6, 13
					var length=temp.length;var x;var i=0;
					var dia_arr=new Array();
					var sys_arr=new Array();
					for(i=0;i<length;i++){
							var obj= temp[i];
							for(x in obj)
								//document.write(obj[x]);  Date.UTC(2011, 11, 8, 12, 14),146
								// document.write(x); diastolic
								if(x=="diastolic"){
									dia_arr[obj[x][0]]=obj[x][1];
								}else{
									sys_arr[obj[x][0]]=obj[x][1];
									}
										}
	
	/**
					 arr_systolic=new Array();
					 arr_diastolic=new Array();
					var obs=data1.observations[0]['records'];
					var systolic_time=obs[1]["systolic"][0];
					var systolic_value= obs[1]["systolic"][1];
					var systo="[["+systolic_time+","+systolic_value+"]]";
					var systo=JSON.parse(systo);
					
					var diastolic_time=obs[0]["diastolic"][0];
					var diastolic_value= obs[0]["diastolic"][1];
					var diasto="[["+diastolic_time+","+diastolic_value+"]]";
					var diasto=JSON.parse(diasto);
					/**
					var length=obs.length/2;
					for (var i=0,n=0,j=1;i<length;i++,j+2,n+2){
						//var j=i+2;
						var diastolic_time=obs[n]["diastolic"][0];
						var diastolic_value= obs[n]["diastolic"][1];
						arr_diastolic[diastolic_time]=diastolic_value;
						
						
						var systolic_time=obs[j]["systolic"][0];
						var systolic_value= obs[j]["systolic"][1];
						
						//var systo="[["+systolic_time+","+systolic_value+"]]"
						systo="[["+systolic_time+",33]]";
						
						arr_systolic[systolic_time]=systolic_value;
					}
					*/
				var options = {

				chart: {
					renderTo: 'bloodpresure',
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
										maincontentText: 'Time: '+Highcharts.dateFormat('%e. %b :%H:%M ', this.x) +'<br/> '+ 
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
								return Highcharts.dateFormat('%e. %b :%H:%M', this.x) +"<br> "+ this.y;
						}
						
				},

				series: [{
							data:sys_arr,
							name:'Systolic'
						 },
						 {
							data:dia_arr,
							name:'Diastolic'
						 }
						 ]

			};	
				//alert(data1.observations[0].records);
				var chart = new Highcharts.Chart(options);
				remove_loader();
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
		get_blood_presure($("#project_list option:selected").val(),$("#person_list option:selected").val(), start, end );
  }
    function draw_glucose_table(){
		var start_str=document.getElementById('datepicker').value;
		var start_arr=new Array();
		start_arr=start_str.split('/');
		var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
		var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
		get_glucose($("#project_list option:selected").val(), start, end );
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


var Calendar = Class.create();
Calendar.prototype = {
  initialize: function(container, options) {
	this.Container = init(container);//容器(table结构)
	this.Days = [];//日期对象列表
	
	this.SetOptions(options);
	this.FirstDay =this.options.FirstDay || new Date().getDay();
	this.Year = this.options.Year || new Date().getFullYear();
	this.Month = this.options.Month || new Date().getMonth() + 1;
	this.SelectDay = this.options.SelectDay ? new Date(this.options.SelectDay) : null;
	this.onSelectDay = this.options.onSelectDay;
	this.onToday = this.options.onToday;
	this.onFinish = this.options.onFinish;	
	
	this.Draw();
  },
  //设置默认属性
  SetOptions: function(options) {
	this.options = {//默认值
		Year:			0,//显示年
		Month:			0,//显示月
		FirstDay: null,
		SelectDay:		null,//选择日期
		onSelectDay:	function(){},//在选择日期触发
		onToday:		function(){},//在当天日期触发
		onFinish:		function(){}//日历画完后触发
	};
	Extend(this.options, options || {});
  },
  //当前月
  NowMonth: function() {
	this.PreDraw(new Date());
  },
  //上一月
  PreMonth: function() {
	this.PreDraw(new Date(this.Year, this.Month - 1, 1));
  },
  //下一月
  NextMonth: function() {
	this.PreDraw(new Date(this.Year, this.Month+1, 1));
  },
  //上一年
  PreYear: function() {
	this.PreDraw(new Date(this.Year - 1, this.Month - 1, 1));
  },
  //下一年
  NextYear: function() {
	this.PreDraw(new Date(this.Year + 1, this.Month - 1, 1));
  },
  //根据日期画日历
  PreDraw: function(date) {
	//再设置属性
	this.Year = date.getFullYear(); this.Month = date.getMonth() ;
	//重新画日历
	this.Draw();
  },
  //画日历
  Draw: function() {
	//用来保存日期列表
	var arr = [];
	//用当月第一天在一周中的日期值作为当月离第一天的天数
	for(var i = 1, firstDay = new Date(this.Year, this.Month - 1, 1).getDay(); i <= firstDay; i++){ arr.push(0); }
	//用当月最后一天在一个月中的日期值作为当月的天数
	for(var i = 1, monthDay = new Date(this.Year, this.Month, 0).getDate(); i <= monthDay; i++){ arr.push(i); }
	//清空原来的日期对象列表
	this.Days = [];
	//插入日期
	var frag = document.createDocumentFragment();
	while(arr.length){
		//每个星期插入一个tr
		var row = document.createElement("tr");
		//每个星期有7天
		for(var i = 1; i <= 7; i++){
			var cell = document.createElement("td"); cell.innerHTML = "&nbsp;";
			if(arr.length){
				var d = arr.shift();
				if(d){
					cell.innerHTML = d;
					this.Days[d] = cell;
					var on = new Date(this.Year, this.Month - 1, d);
					//判断是否今日
					this.IsSame(on, new Date()) && this.onToday(cell);
					//判断是否选择日期
					this.SelectDay && this.IsSame(on, this.SelectDay) && this.onSelectDay(cell);
				}
			}
			row.appendChild(cell);
		}
		frag.appendChild(row);
	}
	//先清空内容再插入(ie的table不能用innerHTML)
	while(this.Container.hasChildNodes()){ this.Container.removeChild(this.Container.firstChild); }
	this.Container.appendChild(frag);
	//附加程序
	this.onFinish();
  },
  //判断是否同一日
  IsSame: function(d1, d2) {
	return (d1.getFullYear() == d2.getFullYear() && d1.getMonth() == d2.getMonth() && d1.getDate() == d2.getDate());
  } 
}  