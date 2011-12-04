<?php

/*
 * Template for display welcome page for each project 
 * 
 * Fields available:
 * $project, the project object
 */
global $user;

$code = get_project_code($project->id, $user->uid);
if(!$code){
    drupal_goto(generate_project_url($project->path));
    exit;
}

?>
<p></p>
<p>Your project code is：<?=$code;?></p>
<p>You will need to use it after you installing the data collection tool Physical Activity.</p>
<p>You can also find it from My SIDE -> <?=l('My projects', 'person/projects')?> page.</p>

<p>After you collect and upload the data, you can see them on My SIDE -> <?=l('My observation','person_chart')?></p>

<p>Now watch the <a href="https://docs.google.com/open?id=0B2KpyE-jGPf9ZjZmYmIzZmQtMWU2ZS00MjA4LWFmNDgtZTk2NzBkMGNmMDE4" target="_blank">guide video</a> first and start your healthy life journey with Get&Check!</p>

<p><?=l('Visit project home page', generate_project_url($project->path))?></p>
