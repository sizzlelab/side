<?php

/**
 * Menu callback for project/%/manage/message/sms
 */
function project_manage_message_sms_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Messages'), generate_project_url($path).'/manage/message');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'messages'; 
    
    //add css
    drupal_add_css(drupal_get_path('module','reProjectMessage').'/message.css');
    //add js
    drupal_add_js(drupal_get_path('module','reProjectMessage').'/project_send_message_page.js');
    
    //add ajax link Drupal.settings.projectmanage.base_path
    drupal_add_js(array('projectmanage'=>array('base_path'=>url('project/'.$path.'/manage/'))),'setting');
    
    //get person list
    $get = 'SELECT id, user, name, email, phone, tags, notes FROM {research_projects_participants_cards} WHERE project=%d ORDER BY created';
    $res = db_query($get, $project->id);
    $list = array();
    while($p=db_fetch_object($res)){
        $list[] = $p;
    }
    $content.= theme('project_person_list_div', $list, 'sms');
    $content.= drupal_get_form('message_sms_sending_form', $project);
    
    $project->subtitle = t('Send SMS');
    $project->title_tabs = '<a href="'.url("project/$path/manage/message/email").'" class="mail-link">send email</a>';
    $project->right_part = $content;
    return theme('project_manage_page',$project);
}

/** SMS sending form **/

function message_sms_sending_form($form_state, $project){
    $form['project'] = array(
        '#type' => 'hidden',
        '#value' => $project->id,
    );
    $form['to-list'] = array(
        '#type'         => 'textarea',
        '#title'        => t('To'),
        '#description'  => t('The phone number should be splited by ";"'),
        '#required'     => TRUE,
        '#default_value'=> '',
    );
    $form['templates'] = array(
        '#type'         => 'select',
        '#title'        => t('Select a template as message (<a href="'.url("project/{$project->path}/manage/message/templates").'">Manage templates</a>).'),
        '#options'      => get_message_templates_options_by_type($project->id, 'sms'),
        '#required'     => FALSE,
    );
    $form['body'] = array(
        '#type'         => 'textarea',
        '#title'        => t('Body'),
        '#description'  => t('The body of the short message.'),
        '#required'     => TRUE,
    );
    $form['submit'] = array(
        '#type'         => 'submit',
        '#value'        => t('Send'),
    );
    return $form;
}
function message_sms_sending_form_validate($form, &$form_state){
    $to = $form_state['values']['to-list'];
    //generate to_mail array
    $to_phone = array();
    $token = strtok($to,  ';');
    while ($token !== false && valid_phone_number($token))
    {
        $to_phone[] = trim($token);
        $token = strtok(";");
    }
    if(empty($to_phone)){
        form_set_error('phone-list', 'No valid phone number.');
    }else{
        $form_state['values']['to-phone'] = $to_phone;
    }
    
    $body = $form_state['values']['body'];
    if(strlen($body)>300){
        form_set_error('body', 'Too long message!');
    }
}
function message_sms_sending_form_submit($form, &$form_state){
    $pid = $form_state['values']['project']; 
    $to = $form_state['values']['to-list'];
    $body = strip_tags($form_state['values']['body']);
    $to_phone = $form_state['values']['to-phone']; 
    
    send_group_sms($to_phone,$body);
    
    $log_content = "<p>Send short message to $to</p>"
                   ."<p>"
                   .'====================================================<br>'
                   ."$body</p>";
    log_operation($pid, 'message', $log_content, get_project_url($pid).'/manage/message/sms', 'reProjectMessage');
}