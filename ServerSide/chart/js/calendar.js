		function date_change_next(){
				var current_date=document.getElementById('datepicker').value;
				
				//var next_day=current_date_arr[2]+'-'+current_date_arr[0]+'-'+(parseInt(current_date_arr[1],10)+1);				
				var current_date_arr=new Array();
				 current_date_arr=current_date.split('/');
				//var next_day=current_date_arr[0]+'/'+(parseInt(current_date_arr[1],10)+1)+'/'+current_date_arr[2];
				//ÔÂ£¬ÈÕ£¬Äê 4,6,9,11
				var month=current_date_arr[0];var day=current_date_arr[1];var year=current_date_arr[2];
				var next_day=parseInt( day,10)+1;
				if(next_day>28 && month==2){
					next_day=1;
					month=parseInt( month,10)+1;
				}else if(next_day>30 && (month==4||month==6||month==9||month==11)){
				
					next_day=1;
					month=parseInt( month,10)+1;
				}else if(next_day>31 && month==12){
					year=parseInt( year,10)+1;
					next_day=1;
					month=1;
				}else if (next_day>31){
					next_day=1;
					month=parseInt( month,10)+1;
				}
	
				//var date_obj=new Date(current_date_arr[2],current_date_arr[0],(parseInt(current_date_arr[1],10)));
				document.getElementById('datepicker').value=month+'/'+next_day+'/'+year;
				

				draw_blood_preasure_table();
				draw_chart();
				
		}
		function date_change_prev(){
				var current_date=document.getElementById('datepicker').value;
				
				//var next_day=current_date_arr[2]+'-'+current_date_arr[0]+'-'+(parseInt(current_date_arr[1],10)+1);				
				var current_date_arr=new Array();
				 current_date_arr=current_date.split('/');
				//var next_day=current_date_arr[0]+'/'+(parseInt(current_date_arr[1],10)+1)+'/'+current_date_arr[2];
				var month=current_date_arr[0];var day=current_date_arr[1];var year=current_date_arr[2];
				var prev_day=parseInt( day,10)-1;
	
				if(prev_day<1 && month==2){
					prev_day=31;
					month=parseInt( month,10)-1;
				}else if(prev_day<1 && (month==4||month==6||month==9||month==11)){
				
					prev_day=31;
					month=parseInt( month,10)-1;
				}else if(prev_day<1 && month==1){
					year=parseInt( year,10)-1;
					prev_day=31;
					month=12;
				}else if(prev_day<1){
					prev_day=30;
					month=parseInt( month,10)-1;
				}
	
				//var date_obj=new Date(current_date_arr[2],current_date_arr[0],(parseInt(current_date_arr[1],10)));
				document.getElementById('datepicker').value=month+'/'+prev_day+'/'+year;
				draw_blood_preasure_table();
				draw_chart();
		}
		
		