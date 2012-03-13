<?php

/**
 * Menu callback for invite/%
 */
function invite_redirect_to_project_callback($code){
    if(!invitation_code_is_not_used($code)){
        $invite_message = '<font color="RED">Sorry, the invitation code does not exist or it has already been used!</font>';
    }else{
        $check = 'SELECT code, c.project AS project, c.email AS email, c.phone AS phone, c.person AS person, used, p.user AS user, p.role AS role, p.active AS active, u.name as username '
                .'FROM {research_projects_onetime_invite_code} AS c,  {research_projects_persons_list} AS p LEFT JOIN {users} AS u ON u.uid=p.user '
                .'WHERE c.code="%s" AND c.person=p.id';
        $result = db_fetch_object(db_query($check, $code));
        
        if($result){
            $project = load_project($result->project,'id');
            $invite_message = 'To accept the invitation to <strong>'.l($project->name, generate_project_url($project->path), array('attributes'=>array('target'=>'_blank')))."</strong> as a {$result->role}, please register/login below:";
    
            global $user;
        
            #if($user->uid && $result->email!=$user->mail){
                watchdog('user', 'Session closed for %name.', array('%name' => $user->name));

                // Destroy the current session:
                session_destroy();
                // Only variables can be passed by reference workaround.
                $null = NULL;
                user_module_invoke('logout', $null, $user);

                // Load the anonymous user
                $user = drupal_anonymous_user();
            #}
        
            if($user->uid && $user->status){
            
                if($result->active==1){
                    //change code to used
                    $set_code_used = 'UPDATE  {research_projects_onetime_invite_code} SET used=1, changed=NOW() WHERE code="%s"';
                    db_query($set_code_used, $code);
                    drupal_goto(get_project_url($result->project));
                    exit;
                }
            
                $person->exiting_user_join_form = drupal_get_form('exiting_user_join_project_form', $result);
                $person->username = $user->name;
            }elseif($result->user > 0){
                $person->login_to_join_form = drupal_get_form('login_to_join_project_from', $result);
            }else{
                $person->register_to_join_form = drupal_get_form('register_to_join_project_from', $result);
            }
        }else{
            $invite_message = '<font color="RED">Sorry, invalid invitation code.</font>';
        }
        
        
    }
    
    drupal_add_css(drupal_get_path('module','reProjectUser').'/css/invite_page.css');
    return theme('invite_join_project_page', $person, $invite_message);
}

/** Existing user join project team form**/
function exiting_user_join_project_form($form_state, $person){
    $form['project'] = array(
        '#type' => 'hidden',
        '#value' => $person->project,
    );
    $form['person'] = array(
        '#type' => 'hidden',
        '#value' => $person->person,
    );
    $form['code'] = array(
        '#type' => 'hidden',
        '#value' => $person->code,
    );
    $form['role'] = array(
        '#type' => 'hidden',
        '#value'=> $person->role,
    );
    $form['accept'] = array(
        '#type' => 'submit',
        '#value' =>t(' Yes, I accept. '),
        '#suffix' => '<font size=3><a href="javascript:self.close();">No, thanks.</a></font>',  
    );
    return $form;
}

function exiting_user_join_project_form_validate($form, &$form_state){
    
    global $user;
    if(!$user->uid||$user->status<>1){
        drupal_goto('user');    
    }

    $project = $form_state['values']['project'];
    $person = $form_state['values']['person'];
    $code = $form_state['values']['code'];
    
    $check = 'SELECT COUNT(*) FROM {research_projects_onetime_invite_code} AS c,  {research_projects_persons_list} AS p '
                  .'WHERE c.code="%s" AND c.used=0 AND c.project=%d AND c.person=%d AND c.project=p.project AND c.person=p.id';
    if(db_result(db_query($check, $code, $project, $person))==0){
        form_set_error('code', 'Invitation code and project do not match.');
    }
}

function exiting_user_join_project_form_submit($form, &$form_state){
    $project = $form_state['values']['project'];
    $person = $form_state['values']['person'];
    $code = $form_state['values']['code'];
    $role = $form_state['values']['role'];
    global $user;    
    
    //active researcher
    $active = 'UPDATE {research_projects_persons_list} SET active=1, user=%d, changed=NOW() WHERE id=%d';
    db_query($active, $user->uid, $person);
    
    //update info card
    $check = 'SELECT infor_card FROM {research_projects_onetime_invite_code} WHERE code="%s"';
    $info_card = db_result(db_query($check, $code));
    if($info_card && $info_card>0){
        $add_user = 'UPDATE {research_projects_participants_cards} SET user=%d, changed=NOW() WHERE id=%d';
        db_query($add_user, $user->uid, $info_card);
    }
    
    if($role=='participant'){
        module_invoke_all('project_join_new_participant', $project, $user->uid);
    }
    
    $log_content = "{$user->name} accept to join project as a $role.";
    log_operation($project, 'persons', $log_content, '', 'reProjectUser');
    
    //change code to used
    $set_code_used = 'UPDATE  {research_projects_onetime_invite_code} SET used=1, changed=NOW() WHERE code="%s"';
    db_query($set_code_used, $code);
    
    drupal_set_message(t('You have successfully joined the project.'), status);
    $form_state['redirect'] = get_project_url($project).'/welcome';
}


