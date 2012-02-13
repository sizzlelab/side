<?php

/**
 * Menu callback for project/%/manage/tools
 */
function project_manage_tools_list_callback($path){
    $project = prepare_project_manage_page($path);
    
    //set breadcrumb
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'tools'; 
        
    //get right part info
    $content = '<a href="'.url('project/'.$path.'/manage/tools/selete').'" class="add_link">Add new tool</a>';
    $rows = array();
    $header = array(
        array('data'=>'Tool'),
        array('data'=>'Name'),
        array('data'=>'Operation'),
    );
        
    $sql = "SELECT t.id id, t.name, t.logo FROM {research_project_tools_list} AS p, {research_tools} AS t WHERE p.project=%d AND p.tool=t.id";
    $res = db_query($sql, $project->id);
    while($row=db_fetch_object($res)){
        $rows[] = array(
            "<image src='{$row->logo}' width='100px' />",
            "<a href='".url('research/tools/view/'.$row->id)."' target='_blank'>{$row->name}</a>",
            "<p><a href='".url("project/$path/manage/tools/remove/{$row->id}")."' class='delete_link'>Remove</a></p>"
            ."<p><a href='".url("project/$path/manage/tools/editlinks/{$row->id}")."' class='links_link'>Related links</a></p>",
        );
    }
    $content.= theme_table($header, $rows);
    $content.= theme('pager');
    $project->subtitle = 'Tools management';
    $project->right_part = $content;    
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/tools/selete
 */
function project_manage_tools_selete_list_callback($path){
    $project = prepare_project_manage_page($path);
    
    //set breadcrumb
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Tools'), generate_project_url($path).'/manage/tools');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'tools'; 
        
    //get right part info
    if(user_access('create research tool')){
        $content = "<p><a class='add_link' href='".url('research/tools/add')."'>Create new</a></p>";
    }
    $rows = array();
    $header = array(
        array('data'=>'Tool'),
        array('data'=>'Name'),
        array('data'=>'Operation'),
    );
        
    $sql = "SELECT t.id id, t.name, t.logo FROM {research_tools} AS t ORDER BY t.changed DESC";
    $res = db_query($sql);
    while($row=db_fetch_object($res)){
        $rows[] = array(
            "<image src='{$row->logo}' width='100px' />",
            "<a href='".url('research/tools/view/'.$row->id)."' target='_blank'>{$row->name}</a>",
            check_project_using_tool($project, $row->id)?'You are using this tool.':"<a href='".url("project/$path/manage/tools/selete/{$row->id}")."' class='add_link'>Add to project</a>",
        );
    }
    $content.= theme_table($header, $rows);
    $content.= theme('pager');
    $project->subtitle = 'Tools management';
    $project->right_part = $content;    
    return theme('project_manage_page',$project);

}

/**
 * Menu callback for project/%/manage/tools/selete/%
 */
function project_manage_tools_selete_new_callback($path, $id){
    $project = prepare_project_manage_page($path);
    project_bind_tool($project, $id);
    drupal_goto('project/'.$path.'/manage/tools');
}

/**
 * Menu callback for project/%/manage/tools/remove/%
 */
function project_manage_tools_remove_callback($path, $tool_id){
    $project = prepare_project_manage_page($path);
    project_remove_tool($project, $tool_id);
    drupal_goto('project/'.$path.'/manage/tools');
}

/**
 * Menu callback for project/%/manage/tools/editlinks/%
 */
function project_manage_tools_editlink_callback($path, $tool_id){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Tools'), generate_project_url($path).'/manage/tools');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'tools'; 
    
    drupal_add_css(drupal_get_path('module','reTool').'/reTool.css');
    
    $project->subtitle = 'Edit tool releated links';
    $content.= '<p class="message">The links will be showed on tool block in '.l('project home page',generate_project_url($path) ).'</p>';
    $content.= '<a href="'.url("project/$path/manage/tools/newlink/$tool_id").'" class="add_link">Add new</a>';
    
    //get tool info
    $tool = new stdClass;
    $tool->project = $project->id;
    $tool->id = $tool_id;
    
    $rows = array();
    $header = array(
        array('data'=>'Title'),
        array('data'=>'Link'),
        array('data'=>'Operation','width'=>'100px'),
    );
    
    //get links list
    $get = 'SELECT id, project, tool, title, url, permission, weight FROM {research_project_tool_links} WHERE project=%d AND tool=%d ORDER BY weight';
    $res = db_query($get, $project->id, $tool_id);
    while($row = db_fetch_object($res)){
        $rows[] = array(
            '<div class="project-tool-title-div">'.$row->title.'<br/>('.$row->permission.')</div>',
            '<div class="project-tool-link-div">'.l($row->url,$row->url, array('att'=>'')).'</div>',
            '<p><a href="'.url("project/$path/manage/tools/removelink/$tool_id/{$row->id}").'" class="delete_link">Remove</a></p>'.
            '<p><a href="'.url("project/$path/manage/tools/editlink/$tool_id/{$row->id}").'" class="edit_link">Edit</a></p>',
        );
    }
    
    
    $content.= theme_table($header, $rows);
    
    $project->right_part = $content;
    return theme('project_manage_page',$project);
}

/**
 *  Menu callback for project/%/manage/tools/newlink/%
 */
function project_manage_tools_newlink_callback($path, $tool_id){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Tools'), generate_project_url($path).'/manage/tools');
    $breadcrumb[] = l(t(get_research_tool_name($tool_id)), generate_project_url($path).'/manage/tools/editlinks/'.$tool_id);
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'tools'; 
    
    drupal_add_css(drupal_get_path('module','reTool').'/reTool.css');
    
    $project->subtitle = 'Add new link';
    //get tool info
    $tool = new stdClass;
    $tool->project = $project->id;
    $tool->id = $tool_id;
    $content = drupal_get_form('add_new_tool_related_link_form', $tool);
    $project->right_part = $content;
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/tools/removelink/%/%
 */
function project_manage_tools_link_remove_callback($path, $tool_id, $link_id){
    $project = prepare_project_manage_page($path);
    
    //remove
    $remove = 'DELETE FROM {research_project_tool_links} WHERE id=%d AND project=%d AND tool=%d';
    db_query($remove, $link_id, $project->id, $tool_id);
    drupal_goto("project/$path/manage/tools/editlinks/$tool_id");
}

/**
 * Menu callback for project/%/manage/tools/editlink/%/%
 */
function project_manage_tools_edit_selected_link_callback($path, $tool_id, $link_id){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Tools'), generate_project_url($path).'/manage/tools');
    $breadcrumb[] = l(t(get_research_tool_name($tool_id)), generate_project_url($path).'/manage/tools/editlinks/'.$tool_id);
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'tools'; 
    
    $link = get_project_link($link_id);
    if($link && $link->project==$project->id && $link->tool==$tool_id){
        $project->subtitle = 'Edit tool link';
        $content.= drupal_get_form('edit_tool_related_link_form', $link);
        $project->right_part = $content;
        return theme('project_manage_page',$project);
    }else{
        drupal_not_found();
    }    
}
/** add new tool related link form **/
function add_new_tool_related_link_form($form_state, $tool){
    $form['project'] = array(
        '#type'     => 'hidden',
        '#value'    => $tool->project,
    );
    $form['tool'] = array(
        '#type'     => 'hidden',
        '#value'    => $tool->id,
    );
    $form['title'] = array(
        '#title'    => t('Link name'),
        '#type'     => 'textfield',
        '#size'     => 15,
        '#required' => TRUE,
    );
    $form['url'] = array(
        '#title'    => t('Link URL'),
        '#type'     => 'textfield',
        '#size'     => 20,
        '#required' => TRUE,
        '#description' => t('The URL should be a full URL address. e.g. Starts with \'http://\', \'https://\' or \'ftp://\' '),
    );
    $options = array(
        'PUBLIC'   => 'Public',
        'CONFIDENTIAL' => 'Only for researchers',
    );
    $form['permission'] = array(
        '#title'    => t('Permission'),
        '#type'     => 'select',
        '#options'  => $options,
        '#description' => t('Define the user group who can view this link.'),
    );
    $form['weight'] = array(
        '#title'    => t('Weight'),
        '#type'     => 'weight',
        '#required' => TRUE,
        '#description' => t('The smaller number, the higher priority order on the link list.'),
    );
    $form['add'] = array(
        '#type'     => 'submit',
        '#value'    => t('Add'),
    );
    return $form;
}
function add_new_tool_related_link_form_submit($form, &$form_state){
    $project = $form_state['values']['project'];
    $tool = $form_state['values']['tool'];
    $title = $form_state['values']['title'];
    $url = $form_state['values']['url'];
    $weight = $form_state['values']['weight'];
    $permission = $form_state['values']['permission'];
    
    $add = 'INSERT INTO {research_project_tool_links}(project, tool, title, url, weight, permission, created, changed) VALUES(%d, %d, "%s", "%s", %d, "%s", NOW(), NOW())';
    db_query($add, $project, $tool, $title, $url, $weight, $permission);
    
    drupal_goto(get_project_url($project).'/manage/tools/editlinks/'.$tool);
}
/** edit_tool_related_link form **/
function edit_tool_related_link_form($form_state, $link){
    $form['id'] = array(
        '#type'     => 'hidden',
        '#value'    => $link->id,  
    );
    $form['project'] = array(
        '#type'     => 'hidden',
        '#value'    => $link->project,
    );
    $form['tool'] = array(
        '#type'     => 'hidden',
        '#value'    => $link->tool,
    );
    $form['title'] = array(
        '#type'     => 'textfield',
        '#title'    => t('Link title'),
        '#required' => TRUE,
        '#default_value' => $link->title,
    );
    $form['url'] = array(
        '#type'     => 'textfield',
        '#title'    => t('URL'),
        '#required' => TRUE,
        '#default_value' => $link->url,
    );
    $options = array(
        'PUBLIC'   => 'Public',
        'CONFIDENTIAL' => 'Only for researchers',
    );
    $form['permission'] = array(
        '#type'     => 'select',
        '#title'    => t('Permission'),
        '#options'  => $options,
        '#default_value' => $link->permission,
    );
    $form['weight'] = array(
        '#type'     => 'weight',
        '#title'    => t('Weight'),
        '#required' => TRUE,
        '#default_value' => $link->weight,
    );
    $form['save'] = array(
        '#type'     => 'submit',
        '#value'    => t('Save'),
    );
    return $form;
}
function edit_tool_related_link_form_submit($form, &$form_state){
    $id = $form_state['values']['id'];
    $project = $form_state['values']['project'];
    $tool = $form_state['values']['tool'];
    $title = $form_state['values']['title'];
    $url = $form_state['values']['url'];
    $weight = $form_state['values']['weight'];
    $permission = $form_state['values']['permission'];
    
    $update = 'UPDATE {research_project_tool_links} SET title="%s", url="%s", weight=%d, permission="%s", changed=NOW() WHERE id=%d AND project=%d AND tool=%d';
    db_query($update, $title, $url, $weight, $permission, $id, $project, $tool);
    drupal_goto(get_project_url($project).'/manage/tools/editlinks/'.$tool);
}