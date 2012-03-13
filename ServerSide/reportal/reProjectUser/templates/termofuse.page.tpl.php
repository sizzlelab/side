<?php
// $Id$
/**
 * Template to display term of use.
 *
 * Fields available:
 * $project, the project object
 */
?>
<div class='node-content-div'><?=$project->termofuse->body;?></div>
<div class='join-button-div' style="margin-top: 20px;">
    <p><a href="<?=url("project/$project->path/firstguide");?>" style="font-size:20px; font-weight:bold; padding: 10px 20px; border: 1px solid #777; -moz-border-radius: 5px;-webkit-border-radius: 5px;border-radius: 5px;-khtml-border-radius: 5px;">I want join</a></p>
</div>