/** Login to join project form **/
function login_to_join_project_from($form_state, $person){
    $form = user_login($form_state);
    $form['name'] ['#size'] = 20;
    $form['name'] ['#value'] = $person->username;
    $form['name'] ['#disabled'] = TRUE;
    $form['name'] ['#description'] = '';
    $form['pass'] ['#size'] = 20;
    $form['pass'] ['#description'] = l(t('Forgot your password?'),'user/password');
    $form['code'] = array(
        '#type' => 'hidden',
        '#value' => $person->code,
    );
    $form['role'] = array(
        '#type' => 'hidden',
        '#value'=> $person->role,
    );
    $form['submit']['#value'] = t('Login and accept');
    return $form;
}

function login_to_join_project_from_validate($form, &$form_state){
    
    $code = $form_state['values']['code'];
    if(!invitation_code_is_not_used($code)){
        form_set_error('code', 'invitation code does not exist or it has already been used!');
    }
    
    user_authenticate($form_state['values']);
}

function login_to_join_project_from_submit($form, &$form_state){
    $code = $form_state['values']['code'];
    $role = $form_state['values']['role'];
    
    global $user;
    
    if($user->uid){
    
        //active person
        $p = db_fetch_object(db_query('SELECT project, person FROM {research_projects_onetime_invite_code} WHERE code="%s"', $code));
        $active = 'UPDATE {research_projects_persons_list} SET active=1, changed=NOW() WHERE id=%d';
        db_query($active, $p->person);
        
        //update info card
        $check = 'SELECT infor_card FROM {research_projects_onetime_invite_code} WHERE code="%s"';
        $info_card = db_result(db_query($check, $code));
        if($info_card && $info_card>0){
            $add_user = 'UPDATE {research_projects_participants_cards} SET user=%d, changed=NOW() WHERE id=%d';
            db_query($add_user, $user->uid, $info_card);
        }
        
        if($role=='participant'){
            module_invoke_all('project_join_new_participant', $p->project, $user->uid);
        }
        
        //change code to used
        $set_code_used = 'UPDATE  {research_projects_onetime_invite_code} SET used=1, changed=NOW() WHERE code="%s"';
        db_query($set_code_used, $code);
        
        $log_content = "{$user->name} accept to join project as a $role.";
        log_operation($p->project, 'persons', $log_content, '', 'reProjectUser');
        
        drupal_set_message(t('You have successfully joined the project.'), status);
        
        $form_state['redirect'] = get_project_url($p->project).'/welcome';
    }else{
        drupal_goto('user');    
    }

}

/** register to join project form **/
function register_to_join_project_from($form_state, $person){
    $form = user_register();
    $form['name'] ['#size'] = 20;
    $form['name'] ['#description'] = '';
    $form['pass'] ['#size'] = 20;
    $form['pass'] ['#description'] = '';
    $form['mail'] ['#size'] = 20;
    $form['mail'] ['#description'] = '';
    $form['mail'] ['#value'] = $person->email;
    $form['mail'] ['#disabled'] = TRUE;
    $form['code'] = array(
        '#type' => 'hidden',
        '#value' => $person->code,
    );
    $form['role'] = array(
        '#type' => 'hidden',
        '#value'=> $person->role,
    );
    $form['submit']['#value'] = t('Register and accept');
    return $form;
}

/** register to join project form **/
function register_to_join_project_from_validate($form, &$form_state){
    user_module_invoke('validate', $form_state['values'], $form_state['values'], 'account');
}

/** register to join project form **/
function register_to_join_project_from_submit($form, &$form_state){
    user_register_submit($form, $form_state);
    
    $code = $form_state['values']['code'];
    $role = $form_state['values']['role'];
    
    global $user;
    if($user->uid){
    
        //active participant
        $p = db_fetch_object(db_query('SELECT project, person FROM {research_projects_onetime_invite_code} WHERE code="%s"', $code));
        $active = 'UPDATE {research_projects_persons_list} SET active=1, user=%d, changed=NOW() WHERE id=%d';
        db_query($active, $user->uid, $p->person);
        
        //update info card
        $check = 'SELECT infor_card FROM {research_projects_onetime_invite_code} WHERE code="%s"';
        $info_card = db_result(db_query($check, $code));
        if($info_card && $info_card>0){
            $add_user = 'UPDATE {research_projects_participants_cards} SET user=%d, changed=NOW() WHERE id=%d';
            db_query($add_user, $user->uid, $info_card);
        }
        
        if($role=='participant'){
            module_invoke_all('project_join_new_participant', $p->project, $user->uid);
        }
        
        $log_content = "{$user->name} accept to join project as a $role.";
        log_operation($p->project, 'persons', $log_content, '', 'reProjectUser');
        
        $set_code_used = 'UPDATE  {research_projects_onetime_invite_code} SET used=1, changed=NOW() WHERE code="%s"';
        db_query($set_code_used, $code);
        
        drupal_set_message(t('You have successfully joined the project.'), status);
        $form_state['redirect'] = get_project_url($p->project).'/welcome';
    }
}