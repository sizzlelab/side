<?php
/**
 * Menu callback for projects/new
 */
function projects_new_callback(){
    $module_path = drupal_get_path('module', 'reProject');
    drupal_add_js($module_path.'/js/project.js');
    //get project form
    $content = drupal_get_form('create_new_project_form');
    return $content;
}
/**
 * Menu callback for projects/new/editpages/
 */
function projects_new_editpages_callback(){
    //get project form
    if(isset($_SESSION['new_project_id'])&&$_SESSION['new_project_id']>0){
        $content = drupal_get_form('first_edit_project_pages_form',$_SESSION['new_project_id']);
    }else{
        $content = '<h1>Session missing! This page is noly allow when first create project.</h1>';
    }
    
    return $content;
}
/**
 * Menu callback for projects/list
 */
function projects_list_callback(){
    if(user_access('create research project')){
        $content.= '<p style="text-align:right;"><a href="'.url('projects/new').'" class="add_link">Create new project</a></p>';
    }
    $rows = array();
    $header = array(
        array('data'=>'Project name'),
        array('data'=>'Created at'),
        array('data'=>'Creator'),
    );
    
    //get project list
    $sql = "SELECT id, creator, p.name name, u.name username, uid, url, path, founder, introduction, p.created created, p.changed changed FROM {research_projects} AS p LEFT JOIN {users} AS u ON uid=creator ORDER BY p.created DESC";
    
    $res = pager_query($sql,10);
    while($row = db_fetch_object($res)){
        $rows[] = array(
            l($row->name,'project/'.$row->path),
            format_interval(time()-strtotime($row->created)).' ago',
            l($row->username, 'user/'.$row->uid),
        );
    }
    
    $content.= theme_table($header, $rows);
    $content.= theme('pager');
    
    return $content;
    
}

/**
 * Menu callback for project/%
 */
function project_display_callback($path){
    $project = load_project($path);
    
    if($project){drupal_set_breadcrumb($breadcrumb);
        
        drupal_set_breadcrumb(array());
        
        drupal_add_css(drupal_get_path('module','reProject').'/css/retools.css');
        
        $homepage = load_homepage_node($project);
        $project->homepage = $homepage;
        
        //load tools list
        $sql = 'SELECT t.id id, name, logo, introduction FROM {research_project_tools_list} AS p, {research_tools} AS t '
              .'WHERE p.project=%d AND p.tool=t.id';
        $tools = array();
        $res = db_query($sql, $project->id);
        while($tool=db_fetch_object($res)){
            $tool->links = load_project_tool_links($project->id, $tool->id);
            $tools[] = $tool;
        }
        $project->tools_list = $tools;
        return theme('project_home_page', $project);
    }
    drupal_not_found();
}

/**
 * Menu callback for project/%/help
 */
function project_help_callback($path){
    $project = load_project($path);
    if($project){
        $sql = 'SELECT node FROM {research_projects_contents} WHERE project=%d AND type="%s"';
    
        //get helppage
        $helppage_id = db_result(db_query($sql, $project->id, 'helppage'));
        if($helppage_id){
            $helppage = node_load($helppage_id);
            return $helppage->body;
        }
    }
    drupal_not_found();
}

/***************** Create new project form********************/

/**
 * Form for create new project
 */
function create_new_project_form(){
    
    $form['name'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Project name'),
        '#description'      => t('The name of the project.'),
        '#required'         => TRUE,
        //'#default_value'    => isset($project->name)?$project->name:'',
    );
    $form['founder'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Organization'),
        '#description'      => t('The institution who is responsible for the project. If this is a personal project, you can keep this field empty. '),
        //'#default_value'    => isset($project->founder)?$project->founder:'',
    );
    $form['path'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Define a project path'),
        '#description'      => t('Unique path to access the project. By a-z, A-Z, 0-9 and _'),
        '#required'         => TRUE,
        '#size'             => 15,             
        //'#default_value'    => isset($project->path)?$project->path:'',
    );
    $form['base-path-value'] = array(
        '#type' => 'hidden',
        '#value' => 'http://'.$_SERVER['HTTP_HOST'].base_path().'project/',
    );   
    $form['introduction'] = array(
        '#type'             => 'textarea',
        '#title'            => t('Write a short introduction for your project'),
        '#description'      => t('A short description of the project'),
        '#required'         => TRUE,
        //'#default_value'    => isset($project->introduction)?$project->introduction:'',
    );
    $form['create'] = array(
        '#type'             => 'submit',
        '#value'            => t('Create'),
    );
    return $form;
}

/**
 * Handle validation for project form
 */
