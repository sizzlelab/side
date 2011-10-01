<?php

/*******************************************************************************
 * Copyright (c) 2011 Aalto University and the authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * Authors:
 * XiaoHang Zou <zxhwd19871004@gmail.com>
 ******************************************************************************/

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
        if (check_projectCode($projectCode)) {
            session_register("projectCode");
            $_SESSION['projectCode'] = $projectCode;
            if (login_validate($username,$password)) {
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
        $sql = 'SELECT COUNT(*) FROM {project_code} WHERE project_code = %d';
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