var chart;
hs.graphicsDir = 'http://highslide.com/highslide/graphics/';

 $(document).ready(function(){
   // $("#datepicker").datepicker({showOn: 'button', buttonImage: Drupal.settings.chart.module_path+'/images/calendar.gif', buttonImageOnly: true});
    get_project();
	
    $("#datepicker_button").click(function () {
    $(".Calendar").toggle();

    });

    $("#person_list").change(function() {
	$("#bloodpresure_loader").css('display','block');
	$("#glucose_loader").css('display','block');
	$("#chart_loader").css('display','block');
	draw_chart();
    });
});
function initialize(){
	var perid=document.getElementById('person_list').value;
    var proid=document.getElementById('project_list').value;
	var start_str=document.getElementById('datepicker').value;
    var start_arr=new Array();
    start_arr=start_str.split('/');
    var start=start_arr[2]+'-'+start_arr[0]+'-00';
    var end=start_arr[2]+'-'+(parseInt(start_arr[0],10)+1)+'-00';
	var flag= new Array();
	//$.getJSON(Drupal.settings.chart.getdate+'?proid=14&perid=28&end=2011-12-00&start=2011-11-00',function(results){	
	
    $.getJSON(Drupal.settings.chart.getdate+'?proid='+proid+'&perid='+perid+'&end='+end+'&start='+start,function(results){	
	for(x in results){
	    flag[x]=results[x]['DAYOFMONTH(time)'];
	}
	
    })

	
	//var flag = [10,15,20];
 cale = new Calendar("idCalendar", {
	SelectDay: new Date().setDate(10),
	FirstDay:flag[0],
	onSelectDay: function(o){ o.className = "onSelect"; },
	onToday: function(o){ o.className = "onToday"; },
	onFinish: function(){
		var month_text;
		switch(this.Month)
		{
			case 1:
			month_text="January";
			break;
			case 2:
			month_text="Feburay";
			break;
			case 3:
			month_text="March";
			break;
			case 4:
			month_text="April";
			break;
			case 5:
			month_text="May";
			break;
			case 6:
			month_text="June";
			break;
			case 7:
			month_text="July";
			break;
			case 8:
			month_text="August";
			break;
			case 9:
			month_text="September";
			break;
			case 10:
			month_text="October";
			break;
			case 11:
			month_text="November";
			break;
			case 12:
			month_text="December";
			break;
			
		}
		$("#idCalendarYear").html(this.Year); 
		$("#idCalendarMonth").html(month_text);
		
		for(var i = 0, len = flag.length; i < len; i++){
			var string='"'+this.Month+"/"+flag[i]+"/"+this.Year+'"';
			this.Days[flag[i]].innerHTML = "<a href='javascript:showData("+string+");'>" + flag[i] + "</a>";
		
		}
		
	}
});


$("#idCalendarPre").click(function(){ cale.PreMonth(); });
$("#idCalendarNext").click(function(){ cale.NextMonth(); });

}
function showData(data){
	$("#datepicker").val(data);
	//draw_chart();
	$(".Calendar").toggle();
}
function remove_loader() {       
         $('.process_bar').css('display','none');
         //targelem.style.display='none';
         //targelem.style.visibility='hidden';
      }
function draw_chart(){
	draw_tables();
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
    var start_str=document.getElementById('datepicker').value;
    var start_arr=new Array();
    start_arr=start_str.split('/');
    var start=start_arr[2]+'-'+start_arr[0]+'-'+start_arr[1];
    var end=start_arr[2]+'-'+start_arr[0]+'-'+(parseInt(start_arr[1],10)+1);
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
	this.PreDraw(new Date(this.Year, this.Month - 2, 1));
  },
  //下一月
  NextMonth: function() {
	this.PreDraw(new Date(this.Year, this.Month, 1));
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
	this.Year = date.getFullYear(); this.Month = date.getMonth() + 1;
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