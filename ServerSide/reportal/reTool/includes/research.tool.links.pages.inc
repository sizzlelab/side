<?php

/**
 * Menu callback for research/tools/view/%/links/add
 */
function research_tools_links_add_callback($tool_id){
    $link = new stdClass;
    $link->tool = $tool_id;
    $content.= drupal_get_form('research_tool_link_form', $link);
    return $content;
}

/**
 * Menu callback for research/tools/view/%/links/edit
 */
function research_tools_links_edit_callback($tool_id){
    //get tool name
    $get_name = 'SELECT name FROM {research_tools} WHERE id=%d';
    $name = db_result(db_query($get_name, $tool_id));
    if($name){
        $breadcrumb = drupal_get_breadcrumb();
        $breadcrumb[] = l('Tools', 'research/tools/');
        $breadcrumb[] = l($name, 'research/tools/view/'.$tool_id);
        drupal_set_breadcrumb($breadcrumb);
        
        //get tool links
        $get = "SELECT id, title, url FROM {research_tool_links} WHERE tool=%d ORDER BY created";
        $res = db_query($get, $tool_id);
        $links = array();
        while($row = db_fetch_object($res)){
            $links[] = $row;
        }
        return drupal_get_form('research_tool_links_edit_form',$links, $tool_id);
    }
    
    drupal_not_found();
}


/** tool link form **/
function research_tool_link_form(&$form_state, $link){
    
    
    $form['id'] = array(
        '#type'     => 'hidden',
        '#value'    => isset($link->id)?$link->id:0,
    );
    $form['tool'] = array(
        '#type'     => 'hidden',
        '#value'    => isset($link->tool)?$link->tool:0,
    );
    $form['title'] = array(
        '#type'     => 'textfield',
        '#title'    => t('Link title'),
        '#required' => TRUE,
        '#default_value'    => isset($link->title)?$link->title:'',
    );
    $form['url'] = array(
        '#type'     => 'textfield',
        '#title'    => t('URL'),
        '#required' => TRUE,
        '#default_value'    => isset($link->url)?$link->url:'',
        '#description' => t("The URL should be a full URL address. e.g. Starts with 'http://', 'https://' or 'ftp://'"),
    );
    $form['submit'] = array(
        '#type'     => 'submit',
        '#value'    => t('Save'),
    );
    if(isset($link->id)&&$link->id>0){
        $form['submit']['#suffix']=l('Delete', "research/tools/view/{$link->tool}/links/delete/{$link->id}");
    }
    return $form;
}
function research_tool_link_form_submit($form, &$form_state){
    
    $id = (int)$form_state['values']['id'];
    $tool = (int)$form_state['values']['tool'];
    $title = $form_state['values']['title'];
    $url = $form_state['values']['url'];
    
    if($id>0){
        //update record
        $update = "UPDATE {research_tool_links} SET title='%s', url='%s', changed=NOW() WHERE id=%d AND tool=%d";
        db_query($update, $title, $url, $id, $tool);
    }else{
        //insert new
        $insert = "INSERT INTO {research_tool_links} (tool, title, url, created, changed) VALUES(%d, '%s', '%s',NOW(), NOW())";
        db_query($insert, $tool, $title, $url);
    }
    drupal_goto('research/tools/view/'.$tool);
}

/************ research tool links form ****************/
function research_tool_links_edit_form(&$form_state, $links, $tool){
    $form['description'] = array(
        '#value' => '<div class="description">'.t('To delete the link, make the title and url empty').'</div>',
    );
    foreach ($links as $l) {
        $form['link-'.$l->id]['id-'.$l->id] = array(
            '#type' => 'hidden',
            '#value'=> $l->id,
        );   
        $form['link-'.$l->id]['title'.$l->id] = array(
            '#type'     => 'textfield',
            '#title'    => t('Link title'),
            '#default_value' => $l->title,
        );
        $form['link-'.$l->id]['url'.$l->id] = array(
            '#type'     => 'textfield',
            '#title'    => t('URL'),
            '#description' => t("The URL should be a full URL address. e.g. Starts with 'http://', 'https://' or 'ftp://'"),
            '#default_value' => $l->url,
        );
    }
    $form['tool'] = array(
        '#type' => 'hidden',
        '#value'=> $tool,
    );

    $form['link-new']['title'] = array(
        '#type'     => 'textfield',
        '#title'    => t('New Link title'),
    );
    $form['link-new']['url'] = array(
        '#type'     => 'textfield',
        '#title'    => t('New URL'),
        '#description' => t("The URL should be a full URL address. e.g. Starts with 'http://', 'https://' or 'ftp://'"),
    );
    $form['submit'] = array(
        '#type'     => 'submit',
        '#value'    => t('Save'),
    );
    
    return $form;
}
function research_tool_links_edit_form_validate($form, &$form_state){
    foreach($form_state['values'] as $key => $value){
        if(strstr($key, 'id-')){
            $update_title = $form_state['values']['title'.$value];
            $update_url = $form_state['values']['url'.$value];
            
            if(empty($update_title)&&!empty($update_url)){
                form_set_error('title'.$value, 'The title is required.');
            }
            if(!empty($update_title)&&empty($update_url)){
                form_set_error('url'.$value, 'The url is required.');
            }
        }
    }
    
    $new_title = $form_state['values']['title'];
    $new_url = $form_state['values']['url'];
    
    if(empty($new_title)&&!empty($new_url)){
        form_set_error('title', 'Please input a title form the new link.');
    }
    if(!empty($new_title)&&empty($new_url)){
        form_set_error('url', 'Please input a url form the new link.');
    }
}
function research_tool_links_edit_form_submit($form, &$form_state){
    $tool = $form_state['values']['tool'];
    
    //get links
    foreach($form_state['values'] as $key => $value){
        if(strstr($key, 'id-')){
            $update_title = $form_state['values']['title'.$value];
            $update_url = $form_state['values']['url'.$value];
            
            if(empty($update_title)&&empty($update_url)){
                //remove the link
                $delete = 'DELETE FROM {research_tool_links} WHERE id=%d AND tool=%d';
                db_query($delete, $value, $tool);
            }else{
                //update record
                $update = "UPDATE {research_tool_links} SET title='%s', url='%s', changed=NOW() WHERE id=%d AND tool=%d";
                db_query($update, $update_title, $update_url, $value, $tool);
            }  
        }
    }
    
    //insert new key if there is
    $new_title = $form_state['values']['title'];
    $new_url = $form_state['values']['url'];
    
    if(!empty($new_title)&&!empty($new_url)){
        //insert new
        $insert = "INSERT INTO {research_tool_links} (tool, title, url, created, changed) VALUES(%d, '%s', '%s',NOW(), NOW())";
        db_query($insert, $tool, $new_title, $new_url);
    }
    
    drupal_set_message('Change has been saved');
    $form_state['redirect'] = 'research/tools/view/'.$tool;
    
}