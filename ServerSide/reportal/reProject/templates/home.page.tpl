<?php
// $Id$
/**
 * ##################Not use now#######################
 * Template to display project home page.
 *
 * Fields available:
 * $project, the project object
 */
?>
<div class="project-introduction-div"><?=$project->homepage->body;?></div>

<!-- start part of Research tools -->
<div id="research-tools-div">
<h3>Research tools</h3>
<div class="research-tool-blocks">
    <?php foreach($project->tools_list as $tool): ?>
    <div class="research-tool-block">
        <div class="research-tool-log-div">
            <img class="research-tool-logo" src="<?=$tool['logo']?>" alt="<?=$tool['name']?>"/>
        </div>
        <p class="research-tool-name-p"><a class="tab-window-link" href="<?=url("research/tools/{$tool['id']}")?>"><?=$tool['name']?></a></p>
        <ul>
            <li><a class="tab-window-link" href="<?=url("research/tools/{$tool['id']}")?>">Read more</a></li>
            
        </ul>
    </div>
    <?php endforeach; ?>
</div><!-- the end of class research-tool-blocks -->
</div>