function create_new_project_form_validate($form, &$form_state){
    //$name = $form_state['values']['name'];
    $path = $form_state['values']['path'];
    //$introduction = $form_state['values']['introduction'];
    
    //validate if the path contain illegal characters
    if(!preg_match('/^([0-9]|[_]|[a-z]|[A-Z])*$/',$path)){
        form_set_error('path','The path contain illegal characters. Only letters, numbers and _ are allowed');
    }
    
    //validate if the path is unique
    $sql = "SELECT COUNT(*) FROM {research_projects} WHERE path='%s'";
    if(db_result(db_query($sql, $path))>0){
        form_set_error('path','Unlucky! The path has alreay been used by another project.');
    }
}

/**
 * Handle submit for project form
 */
function create_new_project_form_submit($form, &$form_state){
    
    //get current user
    global $user;
    if($user->uid==0){
        //not authentication user, redirect to login page
        drupal_goto('user');
    }
    
    $project = new stdClass;
    $project->creator       = $user->uid;
    $project->name          = $form_state['values']['name'];
    $project->founder       = $form_state['values']['founder'];
    $project->path          = $form_state['values']['path'];
    $project->introduction  = $form_state['values']['introduction'];
    
    if(create_new_project($project)){
        //succeed to create new project
        drupal_set_message($message = "Welcome to your new project.", $type='status' );
        drupal_goto(generate_project_url($project->path));
        //$_SESSION['new_project_id'] = $project->id;
        //drupal_goto("projects/new/editpages");
    }else{
        drupal_set_message($message = 'Failed to create new project', $type = 'error');
        drupal_goto('user');
    }
}

/**
 * Form for first edit project pages
 */
function first_edit_project_pages_form(&$form_state, $pid=0){
    
    $sql = 'SELECT node FROM {research_projects_contents} WHERE project=%d AND type="%s"';
    
    //get homepage
    $homepage_id = db_result(db_query($sql, (int)$pid, 'homepage'));
    if($homepage_id){
        $homepage = node_load($homepage_id);
    }
    
    //get helppage
    $helppage_id = db_result(db_query($sql, (int)$pid, 'helppage'));
    if($helppage_id){
        $helppage = node_load($helppage_id);
    }
    
    $form['project_id'] = array(
        '#type'         => 'hidden',
        '#value'        =>  ''.$pid, 
    );
    $form['homepage'] = array(
        '#type'         => 'textarea',
        '#title'        => t('Home page content'),
        '#description'  => t('This is a public page. Please not put any confidential informatoin here!'),
        '#required'     => TRUE,
        '#default_value'      => $homepage->body,
    );
    $form['homepage_nid'] = array(
        '#type'         => 'hidden',
        '#value'        =>  ''.$homepage_id, 
    );
    $form['helppage'] = array(
        '#type'         => 'textarea',
        '#title'        => t('Help page content'),
        '#description'  => t('This is a public page. Please not put any confidential informatoin here!'),
        '#required'     => TRUE,
        '#default_value'  => $helppage->body,
    );
    $form['helppage_nid'] = array(
        '#type'         => 'hidden',
        '#value'        =>  ''.$helppage_id, 
    );
    $form['submit'] = array(
        '#type'         => 'submit',
        '#value'        => t('Save'),
    );
    
    return $form;
}

/**
 * Handle submit for first edit project pages
 */
function first_edit_project_pages_form_submit($form, &$form_state){
    //die(print($form_state));
    
    $pid = $form_state['values']['project_id'];
    $homepage_body = $form_state['values']['homepage'];
    $homepage_nid = $form_state['values']['homepage_nid'];
    $helppage_body = $form_state['values']['helppage'];
    $helppage_nid = $form_state['values']['helppage_nid'];
    
    
    //save homepage
    $homepage = node_load($homepage_nid);
    $homepage->body = $homepage_body;
    node_save($homepage);
    
    //save helppage
    $helppage = node_load($helppage_nid);
    $helppage->body = $helppage_body;
    node_save($helppage);
    
    //get project path
    drupal_goto(get_project_url($pid));
}

/**
 * Handle operation to create a new project().
 */
function create_new_project($project){
    //step.1 save project info
    if(_research_projects_save($project)){
        
        //log operation
        log_operation($project->id, 'information', "Create new project", 'http://'.$_SERVER['HTTP_HOST'].url('projects/new'), REPROJECT_MODULE_NAME);
        
        //update project activity
        update_project_activity($project->id, 1);
        
        //invoke hook_create_project().
        module_invoke_all('create_project', $project);
        
        return TRUE;
        
    }else{
        return FALSE;
    }
    
}

/**
 * Menu callback for project/%/firstguide
 */
function project_firstguide_callback($path){
    $project = load_project($path);
    if($project){
        $firstguide = load_first_guide_node($project);
        
        return $firstguide->body;
    }
    drupal_not_found();
}