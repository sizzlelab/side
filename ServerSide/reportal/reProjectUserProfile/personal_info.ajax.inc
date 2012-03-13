<?php
/**
 * Menu callback for user/%user/ajax/personal_info
 */
function user_ajax_personal_info_callback($user){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    
    $return = new stdClass();
    
    if(empty ($user->profile_info->firstname)&& empty ($user->profile_info->lastname)){
        $return->name = t('Anonymous');
    }else{
        $return->name = $user->profile_info->firstname.' '.$user->profile_info->lastname;
    }
    $return->mail = empty ($user->mail)?t('Unknown'):$user->mail;
    $return->phone = empty($user->profile_info->phone)?t('Unknown'):$user->profile_info->phone;
    $return->picture = empty($user->picture)?variable_get('user_picture_default',''):$user->picture;
    $return->profile_url = url('user/'.$user->id);
    //print_r($user);
    print(json_encode($return));
}