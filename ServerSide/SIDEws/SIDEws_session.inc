<?php
function SIDEws_session_POST($arg) {
    global $user;
    $username = $_POST['username'];
    $projectCode = $_POST['projectCode'];
    $password = $_POST['password'];
    $mobileID = $_POST['mobileID'];
    //$username = "zou xiaohang";
    //$password = "19871004";
    //$mobileID = "1";
    if (!user_is_logged_in()) {
        if (login_validate($username,$projectCode,$password)) {
            check_mobileID($mobileID);
            session_register("mobileID");
            $_SESSION['mobileID'] = $mobileID;
            $sql = 'SELECT * FROM {sessions} WHERE uid = %d';
            $current_user_session_obj = db_fetch_array(db_query($sql,$user->uid));
            $response = array(
                'sessionID' => $current_user_session_obj['sid'],
            );
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
                'message' => 'Login failed!',
            );
            return response_error ($response_code, $message);
        }
    }else {
        $response_code = "403";
        $message = array(
            'message' => 'Login failed!',
        );
        return response_error ($response_code, $message);
    }
}

function login_validate($username,$projectCode,$password) {
    if (isset($username) && !empty($username)) {
        $sql = 'SELECT * FROM {users} WHERE LOWER(name) = LOWER("%s")';
        $user_obj = db_fetch_object(db_query($sql,$username));
        if ($user_obj!=null) {
            if (md5($password)==$user_obj->pass) {
                $account = user_load($user_obj->uid);
                user_external_login($account);
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
    elseif (isset($projectCode) && !empty($projectCode)) {
        $sql = 'SELECT * FROM {users} WHERE project_code = %d';
        $person_obj = db_fetch_object(db_query($sql,$projectCode));
        if ($person_obj!=null) {
            if (md5($password)==$person_obj->password) {
                $account = user_load($person_obj->uid);
                user_external_login($account);
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}

function check_mobileID($mobileID) {
    $sql = 'SELECT COUNT(*) FROM {mobile} WHERE idmobile = %d';
    if (db_result(db_query($sql,$mobileID))==0) {
        $new_mobile_sql = "INSERT INTO {mobile}(idmobile) VALUES(%d);";
        db_query($new_mobile_sql,$mobileID);
    }
}

/*function login_validate($username,$password) {
    $password = md5($password);
    $check_username_sql = 'SELECT * FROM {users} WHERE LOWER(name) = LOWER("%s")';
    $user_obj = db_result(db_query($check_username_sql,$username);
    if($user_obj!=null){
        if ($password==$user_obj->pass) {
            $account = user_load($user_obj->uid);
            user_external_login($account);
            return true;
        }else {
            return false;
        }
    }else {
        $new_user_obj = array(
            'name' => $username,
            'pass' => $password,
            'status' => 1,
        )
        $user = user_save($account,$new_user_obj);
        if(isset($user->uid)){
            $add_person_sql = "INSERT INTO {person}(uid,username,password) VALUES(%d,'%s');";
            if(db_query($add_person_sql,$username,$password)){
                $account = user_load($user->uid);
                user_external_login($account);
            }
        }
    }
}*/

function SIDEws_session_DELETE($arg) {
    if (user_is_logged_in()) {
        session_destroy();
        return array(
            'response_code' => '200', // OK
            'headers' => array(),
            'body' => '',
            'media_type' => 'text/plain',
            'charset' => 'utf-8',
        );
    }else {
        $response_code = "403";
        $message = array(
            'message' => 'This action requires login first!',
        );
        return response_error ($response_code, $message);
    }
}

function SIDEws_session_GET($arg) {
    if (user_is_logged_in()) {
        global $user;
        $sql = 'SELECT * FROM {sessions} WHERE uid = %d';
        $current_user_session_obj = db_fetch_array(db_query($sql,$user->uid));
        $response = array(
            'sessionID' => $current_user_session_obj['sid'],
            'username' => $user->name,
            'mobileID' => $_SESSION['mobileID'],
        );
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
            'message' => 'This action requires login first!',
        );
        return response_error ($response_code, $message);
    }
}

?>