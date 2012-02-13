<?php

/**
 * Menu callback for project/%/manage/persons/researcher/add
 */
function project_manage_persons_researcher_add_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Persons'), generate_project_url($path).'/manage/persons');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    $project->subtitle = 'Add new researcher';
    $project->right_part = drupal_get_form('project_invite_research_form', $project->id);
    return theme('project_manage_page',$project);
}

/** Invite researcher form **/
function project_invite_research_form(&$form_state, $pid){
    $form['pid'] = array(
        '#type' => 'hidden',
        '#value' => $pid,
    );
    $form['email'] = array(
        '#type' => 'textfield',
        '#title' => t('Email address'),
        '#required' => TRUE,
        '#description' => t('The email of the researcher to be invited. '.l('Edit the invitation letter.', get_project_url($pid).'/manage/settings/templates')),
    );
    $form['submit'] = array(
        '#type' => 'submit',
        '#value' => t('Add'),
    );
    
    return $form;
}
function project_invite_research_form_validate($form, &$form_state){
    $pid = $form_state['values']['pid'];
    $email = $form_state['values']['email'];
    if(!valid_email_address($email)){
        form_set_error('email', 'The email address you input is invalid.');
    }
    
    //check if the email user already in project
    $check = 'SELECT name, active FROM {users} AS u, {research_projects_persons_list} AS p WHERE u.mail="%s" AND p.user=u.uid AND p.project=%d AND p.role IN ("researcher", "creator") AND active=1 ';
    $r = db_fetch_object(db_query($check, $email, $pid));
    
    if($r){
        //if the user already in researcher list
        form_set_error('email', 'The researcher has already in your project.');
    }
    
}
function project_invite_research_form_submit($form, &$form_state){
    $pid = $form_state['values']['pid'];
    $email = $form_state['values']['email'];
    
    send_invitation_email($pid, 'researcher', $email);
    
    //log the operation
    global $user;
    $content = "Send mail to invite $email to join project as a researcher.";
    log_operation($pid, 'persons', $content, get_project_url($pid).'/manage/persons/researcher/add', 'reProjectUser');
    
    $form_state['redirect']  = get_project_url($pid).'/manage/persons';
    
    
}