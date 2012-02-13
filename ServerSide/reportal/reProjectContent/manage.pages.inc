<?php
/**
 * Menu callback for project/%/manage/contents
 */
function project_manage_contents_list_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'content'; 
        
    //get right part info
    $content = '<a href="'.url("project/$path/manage/contents/new").'" class="add_link">Create new</a>';
    $rows = array();
    $header = array(
        array('data'=>'Title'),
        array('data'=>'Type'),
        array('data'=>'Last update'),
        array('data'=>'Operations'),
    );
        
    $sql = "SELECT p.id id, n.title title, p.title_path path, p.type type, p.deletable deletable, n.changed changed FROM {research_projects_contents} AS p, {node} AS n WHERE p.project={$project->id} AND p.node=n.nid ORDER BY n.changed DESC";
    $res = pager_query($sql, 30);
    while($row=db_fetch_object($res)){
        $rows[] = array(
            l($row->title,'project/'.$path.'/manage/contents/'.$row->id),
            $row->type,
            format_interval(time()-$row->changed).' ago',
            '<p>'
                .l('View','project/'.$path.'/content/'.$row->path, array('attributes' => array('target'=>'_blank', 'class'=>'view_link')))
                .l('Edit','project/'.$path.'/manage/contents/'.$row->id, array('attributes' => array('class'=>'edit_link')))
                .(($row->deletable==1)?l('Delete','project/'.$path.'/manage/contents/remove/'.$row->id, array('attributes' => array('class'=>'delete_link'))):'')
            .'</p>',
        );
    }
    $content.= theme_table($header, $rows);
    $content.= theme('pager');
    $project->subtitle = 'Contents management';
    $project->right_part = $content;    
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/contents/%
 */
function project_manage_content_edit_callback($path, $id){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Contents'), generate_project_url($path).'/manage/contents');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'content'; 
    
    //get content node
    $sql = 'SELECT id, node, type, permission, project FROM {research_projects_contents} WHERE id=%d AND project=%d LIMIT 1';
    $content = db_fetch_object(db_query($sql, $id, $project->id));
    if($content && $content->node>0){
        $node = node_load($content->node);
        $content->node = $node;
        $project->subtitle = "Edit {$node->title}";
        $project->right_part = drupal_get_form('project_content_edit_form', $content);
        return theme('project_manage_page',$project);
    }
    
}

/**
 * Menu callback for project/%/manage/contents/new
 */
function project_manage_content_new_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Contents'), generate_project_url($path).'/manage/contents');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'content'; 
    
    $project->subtitle = 'Create a new content page';
    $content = new stdClass;
    $content->project = $project->id;
    $project->right_part = drupal_get_form('project_content_edit_form', $content);
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/contents/remove/%
 */
function project_manage_content_remove_callback($path, $id){
    $project = prepare_project_manage_page($path);
    //get content node
    $sql = 'SELECT id, node, project, title, title_path, r.type, permission, deletable FROM {research_projects_contents} AS r, {node} AS n WHERE id=%d AND project=%d AND node=nid LIMIT 1';
    $content = db_fetch_object(db_query($sql, $id, $project->id));
    if($content && $content->node>0 && $content->deletable==1){
        $project->subtitle = "Delete content";
        $content->project_path = $path;
        $right_part = '<p>You are deleting '.l($content->title, "project/$path/content/{$content->title_path}").' from project. This step can not be revoked.</p>';
        $project->right_part = $right_part.drupal_get_form('project_content_remove_form', $content);
        return theme('project_manage_page',$project);
    }else{
        drupal_not_found();
    }
    
}

/** Content edit form **/
function project_content_edit_form(&$form_state, $content){

    $form['id'] = array(
        '#type'             => 'hidden',
        '#value'            => isset($content->id)?$content->id:0,
    );
    $form['nid'] = array(
        '#type'             => 'hidden',
        '#value'            => isset($content->node->nid)?$content->node->nid:0,
    );
    $form['type'] = array(
        '#type'             => 'hidden',
        '#value'            => isset($content->type)?$content->type:'default',  
    );
    $form['project'] = array(
        '#type'             => 'hidden',
        '#value'            => $content->project,  
    );
    $form['title'] = array(
        '#type'             => 'textfield',
        '#title'            => t('Title'),
        '#required'         => TRUE,
        '#default_value'    => isset($content->node->title)?$content->node->title:'',
    );
    $options = array(
        'PUBLIC' => 'Public -- can be viewed by everyone',
        'CONFIDENTIAL' => 'Confidential -- only viewed by researchers',
    );
    $form['permission'] = array(
        '#type'             => 'select',
        '#title'            => 'View permission',
        '#options'        => $options,
        '#default_value'    => isset($content->permission)?$content->permission:'PUBLIC',  
    );
    $form['body'] = array(
        '#type'             => 'textarea',
        '#title'            => t('Content'),
        '#default_value'    => isset($content->node->body)?$content->node->body:'',
        '#required'         => TRUE,
    );
    $form['submit'] = array(
        '#type'             => 'submit',
        '#value'            => t('Save'),
    );
    return $form;
}
function project_content_edit_form_submit($form, &$form_state){
    $id = $form_state['values']['id'];
    $nid = $form_state['values']['nid'];
    $type = $form_state['values']['type'];
    $project_id = $form_state['values']['project'];
    $permission = $form_state['values']['permission'];
    $title = $form_state['values']['title'];
    $body = $form_state['values']['body'];
    
    if($id<>0&&$nid<>0){
        //load $node
        $node = node_load($nid);
        $node->title = $title;
        $node->body = $body;
        node_save($node);
    
        //update {research_projects_contents}
        $update = 'UPDATE {research_projects_contents} SET permission="%s", changed=NOW() WHERE id=%d';
        db_query($update, $permission, $id);
    }else{
        $node = new stdClass;
        $node->type = 'projectcontent';
        $node->title = $title;
        $node->body = $body;
        node_save($node);
        project_add_content($project_id, $node->nid, $permission, 'default', 1);
    }
    drupal_goto(get_project_url($project_id).'/manage/contents');

}

/** Remove content form **/
function project_content_remove_form(&$form_state, $content){
    $form['nid'] = array(
        '#type'      => 'hidden',
        '#value'     => $content->node,
    );
    $form['pid'] = array(
        '#type'      => 'hidden',
        '#value'     => $content->project,
    );
    $form['submit'] = array(
        '#type'      => 'submit',
        '#value'     => 'Remove',
        '#suffix'    => l('Or cancel', 'project/'.$content->project_path.'/manage/contents'),
    );
    return $form;
}
function project_content_remove_form_submit($form, &$form_state){
    $nid = $form_state['values']['nid'];
    $pid = $form_state['values']['pid'];
    project_remove_content($pid, $nid);
    drupal_goto(get_project_url($pid).'/manage/contents');
}