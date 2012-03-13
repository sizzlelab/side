<?php
/**
 * Menu callback for project/%/manage/message
 */
function project_manage_message_callback($path){
    $project = prepare_project_manage_page($path);
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l($project->name, generate_project_url($path));
    $breadcrumb[] = l(t('Manage'), generate_project_url($path).'/manage');
    drupal_set_breadcrumb($breadcrumb);
    
    drupal_add_css(drupal_get_path('module','reProjectMessage').'/message.css');
    
    $project->path_category = 'manage';
    $project->path_category_subitem = 'messages'; 
    
    $image_folder = base_path().drupal_get_path('module','reProjectMessage').'/images';
    $message_path = url(generate_project_url($project->path).'/manage/message');
    $content = '<div id="message-selections">'
                ."<h3><a href='$message_path/email'><img src='$image_folder/mail-big.png'><br>Send Email</a></h3>"
                ."<h3><a href='$message_path/sms'><img src='$image_folder/sms-big.png'><br>Short Message</a></h3>"
                ."<h3><a href='$message_path/history'><img src='$image_folder/history-big.png'><br>Messages history</a></h3>"
               .'<div>';
                //."<li><a href='$message_path'>Send Email</a></li>"
                //.'<li onclick="javascript:window.location.href='."'$message_path/email'".'">'
                //    .'<img src="'.$image_folder.'mail-big.png" alt="email"/></a></li>'
                //.l('Email',generate_project_url($project->path).'/manage/message/email').'</li>'
               //.'<li onclick="javascript:window.location.href='."'$message_path/email'".'">'
               // .'<img src="'.$image_folder.'sms-big.png" alt="short message"/><br>'
               // .l('SMS',generate_project_url($project->path).'/manage/message/sms').'</li><li>'
               // .'<img src="'.$image_folder.'history-big.png" alt="history"/><br>'
               // .l('Message history',generate_project_url($project->path).'/manage/message/history')
               //.'</li></div>';
    
    $project->subtitle = t('Send messages');
    $project->right_part = $content;
    return theme('project_manage_page',$project);
}