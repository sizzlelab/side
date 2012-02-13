<?php

/**
 * Menu callback for project/%/manage/persons
 */
function project_manage_persons_list_callback($path){
    $project = prepare_project_manage_page($path);
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    //add css
    drupal_add_css(drupal_get_path('module','reProjectUser').'/css/user_manage.css');
    drupal_add_css(drupal_get_path('module','reProjectUserProfile').'/personalInfo.css');
    //add js
    drupal_add_js(drupal_get_path('module','reProjectUserProfile').'/personalInfo.js');
    //add jQuery ui
    load_jquery_ui();
    drupal_add_js(drupal_get_path('module','reProjectUser').'/js/edit_tags_window.js');
    
    //add ajax link Drupal.settings.reProjectUser.base_path
    drupal_add_js(array('reProjectUser'=>array('base_path'=>url('project/'.$path.'/manage/persons/'))),'setting');
    //get right part info
    
    //get researchers
    $content.= '<h2>Researchers list</h2>';
    $content.= '<p><a href="'.url('project/'.$path.'/manage/persons/researcher/add').'" class="add_link">Add researcher</a></p>';
    $rows = array();
    $header = array(
        //array('data'=>'Photo'),
        array('data'=>'Name'),
        array('data'=>'Email'),
    );
    $owner = user_load($project->creator);
    $rows[] = array(
        l($owner->name, 'user/'.$owner->uid, array('attributes'=>array('class'=>'show_person_card'))).' (creator)',
        $owner->mail,
    );
    
    $sql = "SELECT name, mail FROM {research_projects_persons_list} AS p, {users} AS u WHERE p.project=%d AND p.user=u.uid AND p.role='researcher' AND p.active=1";
    $res = db_query($sql, $project->id);
    while($row=db_fetch_object($res)){
        $rows[] = array(
            l($row->name, 'user/'.$row->uid),
            $row->mail,
        );
    }
    $content.= theme_table($header, $rows);
    
    $content.= '<p>&nbsp;</p>';    
    //get normal user
    $content.= '<h2>Participants list</h2>';
    $content.= '<p><a href="'.url("project/$path/manage/persons/participant/new").'" class="add_link">Add new participant</a>';    
    $content.= '<a href="'.url("project/$path/manage/persons/tags/manage").'" class="manage_link">Manage tags</a></p>';
    
    $rows = array();
    $header = array(
        array('data'=>'Name'),
        array('data'=>'Profile'),
        array('data'=>'Operations'),
    );
        
    $sql2 = "SELECT id, user, name, email, phone, tags, notes FROM {research_projects_participants_cards} WHERE project={$project->id} ORDER BY created DESC";
    $res = pager_query($sql2, 30);
    while($row=db_fetch_object($res)){
        $tags = get_tags_as_array($row->tags);
        $row->tags_array = $tags;
        
        $name_div = '<div class="participant-card-name-div">'.$row->name.'<br/><strong>(Unauthorized)</strong></div>';
        if($row->user!=null && $row->user >0 ){
            //check if user is active in the project
            $check = 'SELECT active FROM {research_projects_persons_list} WHERE project=%d AND user=%d AND role="participant"';
            if(db_result(db_query($check, $project->id, $row->user))==1){
                $name_div = '<div class="participant-card-name-div">'.l($row->name, 'user/'.$row->user).'</div>';
            }
        }
        
        $p_card = module_invoke_all('project_participant_card_info', $project->id, $row->user);
        $p_card['email'] = array(
            'name' => t('Email'),
            'value'=> $row->email,
            'weight'=> 0,
        );
        $p_card['phone'] = array(
            'name' => t('Phone'),
            'value'=> $row->phone,
            'weight'=> 0,
        );
        
        $p_card['tags'] = array(
            'name' => t('Tags'),
            'value'=> $row->tags_array,
            'weight'=> 9,
        );
        $p_card['notes'] = array(
            'name' => t('Notes'),
            'value'=> $row->notes,
            'weight'=> 10,
        );
        
        
        $rows[] = array(
            $name_div,
            array(
                'data'  => theme('participants_card_div', $p_card, $project->path),
            ),
            '<p class="operation-p"><a class="delete_link" href="'.url("project/$path/manage/persons/participant/remove/{$row->id}").'">Remove</a></p>'
            .'<p class="operation-p"><a class="edit_link" href="'.url("project/$path/manage/persons/participant/edit/{$row->id}").'">Edit</a></p>'
            .'<p class="operation-p"><a class="tags_link window_tab" href="javascript:void(0);" onclick="onclick_edit_tags('."'".$row->id."'".');" >Tags</a></p>',
        );
    }
    $content.= theme('participants_tags_div',$project);
    $content.= '<div class="project-manage-page-table-div">'.theme_table($header, $rows).'</div>';
    $content.= theme('pager');
    $project->right_part = $content;    
    return theme('project_manage_page',$project);
}



/**
 * Menu callback for project/%/manage/persons/filter/%
 */
