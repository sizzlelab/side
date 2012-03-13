<?php

/**
 * Menu callback for project/%/manage/persons/tagsget/%
 */
function ajax_get_participant_tags_callback($path, $participant){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
   
    $project = load_project($path);
    $all_tags = get_project_all_participants_tags($project->id);
    $tags = get_participant_all_tags($participant, $project->id);
    $result = array(
        'tags' => $tags,
        'all'  => $all_tags,
    );
    print json_encode($result);

}

/**
 * Menu callback for project/%/manage/persons/tagsadd/%/%
 */
function ajax_add_participant_tag_callback($path, $participant, $tag){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Persons'), generate_project_url($path).'/manage/persons');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    $project = load_project($path);
    add_tag_to_participant($project->id, $participant, $tag);
    
    $all_tags = get_project_all_participants_tags($project->id);
    $tags = get_participant_all_tags($participant, $project->id);
    $result = array(
        'tags' => $tags,
        'all'  => $all_tags,
    );
    print json_encode($result);
}

/**
 * Menu callback for project/%/manage/persons/tagsremove/%/%
 */
function ajax_remove_participant_tag_callback($path, $participant, $tag){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    $project = load_project($path);
    remove_tag_from_participant($project->id, $participant, $tag);
    
    $all_tags = get_project_all_participants_tags($project->id);
    $tags = get_participant_all_tags($participant, $project->id);
    $result = array(
        'tags' => $tags,
        'all'  => $all_tags,
    );
    print json_encode($result);
}

/**
 * Menu callback for project/%/manage/persons/tags/manage
 */
function manage_participant_tags_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    $breadcrumb[] = l(t('Persons'), generate_project_url($path).'/manage/persons');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    $project->subtitle = 'Manage tags';
    
    $rows = array();
    $header = array(
        array('data'=>'Tag name'),
        array('data'=>'Statistics'),
        array('data'=>'Operation'),
    );
    
    $participants_count = count_participants_by_project($project->id);
    
    $all_tags = get_project_all_participants_tags($project->id);
    foreach($all_tags as $tag){
        $tag_count = count_participants_by_tag($project->id, html_entity_decode($tag['tag']));
        $rows[] = array(
            $tag['tag'],
            $tag_count.'/'.$participants_count.' ('.number_format($tag_count/$participants_count*100, 2).'%)',
            '<a class="delete_link" href="'.url("project/$path/manage/persons/tags/remove/{$tag['tag']}").'">Remove</a>',
        );
    }
    
    $project->right_part = theme_table($header, $rows);    
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/persons/tags/remove/% 
 */
function manage_participant_tag_remove_callback($path, $tag){
    $project = prepare_project_manage_page($path);
    
    //get participants
    $participants = get_project_all_participants($project->id);
    
    //remove tag from $participants
    foreach($participants as $p){
        remove_tag_from_participant($project->id, $p->id, $tag);
    }
    
    //delete tag from project
    $delete = 'DELETE FROM {research_projects_participants_tags} WHERE project=%d AND tag="%s"';
    db_query($delete, $project->id, $tag);
    drupal_goto("project/$path/manage/persons/tags/manage");
}

