<div style="text-align:center;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">
	<select id="project_list" name="project" onchange='draw_chart(); draw_bloodpresure_chart();draw_glucose_chart()' >
	</select>
</div>

<!---calendar div------------------------------------------------>
<div> 
<p id="date_choice" >
	<span ><button style="cursor:pointer;" type="button" onclick="update_input_date(-7)">1w</button> </span>
	<span ><button style="cursor:pointer;" type="button" onclick="update_input_date(-30)">1m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(-90)">3m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(-180)">6m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(-365)">1y</button> </span>


<span id="date_form" style="float:right">
<input id="error_message" type="text"  size="17px" style="color:red" value="" />
From: <input style="border:1px solid gray;" onchange='checkInput(this);' id="from_date" type="text" size="10" name="start_date" value='<?php echo date("d-m-Y", mktime(0, 0, 0, date("m")-3, date("d"),   date("Y")));?>'/>
To: <input style="border:1px solid gray;" onchange='checkInput(this);' id="to_date" type="text" size="10" name="end_date" value='<?php echo date("d-m-Y");?>'/>
</span>
</p>
</div>
<!---calendar div------------------------------------------------>
<!---Blood presure chart------------------------------------------------>
<div style"=margin:20px;" id="bloodpresure">
</div>

<div style="display:none" class="process_bar">
    <div align="center">Loading data... </div>
    <div align="center"><img border='0' src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>loading1.gif' alt="Loading"/></div>
</div>
<!---Heart beat chart------------------------------------------------>
<div id="chart_loader" style="display:none" class="process_bar">
    <div align="center">Loading data... </div>
    <div align="center"><img border='0' src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>loading2.gif' alt="Loading"/></div>
</div>
<div id="shadow-container"> 
	<div class="shadow1"> 
		<div class="shadow2"> 
			<div class="shadow3"> 
			<!-- 3. Add the container --> 
				<div id="container" ></div> 
			</div>
		</div>
	</div>
</div>
<br/>
<div style"=margin:20px;" id="glucose">
</div>
<br/><br/>
