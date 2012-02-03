<?php
ini_set("display_errors","on");
$con = mysql_connect("localhost","miyula","miyula123");
if (!$con){
    die('Could not connect: ' . mysql_error());
}
mysql_select_db("production", $con);
$records_num = mysql_num_rows(mysql_query("SELECT * FROM file_upload_records"));
$sql = mysql_query("SELECT * FROM file_upload_records");
while ($file_upload_record = mysql_fetch_array($sql,MYSQL_ASSOC)) {
    if ($file_upload_record['status']=='uploaded') {
        $id = $file_upload_record['idrecord'];
        $fileName = $file_upload_record['file_name'];
        $mobileID = $file_upload_record['idmobile'];
        $personID = $file_upload_record['idperson'];
        $projectID = $file_upload_record['idproject'];
        transport_to_database($id,$fileName,$mobileID,$personID,$projectID);
    }
}

function transport_to_database($id,$fileName,$mobileID,$personID,$projectID) {
    mysql_query("UPDATE file_upload_records SET status = 'reading' WHERE idrecord = '$id'");
    $rSQLite = new PDO("sqlite:/var/www/getcheck/sites/all/modules/custom/SIDEws/uploadFile/".$fileName);
    $sql = "SELECT * FROM observations";
    $result = $rSQLite->query($sql);
    $result->setFetchMode(PDO::FETCH_ASSOC);
    foreach ($result as $each_result) {
        $type = $each_result['type'];
        $keyName = get_keyname($type);
        $time = $each_result['time'];
        $insert_observation_record_sql = "INSERT INTO observation_record (ido_type, idmobile, time, idperson,idproject) VALUES ('$type','$mobileID','$time','$personID','$projectID')";
        mysql_query($insert_observation_record_sql);
        $ido_record = mysql_insert_id();
        foreach ($each_result as $key=>$value) {
            if ($key=="observation1" && count($keyName)>=1) {
                $ido_keyname = $keyName[0];
                insert_record_value($ido_record,$ido_keyname,$value);
            }
            elseif ($key=="observation2" && count($keyName)>=2) {
                $ido_keyname = $keyName[1];
                insert_record_value($ido_record,$ido_keyname,$value);
            }
            elseif ($key=="observation3" && count($keyName)>=3) {
                $ido_keyname = $keyName[2];
                insert_record_value($ido_record,$ido_keyname,$value);
            }
        }
    }
    mysql_query("UPDATE file_upload_records SET status = 'recorded' WHERE idrecord = '$id'");
}

function get_keyname($type) {
    $sql = "SELECT ido_keyname FROM observation_keyname WHERE ido_type = '$type' order by ido_keyname asc";
    $result = array();
    $sql_result = mysql_query($sql);
    while ($one_result = mysql_fetch_row($sql_result)) {
        array_push($result,$one_result[0]);
    }
    return $result;
}

/*function get_id_keyname ($type) {
    $id = mysql_fetch_row(mysql_query("SELECT * FROM observation_keyname WHERE keyname='$type'"));
    return $id[0]; 
}*/

function insert_record_value($ido_record,$ido_keyname,$value) {
    mysql_query("INSERT INTO observation_record_value (ido_record,ido_keyname,value) VALUES ('$ido_record','$ido_keyname','$value')");
}
?>
