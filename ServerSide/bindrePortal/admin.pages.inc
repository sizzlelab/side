<?php

/**
 * Menu callback for admin/settings/reportal
 */
function admin_settings_reportal_default_templates_callback(){
    return drupal_get_form('reportal_default_message_templates_form');
}

/** default message templates form **/
function reportal_default_message_templates_form(){
    
    $form['invite-researcher'] = array(
        '#type' => 'fieldset',
        '#title' => t('Invite researcher'),
        '#collapsible' => TRUE,
        '#collapsed' => FALSE,
        '#weight' => 0,
    );    
    $form['invite-researcher']['researcher-template-title'] = array(
        '#type' => 'textfield',
        '#title' => t('Subject'),
        '#default_value' => variable_get('default_template_invite_researcher_subject',''),
        '#required' => TRUE,
    );
    $form['invite-researcher']['researcher-template-content'] = array(
        '#type' => 'textarea',
        '#rows' => 15,
        '#title' => t('Content'),
        '#default_value' => variable_get('default_template_invite_researcher_body',''),
        '#required' => TRUE,
    );
    
    $form['invite-participant'] = array(
        '#type' => 'fieldset',
        '#title' => t('Invite participant'),
        '#collapsible' => TRUE,
        '#collapsed' => FALSE,
        '#weight' => 0,
    );
    $form['invite-participant']['participant-template-title'] = array(
        '#type' => 'textfield',
        '#title' => t('Subject'),
        '#default_value' => variable_get('default_template_invite_participant_subject',''),
        '#required' => TRUE,
    );
    $form['invite-participant']['participant-template-content'] = array(
        '#type' => 'textarea',
        '#rows' => 15,
        '#title' => t('Content'),
        '#default_value' => variable_get('default_template_invite_participant_body',''),
        '#required' => TRUE,
    );
    
    $form['project-code-message'] = array(
        '#type' => 'fieldset',
        '#title' => t('Template for send project code'),
        '#collapsible' => TRUE,
        '#collapsed' => FALSE,
    );
    $form['project-code-message']['project-code-message-subject'] = array(
        '#type' => 'textfield',
        '#title'=> t('Subject'),
        '#default_value'=> variable_get('default_template_for_send_project_code_subject',''),
        '#required' => TRUE,
    );
    $form['project-code-message']['project-code-message-body'] = array(
        '#type' => 'textarea',
        '#title'=> t('Content'),
        '#default_value'=> variable_get('default_template_for_send_project_code_body',''),
        '#required' => TRUE,
    );
    $form['user-guide'] = array(
        '#type' => 'fieldset',
        '#title' => t('User guide page'),
        '#collapsible' => TRUE,
        '#collapsed' => FALSE,
    );
    $form['user-guide']['user-guide-content'] = array(
            '#type' => 'textarea',
            '#title' => t('Content'),
            '#required' => TRUE,
            '#default_value' => variable_get('default_template_for_user_guide',''),
        );
    $form['available-variable'] = array(
        '#value' => '<div>'
                    .'<h3>Available variables</h3>'
                    .'！project_link : the full link to the project<br/>'
                    .'@project_name : the name of the project<br/>'
                    .'@username : the user name of the user this email sending to.<br/>'
                    .'@project_code : the project code<br/>'
                    .'!invite_link : the invite link for new user<br/>'
                    .'!app_downlowd_link: the link to download Physical Activity Hx<br/>'
                    .'!user_guide_link: the link to view User Guide of Get&Check<br/><br/>'
                    .'</div>',
    );
    $form['submit'] = array(
        '#type' => 'submit',
        '#value' => t('Save'),
        '#suffix' => l('Reset configuration', 'admin/settings/reportal/restore_templates'),
    );
    return $form;
}
function reportal_default_message_templates_form_submit($form, &$form_state){
    
    $r_message_subject = $form_state['values']['researcher-template-title'];
    variable_set('default_template_invite_researcher_subject',$r_message_subject);
    $r_message_body = $form_state['values']['researcher-template-content'];
    variable_set('default_template_invite_researcher_body',$r_message_body);
    
    $p_message_subject = $form_state['values']['participant-template-title'];
    variable_set('default_template_invite_participant_subject',$p_message_subject);
    $p_message_body = $form_state['values']['participant-template-content'];
    variable_set('default_template_invite_participant_body',$p_message_body);
    
    $pc_message_subject = $form_state['values']['project-code-message-subject'];
    variable_set('default_template_for_send_project_code_subject',$pc_message_subject);
    $pc_message_body = $form_state['values']['project-code-message-body'];
    variable_set('default_template_for_send_project_code_body',$pc_message_body);
    
    $user_guid_content = $form_state['values']['user-guide-content'];
    variable_set('default_template_for_user_guide',$user_guid_content);
    
    drupal_set_message('Templates have been saved.');
}

/**
 * Menu callback for admin/settings/reportal/restore_templates
 */
function admin_settings_reportal_restore_templates_callback(){

    reset_global_invite_tempates();
    reset_global_projectcode_tempates();
    drupal_set_message('Restore the defalut template');
    drupal_goto('admin/settings/reportal');
}