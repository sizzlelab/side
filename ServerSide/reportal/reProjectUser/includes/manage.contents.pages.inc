<?php

/**
 * Menu callback for project/%/manage/contents/termofuse
 */
function project_manage_content_termofuse_callback($path){
    //get project
    $project = load_project($path);
    if($project){        
        $termofuse = load_term_of_use_node($project);
        $project->termofuse = $termofuse;
        $termofuse->project = $project->id;
        $content.= drupal_get_form('project_content_edit_form', $termofuse);
        return $content;
    }
    drupal_not_found();
}

