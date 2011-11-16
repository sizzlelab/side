<?php
$start=$_GET['start'];$perid=$_GET['perid'];$proid=$_GET['proid'];$end=$_GET['end'];
	$time_array=array();
	$time_array=explode("-",$start);
	$time_string="[Date.UTC(".$time_array[0].','.$time_array[1].','.$time_array[2].'),null]]';

	//glucose
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=2&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	$glucose_first_part=substr($file,0,54);
	$glucose_second_part=substr($file,54);
	$glucose_second_part=str_ireplace('"','',$glucose_second_part);
	$glucose_result=$glucose_first_part.$glucose_second_part;
	//$position=strpos($glucose_result,'null');
	echo $glucose_result;

?>