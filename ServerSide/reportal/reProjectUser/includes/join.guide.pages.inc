<?php

/**
 * Menu callback for project/%/termofuse/%
 */
function project_termofuse_callback($path, $inviteCode){
    //get project
    $project = load_project($path);
    if($project){
        
        $breadcrumb = drupal_get_breadcrumb();
        $breadcrumb[] = l($project->name, generate_project_url($path));
        drupal_set_breadcrumb($breadcrumb);
        
        $termofuse = load_term_of_use_node($project);
        $project->termofuse = $termofuse;
        return $termofuse->body;
    }
    drupal_not_found();
}