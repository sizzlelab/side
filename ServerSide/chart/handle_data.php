<?php
$start=$_GET['start'];$perid=$_GET['perid'];$proid=$_GET['proid'];$end=$_GET['end'];
	$time_array=array();
	$time_array=explode("-",$start);
	$time_string="[Date.UTC(".$time_array[0].','.$time_array[1].','.$time_array[2].'),null]';
//[Date.UTC(2011, 10, 11, 8, 13),69]
	$time_stamp=
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
	$position=strpos($glucose_result,'null');
	if(position){
		$glucose_string='"records":'.$time_string;
		$glucose_result=str_ireplace('"records":null',$glucose_string,$glucose_result);
	}
	$glucose_result=substr($glucose_result, 0, -1) ;



	//blood pressure
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=3&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	$blood_pressure_first_part=substr($file,0,74);
	$blood_pressure_second_part=substr($file,74);
	$blood_pressure_first_part=str_ireplace('observations','observation3',$blood_pressure_first_part);
	$blood_pressure_first_part=substr( $blood_pressure_first_part, 1 );//delete the first character '{'
	$blood_pressure_second_part=str_ireplace('"','',$blood_pressure_second_part);
	//check if there is diastolic or systolic
	$position=strpos($blood_pressure_second_part,'systolic');
	if($position){
			$blood_pressure_second_part=str_ireplace('systolic','"systolic"',$blood_pressure_second_part);
	}else{
			$blood_pressure_array='"records":{"diastolic":'.$time_string.',"systolic":'.$time_string.'}';
			$blood_pressure_first_part=str_ireplace('"records":null',$blood_pressure_array,$blood_pressure_first_part);
	}

	$blood_pressure_result=$blood_pressure_first_part.$blood_pressure_second_part;
	$finally_result=$result.','.$glucose_result.','.$blood_pressure_result;
	echo $finally_result;

?>