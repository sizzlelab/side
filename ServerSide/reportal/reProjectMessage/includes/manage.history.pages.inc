<?php

/**
 * Menu callback for project/%/manage/message/history
 */
function project_manage_message_history_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Messages'), generate_project_url($path).'/manage/message');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'messages'; 
    
    //add js
    drupal_add_js(drupal_get_path('module','reProjectUserProfile').'/personalInfo.js');
    //add css
    drupal_add_css(drupal_get_path('module','reProjectMessage').'/message.css');
    drupal_add_css(drupal_get_path('module','reProjectUserProfile').'/personalInfo.css');
    
    $header = array(
        array('data' => t('Operator')),
        array('data' => t('Content')),
        array('data' => t('Time')),
    );
    
    //get person list
    $get = 'SELECT name, operator, content, LEFT(time,10) date, RIGHT(time,8) time FROM {research_projects_operation_logs} LEFT JOIN {users} ON uid=operator WHERE project='.$project->id.' AND type="message" ORDER BY time DESC';
    $res = pager_query($get, 30);
    $rows = array();
    while($log=db_fetch_object($res)){
        $rows[] = array(
            l($log->name, 'user/'.$log->operator, array('attributes'=>array('class'=>'show_person_card'))),
            $log->content,
            $log->date.'<br/>'.$log->time,
        );
    }
    
    $content = theme_table($header,$rows);
    $content.= theme('pager');
    
    $project->subtitle = t('Messages history');
    $project->right_part = $content;
    return theme('project_manage_page',$project);
}