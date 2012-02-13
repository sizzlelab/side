<?php

/**
 * Menu callback for project/%/content/%
 */
function project_content_display_callback($path, $title){
    $project = load_project($path);
    
    if(!$project){
        drupal_not_found();
        exit;
    }
    
    $node = load_project_content_by_title($project->id, $title);
    
    if($node){
        $breadcrumb = drupal_get_breadcrumb();
        $breadcrumb[] = l($project->name, generate_project_url($path));
        $breadcrumb[] = l('Pages',generate_project_url($path).'/contents');
        drupal_set_breadcrumb($breadcrumb);
        
        if($node->content_type == 'homepage'){
            drupal_goto(generate_project_url($path));
            exit;
        }
        
        if(variable_get('open_g_translation_widget', 'disable')=='enable'){
            return add_translation_widget($node->body);
        }else{
            return $node->body;
        }
        
    }else{
        drupal_not_found();
        exit;
    }
}

/**
 * Menu callback for project/%/contents
 */
function project_contents_list_callback($path){
    $project = load_project($path);
    if($project){
        
        $breadcrumb = drupal_get_breadcrumb();
        $breadcrumb[] = l($project->name, generate_project_url($path));
        drupal_set_breadcrumb($breadcrumb);
        
        global $user;
        //check the role of current user
        if(is_project_researcher($project->id, $user)){
            //fetch all contents list
            $get = 'SELECT SUBSTRING(title, 1, 1) AS first_letter, title_path, title FROM {research_projects_contents} AS c, {node} AS n '
                  .'WHERE c.project=%d AND n.nid = c.node ORDER BY title';
        }else{
            //fetch public contents
            $get = 'SELECT SUBSTRING(title, 1, 1) AS first_letter, title_path, title FROM {research_projects_contents} AS c, {node} AS n '
                  .'WHERE c.project=%d AND c.permission="PUBLIC" AND  n.nid = c.node ORDER BY title';
        }
        
        $res = db_query($get, $project->id);
        $content = '';
        while($r = db_fetch_object($res)){
            if(!isset($first_letter)){
                $first_letter = $r->first_letter;
                $content.= "<h2>$first_letter</h2><ul>"; 
            }elseif($r->first_letter!=$first_letter){
                $first_letter = $r->first_letter;
                $content.= "</ul><h2>$first_letter</h2><ul>"; 
            }
            $content.= '<li>'.l($r->title, generate_project_url($path).'/content/'.$r->title_path).'</li>';
        }
        if(empty ($content)){
            $content = '<p>No pages can be found.</p>';
        }else{
            $content.= '</ul>';
        }
        return $content;
        
    }else{
        drupal_not_found();
    }
}