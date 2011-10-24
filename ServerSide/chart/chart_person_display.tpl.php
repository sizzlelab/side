<form action="" method="post" name="chart_form">
	<input type="hidden" id="moduleUrl" value="<?=drupal_get_path('module', 'chart'); ?>" />
	<div style="text-align:left;margin-bottom:30px;background-color:#E1E8F0;font-size:20px">
		<select id="project_list" name="project"><option  selected='selected' value="--Choose project--"  >--Choose project--</option></select>
	</div>
	<div  style="text-align:center;font-size:15px">
		<span style="position: relative;left:-60px" onclick='date_change_prev()'> << Previous day</span>
		<input type="text" id="datepicker" name="date" onchange='draw_chart()' value='<?=date('m/d/Y');?>' style="background: yellow; margin:0 auto">
		<span id="next_day" style="position: relative;right:-60px" onclick='date_change_next()'>Next day >></span>
	</div>
</form>
<div id="shadow-container"> 
	<div class="shadow1"> 

		<div class="shadow2"> 

			<div class="shadow3"> 
				<!-- 3. Add the container --> 
				<div id="container" >
				</div> 
			</div>
		</div>
	</div>
</div>

<script type="text/javascript">
	$(function() {
		$("#datepicker").datepicker({showOn: 'button', buttonImage: '<?=drupal_get_path('module', 'chart'); ?>/images/calendar.gif', buttonImageOnly: true});
	});
	
</script>	

