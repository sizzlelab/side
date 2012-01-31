<?php
/**
 * Menu callback for researcher/projects/
 */
function researcher_projects_callback(){
    global $user;
    
    drupal_add_css(drupal_get_path('module','bindrePortal').'/bindrePortal.css');
    
    if($user->uid){
        $header = array(
            array('data'=>'Project'),
            array('data'=>''),
            array('data'=>'Joined date'),
            
        );
        $rows = array();
        
        $sql = 'SELECT project, LEFT(changed,10) changed FROM {research_projects_persons_list} WHERE user='.$user->uid.' AND role IN ("researcher", "creator") AND active=1 ORDER BY changed DESC ';
        $res = pager_query($sql, 10);
        $link_button_attr = array('attributes'=>array('class'=>'link_button'));
        while($r = db_fetch_object($res)){
            $project = load_project($r->project, 'id');
            /*$roles = is_project_participant($project->id, $user->uid);
            $roles_data = '';
            foreach($roles as $role){
                $roles_data.= ' '.$role;
            }*/
            if($project){
                $rows[] = array(
                    //l($project->name, generate_project_url($project->path)),
                    //$roles_data,
                    l($project->name, 'chart'),
                    l('View',generate_project_url($project->path),$link_button_attr).l('Manage',generate_project_url($project->path).'/manage',$link_button_attr),
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
            array('data'=>'Project code'),
            array('data'=>'Joined at'),
            
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
    $project = load_project($path);
    if($project){
        return theme('project_welcome_page', $project);
    }
    drupal_not_found();
}
