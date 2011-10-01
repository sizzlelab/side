<?php
	$start=$_GET['start'];$perid=$_GET['perid'];$proid=$_GET['proid'];$end=$_GET['end'];
	$url="http://jimu.cs.hut.fi/side/researcher/observations/data/json?type=2&start=".$start."&end=".$end."&perid=".$perid."&proid=".$proid;
	$file = file_get_contents($url, true);
	//$file=str_ireplace('"','',$file);
	$first_part=substr($file,0,65);
	$second_part=substr($file,65);
	$second_part=str_ireplace('"','',$second_part);
	$result=$first_part.$second_part;
	echo $result;		

?>