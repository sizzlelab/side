<?php

/**
 * Menu callback for project/%/manage/persons/participant/remove/%
 */
function project_manage_persons_participant_remove_callback($path, $id){
    $project = prepare_project_manage_page($path);
    
    //check if there is user id
    $check = 'SELECT user FROM {research_projects_participants_cards} WHERE id=%d AND project=%d';
    $uid = db_result(db_query($check, $id, $project->id));
    if($uid){
        //remove person from person list
        $remove = "DELETE FROM {research_projects_persons_list} WHERE user=%d AND project=%d AND role='participant' ";
        db_query($remove, $uid, $project->id);
    }
    
    $remove = "DELETE FROM {research_projects_participants_cards} WHERE id=%d AND project=%d";
    db_query($remove, $id, $project->id);
    drupal_goto("project/$path/manage/persons");
    
}

/**
 * Menu callback for project/%/manage/persons/participant/new
 */
function project_manage_persons_participants_new_callback($path){
    $project = prepare_project_manage_page($path); 
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Persons'), generate_project_url($path).'/manage/persons');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    $project->subtitle = 'New participants';
    $project->right_part = drupal_get_form('new_participant_info_card_form', $project);
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/persons/participant/edit/%
 */
function project_manage_persons_participant_edit_callback($path, $id){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Persons'), generate_project_url($path).'/manage/persons');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    //get person info
    $get = "SELECT id, project, name, email, phone, notes FROM {research_projects_participants_cards} WHERE id=%d AND project=%d";
    $person = db_fetch_object(db_query($get, $id, $project->id));
    if($person){
        
        $project->subtitle = 'Edit person information';
        $project->right_part = drupal_get_form('edit_person_info_card_form', $person);
        return theme('project_manage_page',$project);

    }else{
        drupal_not_found();
    }
    
}
/** new person info card form **/
/** New participant info card form **/
function new_participant_info_card_form($form_state, $project){
    $form['project'] = array(
        '#type'         => 'hidden',
        '#value'        => $project->id,
    );
    $form['name'] = array(
        '#title'        => t('Participant name'),
        '#type'         => 'textfield',
        '#size'         => 20,
        '#required'     => TRUE,
    );
    $form['email'] = array(
        '#title'        => t('Email'),
        '#type'         => 'textfield',
        '#size'         => 20,
        '#required'     => TRUE,
    );
    $form['send-invitation'] = array(
        '#title'        => t('Send invitation email. '.l('[Edit the invitation]', generate_project_url($project->path).'/manage/settings/templates')),
        '#type'         => 'checkbox',
        '#description'  => t("Participant's data can be viewed after s(he) has accepted the invitation to the project."),
        '#default_value'=> TRUE,
    );
    $form['phone'] = array(
        '#title'        => t('Phone'),
        '#type'         => 'textfield',
        '#size'         => 20,
    );
    $form['notes'] = array(
        '#title'        => t('Notes'),
        '#type'         => 'textarea',
        '#cols'         => 5,
        '#rows'         => 3,
        '#description'  => t('Additional infomation of this participant.'),
    );
    $form['submit'] = array(
        '#type'         => 'submit',
        '#value'        => t('Add'),
    );
    return $form;
}
function new_participant_info_card_form_validate($form, &$form_state){
    $name = $form_state['values']['name'];
    if(empty($name)){
        form_set_error('name','The name is required.');
    }
    
    $email = $form_state['values']['email'];
    if(!valid_email_address($email)){
        form_set_error('email','Invalid email address.');
    }
    
    $project = $form_state['values']['project'];
    $check_person_sql = 'SELECT id, user, name, email, phone, tags, notes FROM {research_projects_participants_cards} '
                       .'WHERE project=%d AND email="%s" ';
    $res = db_fetch_object(db_query($check_person_sql, $project, $email));
    if($res){
        //TODO: add link to visit personal info
        form_set_error('email','The email already in your participant list. ');//.l('View the participant.', get_project_url($project).'/manage/persons/info/'.$res->id));
    }
    
    
    $phone = $form_state['values']['phone'];
    if(!empty($phone) && !valid_phone_number($phone)){
        form_set_error('phone','Invalid phone number.');
    }            
    
}
function new_participant_info_card_form_submit($form, &$form_state){
    
    $project = $form_state['values']['project'];
    $name = $form_state['values']['name'];
    $email = $form_state['values']['email'];
    $phone = $form_state['values']['phone'];
    $notes = filter_filter('process', 0, -1, $form_state['values']['notes']);
    
    $insert = 'INSERT INTO {research_projects_participants_cards} '
             .'(project, name, email, phone, notes, created, changed) '
             .'VALUES(%d, "%s", "%s", "%s", "%s", NOW(), NOW())';
    db_query($insert, $project, $name, $email, $phone, $notes);
    $info_card = mysql_insert_id();
    
    //check if send invitation letter
    $send = $form_state['values']['send-invitation'];    
    //if yes, send invitation letter, add participant card
    if($send){
        send_invitation_email($project, 'participant', $email, $info_card);
        $form_state['redirect'] = get_project_url($project).'/manage/persons';
    }
    
}

/** Edit person info card form **/
function edit_person_info_card_form($form_state, $person){
    $form['id'] = array(
        '#type'         => 'hidden',
        '#value'        => $person->id, 
    );
    $form['project'] = array(
        '#type'         => 'hidden',
        '#value'        => $person->project,
    );
    $form['name'] = array(
        '#type'         => 'textfield',
        '#title'        => t('Name'),
        '#default_value'        => $person->name,
        '#required'     => TRUE,
    );
    $form['email'] = array(
        '#type'         => 'textfield',
        '#title'        => t('Email'),
        '#value'        => $person->email,
        '#disabled'   => TRUE,
    );
    $form['phone'] = array(
        '#type'         => 'textfield',
        '#title'        => t('Phone'),
        '#default_value'        => $person->phone,
        '#description'  => t('The phone number should start with +358.'),
    );
    $form['notes'] = array(
        '#type'         => 'textarea',
        '#title'        => t('Notes'),
        '#default_value'        => $person->notes,
    );
    $form['submit'] = array(
        '#type'         => 'submit',
        '#value'        => t('Save'),
    );
    return $form;
}
function edit_person_info_card_form_validate($form, &$form_state){
    //validate email
    $email = $form_state['values']['email'];
    if(!empty($email) && !valid_email_address($email)){
        form_set_error('email', 'Invalid email address.');
    }
    
    //validate phone number
    $phone = $form_state['values']['phone'];
    if(!empty($phone) && !valid_phone_number($phone)){
        form_set_error('phone', 'Invalid phone number.');
    }
}
function edit_person_info_card_form_submit($form, &$form_state){
    $id = $form_state['values']['id'];
    $project = $form_state['values']['project'];
    $name = $form_state['values']['name'];
    $email = $form_state['values']['email'];
    $phone = $form_state['values']['phone'];
    $notes = filter_filter('process', 0, -1, $form_state['values']['notes']);
    
    //update
    $update = 'UPDATE {research_projects_participants_cards} SET name="%s", email="%s", phone="%s", notes="%s" WHERE id=%d and project=%d';
    db_query($update, $name, $email, $phone, $notes, $id, $project);
    $pObj = load_project($project, 'id');
    drupal_goto('project/'.$pObj->path.'/manage/persons');
}