function project_manage_persons_filter_callback($path, $tag){
    $project = prepare_project_manage_page($path); 
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    drupal_set_breadcrumb($breadcrumb);
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'persons'; 
    
    //add css
    drupal_add_css(drupal_get_path('module','reProjectUser').'/css/user_manage.css');
    drupal_add_css(drupal_get_path('module','reProjectUserProfile').'/personalInfo.css');
    //add js
    drupal_add_js(drupal_get_path('module','reProjectUserProfile').'/personalInfo.js');
    //add jQuery ui
    load_jquery_ui();
    drupal_add_js(drupal_get_path('module','reProjectUser').'/js/edit_tags_window.js');
    
    //add ajax link Drupal.settings.reProjectUser.base_path
    drupal_add_js(array('reProjectUser'=>array('base_path'=>url('project/'.$path.'/manage/persons/'))),'setting');
    //get right part info
    
    //get researchers
    $content.= '<h2>Researchers list</h2>';
    $content.= '<p><a href="'.url('project/'.$path.'/manage/persons/researcher/add').'" class="add_link">Add researcher</a></p>';
    $rows = array();
    $header = array(
        //array('data'=>'Photo'),
        array('data'=>'Name'),
        array('data'=>'Email'),
    );
    $owner = user_load($project->creator);
    $rows[] = array(
        l($owner->name, 'user/'.$owner->uid, array('attributes'=>array('class'=>'show_person_card'))).' (creator)',
        $owner->mail,
    );
    
    $sql = "SELECT name, mail FROM {research_projects_persons_list} AS p, {users} AS u WHERE p.project=%d AND p.user=u.uid AND p.role='researcher' AND p.active=1";
    $res = db_query($sql, $project->id);
    while($row=db_fetch_object($res)){
        $rows[] = array(
            l($row->name, 'user/'.$row->uid),
            $row->mail,
        );
    }
    $content.= theme_table($header, $rows);
    
    $content.= '<p>&nbsp;</p>';    

    //get normal user
    $content.= '<h2>Participants list</h2>';
    $content.= '<p><a href="'.url("project/$path/manage/persons/participant/new").'" class="add_link">Add new participant</a>';    
    $content.= '<a href="'.url("project/$path/manage/persons/tags/manage").'" class="manage_link">Manage tags</a>';
    $content.= '<a href="'.url("project/$path/manage/persons").'" class="reload_link">Reload all</a></p>';  
     

    $rows = array();
    $header = array(
        array('data'=>'Name'),
        array('data'=>'Profile'),
        array('data'=>'Operations'),
    );
        
    $sql2 = "SELECT id, name, email, phone, tags, notes FROM {research_projects_participants_cards} WHERE project=%d AND tags LIKE '%;$tag;%' ORDER BY created DESC";
    $res = db_query($sql2, $project->id, $tag);
    while($row=db_fetch_object($res)){
        $tags = get_tags_as_array($row->tags);
        $row->tags_array = $tags;
        
        $name_div = '<div class="participant-card-name-div">'.$row->name.'<br/><strong>(Unauthorized)</strong></div>';
        if($row->user!=null && $row->user >0 ){
            //check if user is active in the project
            $check = 'SELECT active FROM {research_projects_persons_list} WHERE project=%d AND user=%d AND role="participant"';
            if(db_result(db_query($check, $project->id, $row->user))==1){
                $name_div = '<div class="participant-card-name-div">'.l($row->name, 'user/'.$row->user).'</div>';
            }
        }
        
        $p_card = module_invoke_all('project_participant_card_info', $project->id, $row->user);
        $p_card['email'] = array(
            'name' => t('Email'),
            'value'=> $row->email,
            'weight'=> 0,
        );
        $p_card['phone'] = array(
            'name' => t('Phone'),
            'value'=> $row->phone,
            'weight'=> 0,
        );
        
        $p_card['tags'] = array(
            'name' => t('Tags'),
            'value'=> $row->tags_array,
            'weight'=> 9,
        );
        $p_card['notes'] = array(
            'name' => t('Notes'),
            'value'=> $row->notes,
            'weight'=> 10,
        );
        
        
        $rows[] = array(
            $name_div,
            array(
                'data'  => theme('participants_card_div', $p_card, $project->path, $tag),
            ),
            '<p class="operation-p"><a class="delete_link" href="'.url("project/$path/manage/persons/participant/remove/{$row->id}").'">Remove</a></p>'
            .'<p class="operation-p"><a class="edit_link" href="'.url("project/$path/manage/persons/participant/edit/{$row->id}").'">Edit</a></p>'
            .'<p class="operation-p"><a class="tags_link window_tab" href="javascript:void(0);" onclick="onclick_edit_tags('."'".$row->id."'".');" >Tags</a></p>',
        );
    }
    $content.= theme('participants_tags_div',$project);
    $content.= '<div class="project-manage-page-table-div">'.theme_table($header, $rows).'</div>';
    $project->right_part = $content;    
    return theme('project_manage_page',$project);
}

/**
 * Menu callback for project/%/manage/persons/info/%
 */
function project_manage_persons_info_callback($path, $info_card){
    //TODO: add person info page for manage
}

