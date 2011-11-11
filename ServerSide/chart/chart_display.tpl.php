
<div style="text-align:center;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">

	<select id="project_list" onchange='get_person()' name="project">
		<option  selected='selected' value="--Choose project--"  >--Choose project--</option>
	</select>
	<select id="person_list" onchange='draw_blood_preasure_table()' name="person">
		<option  selected='selected' value="--Choose person--" >--Choose person--</option>
	</select>

</div>
<div style"=margin:20px;" id="bloodpresure">

</div>
<div  style="text-align:center;font-size:15px">
	<span style="position: relative;left:-60px" onclick='date_change_prev()'> << Previous day</span>
	<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?php echo date('m/d/Y');?>' style="background: yellow; margin:0 auto">
	<span id="next_day" style="position: relative;right:-60px" onclick='date_change_next()'>Next day >></span>
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
<br/><br/><br/>
