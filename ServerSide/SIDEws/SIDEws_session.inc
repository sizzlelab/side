<?php
function SIDEws_session_POST($arg) {
    $username = $_POST['username'];
    $projectCode = $_POST['projectCode'];
    $password = $_POST['password'];
    $mobileMode = $_POST['mobileID'];
    //$username = "zou xiaohang";
    //$password = "19871004";
    //$mobileID = "1";
    if (!user_is_logged_in()) {
        if (check_projectCode($projectCode)) {
            session_register("projectCode");
            $_SESSION['projectCode'] = $projectCode;
            if (login_validate($username,$password)) {
    		    global $user;
                check_mobileID($mobileMode);
                session_register("mobileID");
                $_SESSION['mobileID'] = $mobileMode;
                $getSession = GetSID(24,$user->uid);
                $response = array(
                    'sessionID' => $getSession,
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
                'message' => 'Project code is wrong!',
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

function check_projectCode($projectCode) {
    if (isset($projectCode) && !empty($projectCode)) {
        $sql = 'SELECT COUNT(*) FROM {project_code} WHERE project_code = "%s"';
        if (db_result(db_query($sql,$projectCode))==0) {
            return false;
        }else {
            return true;
        }
    }else {
        return false;
    }
}

function login_validate($username,$password) {
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
}

function check_mobileID($mobileMode) {
    $sql = 'SELECT COUNT(*) FROM {mobile} WHERE mode = "%s"';
    if (db_result(db_query($sql,$mobileMode))==0) {
        $new_mobile_sql = 'INSERT INTO {mobile}(mode) VALUES("%s");';
        db_query($new_mobile_sql,$mobileMode);
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
	    global $user;
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
        $sql = 'SELECT sid FROM side_sessions WHERE uid = "'.$user->uid.'" LIMIT 1';
        $row = @mysql_fetch_assoc(@mysql_query($sql));
        $response = array(
            'sessionID' => $row['sid'],
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

function GetSID ($nSize=24,$uid) {
    // Search the current uid
    if ($uid) {
        $query = 'SELECT * FROM side_sessions WHERE uid="'.$uid.'" LIMIT 1';
        $result = @mysql_query($query);
        $nums = @mysql_num_rows($result);
        if ($nums==0) {
            $sessionID = SetSID($nSize);
            $nowtime = time();
            $query = 'INSERT INTO side_sessions VALUES ("'.$uid.'","'.$sessionID.'","'.$nowtime.'")';
            @mysql_query($query);
        }else {
            $row = @mysql_fetch_assoc($result);
            $timestamp = $row['timestamp'];
            $cY = date("Y");
            $cM = date("M");
            $cD = date("D");
            $cH = date("H");
            $ci = date("i");
            $cs = date("s");
            $oY = date("Y",$timestamp);
            $oM = date("M",$timestamp);
            $oD = date("D",$timestamp);
            $oH = date("H",$timestamp);
            $oi = date("i",$timestamp);
            $os = date("s",$timestamp);
            if ($cY==$oY && $cM==$oM && $cD==$oD) {
                $timeDiff = ($cH*60*60+$ci*60+$cs)-($oH*60*60+$oi*60+$os);
                if ($timeDiff>(2*60*60)) {
                    $sessionID = SetSID($nSize);
                    $nowtime = time();
                    $query = 'DELETE FROM side_sessions WHERE uid="'.$uid.'"';
                    @mysql_query($query);
                    $query = 'INSERT INTO side_sessions VALUES ("'.$uid.'","'.$sessionID.'","'.$nowtime.'")';
                    @mysql_query($query);
                }else {
                    $sessionID = $row['sid'];
                }
            
            }else {
                $sessionID = SetSID($nSize);
                $nowtime = time();
                $query = 'DELETE FROM side_sessions WHERE uid="'.$uid.'"';
                @mysql_query($query);
                $query = 'INSERT INTO side_sessions VALUES ("'.$uid.'","'.$sessionID.'","'.$nowtime.'")';
                @mysql_query($query);
            }
        }
    }
    return $sessionID;
}
function SetSID ($nSize=24) {
    //Randomize new session.
    mt_srand ((double) microtime() * 1000000);
    for ($i=1; $i<=$nSize; $i++) {
        $nRandom = mt_rand(1,30);
        if ($nRandom <= 10) {
            // Uppercase letters
            $sessionID .= chr(mt_rand(65,90));
        } elseif ($nRandom <=20) {
            $session .= mt_rand(0,9);
        } else {
            // Lowercase letters
            $sessionID .= chr(mt_rand(97,122));
        }
    }   
   return $sessionID; 
}
?>
