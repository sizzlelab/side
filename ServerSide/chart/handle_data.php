<?php
$start=$_GET['start'];$perid=$_GET['perid'];$proid=$_GET['proid'];$end=$_GET['end'];
	$time_array=array();
	$time_array=explode("-",$start);
	$time_string="[Date.UTC(".$time_array[0].','.$time_array[1].','.$time_array[2].'),null]]';
//[Date.UTC(2011, 10, 11, 8, 13),69]
	//$time_stamp=
	//heart beat rate
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=0&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	//$file=str_ireplace('"','',$file);
	$first_part=substr($file,0,65);
	$second_part=substr($file,65);
	$first_part=str_ireplace('observations','observation1',$first_part);
	$second_part=str_ireplace('"','',$second_part);
	$result=$first_part.$second_part;
	$result=substr($result, 0, -1) ;

	//glucose
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=2&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	$glucose_first_part=substr($file,0,54);
	$glucose_first_part=substr( $glucose_first_part, 1 );//delete the first character '{'
	$glucose_first_part=str_ireplace('observations','observation2',$glucose_first_part);
	$glucose_second_part=substr($file,54);
	$glucose_second_part=str_ireplace('"','',$glucose_second_part);
	$glucose_result=$glucose_first_part.$glucose_second_part;
	//$position=strpos($glucose_result,'null');
	echo $result.','.$glucose_result;

?>