<?php
/**
 * Menu callback for researcher/projects/
 */
function researcher_projects_callback(){
    global $user;
    if($user->uid){
        $header = array(
            array('data'=>'Project name'),
            array('data'=>'Roles'),
            array('data'=>'Join at'),
            
        );
        $rows = array();
        
        $sql = 'SELECT project, changed FROM {research_projects_persons_list} WHERE user='.$user->uid.' AND role IN ("researcher", "creator") AND active=1 ';
        $res = pager_query($sql, 10);
        while($r = db_fetch_object($res)){
            $project = load_project($r->project, 'id');
            $roles = is_project_participant($project->id, $user->uid);
            $roles_data = '';
            foreach($roles as $role){
                $roles_data.= ' '.$role;
            }
            
            if($project){
                $rows[] = array(
                    l($project->name, generate_project_url($project->path)),
                    $roles_data,
                    $r->changed,
                );
            }
        }
        $content.= theme_table($header, $rows);
        $content.= theme_pager();
        return $content;
        
    }
    
}

/**
 * Menu callback for person/projects
 */
function person_projects_callback(){
    global $user;
    if($user->uid){
        $header = array(
            array('data'=>'Project name'),
            array('data'=>'Rroject code'),
            array('data'=>'Join at'),
            
        );
        $rows = array();
        
        $sql = 'SELECT project, changed FROM {research_projects_persons_list} WHERE user='.$user->uid.' AND role="participant" AND active=1 ';
        $res = pager_query($sql, 10);
        while($r = db_fetch_object($res)){
            $project = load_project($r->project, 'id');
            $code = get_project_code($project->id, $user->uid);
            
            if($project){
                $rows[] = array(
                    l($project->name, generate_project_url($project->path)),
                    $code,
                    $r->changed,
                );
            }
        }
        $content.= theme_table($header, $rows);
        $content.= theme_pager();
        return $content;
        
    }
}

/**
 * Menu callback for project/%/help
 */
function project_welcome_page_callback($path){
    $project = load_project($project->path);
    if($project){
        return theme('project_welcome_page', $project);
    }
    drupal_not_found();
}