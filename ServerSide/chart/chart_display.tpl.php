
<div style="text-align:center;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" onchange='get_person()' name="project">
		<option  selected='selected' value="--Choose project--"  >--Choose project--</option>
	</select>
	<select id="person_list"  onchange='draw_chart()' name="person">
		<option  selected='selected' value="--Choose person--" >--Choose person--</option>
	</select>

</div>
<!---calendar div------------------------------------------------>
<div> 
<p id="date_choice" >
	<span ><button style="cursor:pointer;" type="button" onclick="update_input_date(1)">1m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(3)">3m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(6)">6m</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(12)">1y</button> </span>
	<span><button style="cursor:pointer;" type="button" onclick="update_input_date(13)">All</button> </span>


<span id="date_form" style="float:right">
From: <input onchange='draw_chart()' id="from_date" type="text" size="10" name="start_date" value='<?php echo date("d-m-Y", mktime(0, 0, 0, date("m")-3, date("d"),   date("Y")));?>'/>
To: <input onchange='draw_chart()' id="to_date" type="text" size="10" name="end_date" value='<?php echo date("d-m-Y");?>'/>
</span>
</p>
</div>


<!--	First design of calendar
<div  style="text-align:center;font-size:15px">
	<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?php echo date('m/d/Y');?>' style="background: yellow; margin:0 auto">
	<input id="datepicker_button" type="image" src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>calendar.gif' name="image" >
<div class="Calendar" style="display: none">	
  <div >
  <span id="idCalendarPre">&lt;&lt;</span>
  <span id="idCalendarMonth"></span> &nbsp; <span id="idCalendarYear"></span>
  <span id="idCalendarNext">&gt;&gt;</span>
  </div>
  
  <table cellspacing="0" >
    <thead>
      <tr>
        <td>Sun</td>
        <td>Mon</td>
        <td>Tue</td>
        <td>Wed</td>
        <td>Thu</td>
        <td>Fri</td>
        <td>Sat</td>
      </tr>
    </thead>
    <tbody id="idCalendar">
    </tbody>
  </table>
</div>




</div>
-->
<div style"=margin:20px;" id="bloodpresure">
<div id="bloodpresure_loader" style="display:none" class="process_bar">
    <div align="center">Loading data... </div>
    <div align="center"><img border='0' src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>loading1.gif' alt="Loading"/></div>
</div>
</div>

<div style"=margin:20px;" id="glucose">
<div id="glucose_loader" style="display:none" class="process_bar">
    <div align="center">Loading data... </div>
    <div align="center"><img border='0' src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>loading1.gif' alt="Loading"/></div>
</div>
</div>


<div id="shadow-container"> 
<div id="chart_loader" style="display:none" class="process_bar">
    <div align="center">Loading data... </div>
    <div align="center"><img border='0' src='<?=base_path().drupal_get_path('module', 'chart').'/images/'?>loading2.gif' alt="Loading"/></div>
</div>
	<div class="shadow1"> 
		<div class="shadow2">
			<div class="shadow3"> 
			<!-- 3. Add the container --> 
				<div id="container" ></div> 
			</div>
		</div>
	</div>
</div>
<br/><br/><br/>
