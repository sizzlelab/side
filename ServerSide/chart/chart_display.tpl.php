
<div style="text-align:center;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" onchange='get_person()' name="project">
		<option  selected='selected' value="--Choose project--"  >--Choose project--</option>
	</select>
	<select id="person_list" onchange='draw_blood_preasure_table()' name="person">
		<option  selected='selected' value="--Choose person--" >--Choose person--</option>
	</select>

</div>
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


<div  style="text-align:center;font-size:15px">
	<span style="position: relative;left:-60px" onclick='date_change_prev()'> << Previous day</span>
	<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?php echo date('m/d/Y');?>' style="background: yellow; margin:0 auto">
	<button id="datepicker_button">Show it</button>
	<div class="Calendar" style="display: none">
  <div id="idCalendarPre">&lt;&lt;</div>
  <div id="idCalendarNext">&gt;&gt;</div>
  <span id="idCalendarMonth"></span> &nbsp; <span id="idCalendarYear"></span>
  <table cellspacing="0">
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

	<span id="next_day" style="position: relative;right:-60px" onclick='date_change_next()'>Next day >></span>
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
