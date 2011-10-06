<?php
	$start=$_GET['start'];$end=$_GET['end'];
	//http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=2&start=2011-3-15&end=2011-3-16&perid=6&proid=11	
	$str_beginning='{"observations":[{"id":"2","name":"Average Heart Beat","records":[';
	$arr_start=Array();
	$arr_start=explode('-',$start) ;
	$start_y=$arr_start[0];$start_m=$arr_start[1];$start_d=$arr_start[2];
	$string='[Date.UTC('.$start_y.','.$start_m.','.$start_d.',';
	for ($i=0;$i<30;$i++){
		$value=rand(50,110);
		$hour=rand(1,23);
		$min=rand(2,59);
		$re=$string.$hour.','.$min.'),'.$value.'],';
		$result.=$re;
}
$result=str_ireplace('"',"",$result);
$result = substr($result,0,-1);
$result=$str_beginning.$result."]}]}";
echo $result;
/*back up 

$start=$_GET['start'];$perid=$_GET['perid'];$proid=$_GET['proid'];$end=$_GET['end'];
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=2&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	//$file=str_ireplace('"','',$file);
	$first_part=substr($file,0,65);
	$second_part=substr($file,65);
	$second_part=str_ireplace('"','',$second_part);
	$result=$first_part.$second_part;
	echo $result;
*/

?>