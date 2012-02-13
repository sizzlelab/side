<?php

/**
 * Menu callback for project/%/manage
 */
function project_manage_callback($path){
    
    $project = prepare_project_manage_page($path);
    $project->path_category = 'manage';
    
    //set breadcrumb
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    drupal_set_breadcrumb($breadcrumb);
    
    //set right part
    //$project->subtitle = 'Management Category';
    
    //get management categories
    $blocks['categoris'] = module_invoke_all('project_manage_category', $project);
    //requir reference, module invoke can not use
    foreach (module_list() as $module) {
        $function = $module . '_project_content_block';
        if (function_exists($function)) {
            $function($project, 'manage_home', $blocks['others']);
        }
    }

    $project->right_part = theme('project_manage_home_page', $blocks);
    
    //add manage.home.css
    drupal_add_css(drupal_get_path('module','reProject').'/css/manage.home.css');
    
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/settings
 */ 
function project_manage_settings_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Settings'), generate_project_url($path).'/manage/settings');
    drupal_set_breadcrumb($breadcrumb);
    
    drupal_add_js(drupal_get_path('module', 'reProject').'/js/project.js');
    
    $project->path_category = 'settings';
    $project->path_category_subitem = 'basic'; 
    
    $project->subtitle = 'Project settings';
    $project->right_part = drupal_get_form('edit_research_project_form', $project);    
    return theme('project_manage_page',$project);
}

/******************* Edit project form **********************/

/**
 * Form for edit research  project
 */
function edit_research_project_form(&$form_state, $project){
    $form['id'] = array(
        '#type'             => 'hidden',
        '#value'            => $project->id,
    );
    $form['name'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Project name'),
        '#description'      => t('The name of the project.'),
        '#required'         => TRUE,
        '#default_value'    => $project->name,
    );
    $form['founder'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Founder'),
        '#description'      => t('The name of founders'),
        '#required'         => FALSE,
        '#default_value'    => $project->founder,
    );
    $form['path'] = array(
        '#type'             => 'textfield',
        '#title'            => t('The path to access project'),
        '#description'      => t('Unique path to access the project. By a-z, A-Z, 0-9 and _'),
        '#required'         => TRUE,
        '#default_value'    => $project->path,
    );
    $form['base-path-value'] = array(
        '#type' => 'hidden',
        '#value' => 'http://'.$_SERVER['HTTP_HOST'].base_path().'project/',
    );   
    /*$form['introduction'] = array(
        '#type'             => 'textarea',
        '#title'            => t('Introduction'),
        '#description'      => t('A short description of the project'),
        '#required'         => TRUE,
        '#default_value'    => $project->introduction,
    );*/
    $project_path = generate_project_url($project->path);
    $sql = 'SELECT id FROM {research_projects_contents} WHERE project=%d AND type="homepage" ORDER BY created DESC LIMIT 1 ';
    $nid = db_result(db_query($sql, $project->id));
    $form['introduction'] = array(
        '#value'            => '<div class="form-item">'.l('Edit introduction (home page)',$project_path.'/manage/contents/'.$nid).'</div>',
    );
    print_r($homepage);
    $form['submit'] = array(
        '#type'             => 'submit',
        '#value'            => t('Save'),
    );
    return $form;
}

/**
 * Handle validation for project form
 */
function edit_research_project_form_validate($form, &$form_state){
    $id = $form_state['values']['id'];
    $path = $form_state['values']['path'];
    
    //validate if the path contain illegal characters
    if(!preg_match('/^([0-9]|[_]|[a-z]|[A-Z])*$/',$path)){
        form_set_error('path','The path contain illegal characters. Only letters, numbers and _ are allowed');
    }
    
    //validate if the path is not used by other project
    $sql = "SELECT id FROM {research_projects} WHERE path='%s'";
    $result = db_result(db_query($sql, $path, $id));
    if($result && $result<>$id){
        form_set_error('path','Unlucky! The path has alreay been used by another project.');
    }
}

/**
 * Handle submit for project form
 */
function edit_research_project_form_submit($form, &$form_state){
    
    $id = $form_state['values']['id'];
    $name = $form_state['values']['name'];
    $founder = $form_state['values']['founder'];
    $path = $form_state['values']['path'];
    //$introduction = $form_state['values']['introduction'];
    
    $project = load_project($id, 'id');
    $project->name = $name;
    $project->founder = $founder;
    $project->path = $path;
    //$project->introduction = $introduction;
    _research_projects_save($project);
    
    drupal_set_message('Settings have been saved.', 'status');
    drupal_goto('project/'.$path.'/manage/settings');
}
