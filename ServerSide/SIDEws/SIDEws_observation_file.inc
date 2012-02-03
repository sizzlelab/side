<?php
function SIDEws_observation_file_GET($arg) {
    $step = $_GET['step'];
    //user_is_anonymous()
    if (user_is_logged_in()) {
        global $user;
        if ($step=='new') {
            $one_time_upload_id = generate_upload_id();
            if (!isset($_SESSION['one_time_upload_id'])) {
                session_register("one_time_upload_id");
            }
            $_SESSION['one_time_upload_id'] = $one_time_upload_id;
            $response = array(
                'oneTimeUploadId' => $one_time_upload_id,
            );
        }
        elseif ($step=='check') {
            $one_time_upload_id = $_GET['oneTimeUploadId'];
            $sql = 'SELECT * FROM {file_upload_records} WHERE one_time_upload_ID = "%s"';
            $file_upload_array = db_fetch_array(db_query($sql, $one_time_upload_id));
            if ($file_upload_array!=null) {
                $response = array(
                    'status' => $file_upload_array['status'],
                );
            }else {
                $response_code = "404";
                $message = array(
                    'code' => '0005',
                    'msg' => 'Can¡¯t find the upload record, you need to upload again!',
                );
                return response_error ();
            }
        }
        return array(
            'response_code' => '200', // OK
            'headers' => array(),
            'body' => json_encode($response),
            'media_type' => 'text/plain',
            'charset' => 'utf-8',
        );
    }else {
        $response_code = "403";
        $message = array(
            'code' => '0004',
            'msg' => 'This action requires login first!',
        );
        return response_error ($response_code, $message);
    }
}

function generate_upload_id() {
    $chars = 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890';
    for ($i=0;$i<9;$i++) {
        $one_time_upload_id = $one_time_upload_id . $chars[ mt_rand(0, strlen($chars)-1)];
    }
    return $one_time_upload_id;
}

function SIDEws_observation_file_POST($arg) {
    $one_time_upload_id = $_POST['oneTimeUploadId'];
    if (user_is_logged_in()) {
        global $user;
        $query = 'SELECT sid from side_sessions WHERE uid="'.$user->uid.'"';
        $row = @mysql_fetch_assoc(@mysql_query($query));
        $sid = $row['sid'];
        if ($one_time_upload_id==$_SESSION['one_time_upload_id']) {
            if (isset($_FILES['file']) && !empty($_FILES['file']) && $_FILES['file']['error']==0) {
                $modulePath = drupal_get_path('module', 'SIDEws');
                $filepath = $modulePath."/uploadFile/";
                $name = $one_time_upload_id.".sqlite";
                $idproject_sql = 'SELECT * FROM {project_code} WHERE project_code="%s"';
                $project_code_obj = db_fetch_object(db_query($idproject_sql,$_SESSION['projectCode']));
                $idproject = $project_code_obj->idproject;
	                $idmobile_sql = 'SELECT * FROM {mobile} WHERE mode="%s"';
	                $mobile_obj = db_fetch_object(db_query($idmobile_sql,$_SESSION['mobileID']));
	                $mobileID = $mobile_obj->idmobile;
                //$name = $_FILES['file']['name'];
                if (move_uploaded_file($_FILES['file']['tmp_name'],$filepath.$name)) {
                    $sql = 'INSERT INTO {file_upload_records} (upload_time,idmobile,idsession,file_name,one_time_upload_id,idperson,idproject,status) VALUES(now(),%d,"%s","%s","%s",%d,%d,"uploaded")';
                    if (db_query($sql,$mobileID,$sid,$name,$one_time_upload_id,$user->uid,$idproject)) {
                        $response = array(
                            'status' => 'File has been uploaded.',
                        );
                        session_destroy();
                        return array(
                            'response_code' => '200', // OK
                            'headers' => array(),
                            'body' => json_encode($response),
                            'media_type' => 'text/plain',
                            'charset' => 'utf-8',
                        );
                    }
                }else {
                    $response_code = "409";
                    $message = array(
                        'code' => '0006',
                        'msg' => 'Moving file is failed.',
                    );
                    return response_error ($response_code, $message);
                }
            }
            else {
                $response_code = "409";
                $message = array(
                    'code' => '0007',
                    'msg' => 'File is empty.',
                );
                return response_error ($response_code, $message);
            }
        }
        else {
            $response_code = "409";
            $message = array(
                'code' => '0008',
                'msg' => 'Your upload id is expired.',
            );
            return response_error ($response_code, $message);
        }
    }
    else {
        $response_code = "403";
        $message = array(
            'code' => '0004',
            'msg' => 'This action requires login first!',
        );
        return response_error ($response_code, $message);
    }
}

?>
