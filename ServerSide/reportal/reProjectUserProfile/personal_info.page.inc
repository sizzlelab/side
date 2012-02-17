<?php

/**
 * Menu callback for user/%user/edit/personal_info
 */
function user_edit_personal_info_callback($user){
    $info = load_user_personal_info($user->uid);
    if($info){
        return drupal_get_form('edit_personal_information_form', $info);
    }else{
        drupal_not_found();   
    }
}

/** Edit personal information form**/
function edit_personal_information_form($form_state, $info){
    $form['user'] = array(
        '#type' => 'hidden',
        '#value' => $info->user,
    );
    $form['firstname'] = array(
        '#type' => 'textfield',
        '#title' => t('First name'),
        '#maxlength' => 45,
        '#default_value' => isset($info->firstname)?$info->firstname:'',
        '#required' => TRUE,
    );
    $form['lastname'] = array(
        '#type' => 'textfield',
        '#title' => t('Last name'),
        '#maxlength' => 45,
        '#default_value' => isset($info->lastname)?$info->lastname:'',
        '#required' => TRUE,
    );
    $gender_option = array(
        'f' => t('Female'),
        'm' => t('Male'),
        's' => t('Secret'),
    );
    $form['gender'] = array(
        '#type' => 'radios',
        '#title' => t('Gender'),
        '#options' => $gender_option,
        '#default_value' => empty($info->gender)?'s':$info->gender,
        '#required' => TRUE,
    );
    $form['birthday'] = array(
        '#type' => 'textfield',
        '#title' => t('Birthday'),
        '#default_value' => isset($info->birthday)?$info->birthday:'',
        '#required' => TRUE,
        '#description'  => t('Date format is yyyy-mm-dd. E.g. 1988-01-01'),
    );
    $form['weight'] = array(
        '#type' => 'textfield',
        '#title' => t('Weight (kg)'),
        '#default_value' => isset($info->weight)?$info->weight:'',
        '#description' => t('Please input a number. The float part should be like 50.62'),
    );
    $form['height'] = array(
        '#type' => 'textfield',
        '#title' => t('Height (cm)'),
        '#default_value' => isset($info->height)?$info->height:'',
        '#description' => t('Please input a number. The float part should be like 168.30'),
    );
    $form['submit'] = array(
        '#type' => 'submit',
        '#value' => t('Save'),
    );
    return $form;
}

function edit_personal_information_form_validate($form, &$form_state){
    //validate uid
    $user = (int)$form_state['values']['user'];
    if($user<=0){
        form_set_error('user', 'The user information is lost, please reload this page.');
    }
    
    //validate birthday, format is yyyy-mm-dd
    $birthday = $form_state['values']['birthday'];
    if(!preg_match('/^[0-9]{4}-(([0][1-9])|([1][0-2]))-(([0][1-9])|([12]\d)|([3][01]))$/',$birthday)){
        form_set_error('birthday', 'Incorrect date format.');   
    }
    
    //validate weight, should be a float or integer and >0
    $weight = $form_state['values']['weight'];
    if(!empty($weight)){
        if(!is_numeric($weight)){
            form_set_error('weight', 'Your weight should be a number.');   
        }elseif($weight<=0){
            form_set_error('weight', 'Please input a valid number.');   
        }
    }
    
    //validate height, should be a float or integer and >0
    $height = $form_state['values']['height'];
    if(!empty($weight)){
        if(!is_numeric($height)){
            form_set_error('height', 'Your height should be a number.');   
        }elseif($height<=0){
            form_set_error('height', 'Please input a valid number.');   
        }
    }
    

}
function edit_personal_information_form_submit($form, &$form_state){
    $user = $form_state['values']['user'];
    $firstname = $form_state['values']['firstname'];
    $lastname = $form_state['values']['lastname'];
    $gender = $form_state['values']['gender'];
    $birthday = $form_state['values']['birthday'];
    $weight = $form_state['values']['weight'];
    $height = $form_state['values']['height'];
    
    //update
    $update = 'UPDATE {research_project_user_profile} SET firstname="%s", lastname="%s", gender="%s", birthday="%s", weight=%f, height=%f, changed=NOW() WHERE user=%d';
    db_query($update, $firstname, $lastname, $gender, $birthday, $weight, $height, $user);
    
    drupal_goto('user');
}
