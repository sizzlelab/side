<?php
// $Id$
/**
 * Template to display project home page.
 *
 * Fields available:
 * $project, the project object
 */
global $user;
?>
<p style="text-align: center"><?= $project->founder; ?></p>
<?php if (is_project_participant($project->id, $user->uid)): ?>
    <ul class="project-navigation-menu">
        <li><a href="<?= url('project/' . $project->path); ?>">Home</a></li>
        <li class='live'></li>
        <li><a href="<?= url('project/' . $project->path . '/contents'); ?>">Pages</a></li>
        <li class='live'></li>
        <?= (is_project_researcher($project->id, $user)) ? '<li>' . l('Manage', 'project/' . $project->path . '/manage') . '</li><li class="live"></li>' : '' ?>
        <li><a href="<?= url('project/' . $project->path . '/content/Help_page'); ?>" >Help</a></li>
    </ul>
<?php endif; ?>

<div class="project-introduction-div"><?= variable_get('open_g_translation_widget', 'disable') == 'enable' ? add_translation_widget($project->homepage->body) : $project->homepage->body; ?></div>

<!-- start part of Research tools -->
<div id="research-tools-div">
    <h3>Research tools</h3>
    <div class="research-tool-blocks">
        <?php foreach ($project->tools_list as $tool): ?>
            <div class="research-tool-block">
                <div class="research-tool-log-div">
                    <img class="research-tool-logo" src="<?= $tool->logo ?>" alt="<?= $tool->name ?>"/>
                </div>
                <p class="research-tool-name-p"><a class="tab-window-link" href="<?= url("research/tools/view/{$tool->id}") ?>" target='_blank'><?= $tool->name ?></a></p>
                <ul>
                    <li><a class="tab-window-link list_link" href="<?= url("research/tools/view/{$tool->id}") ?>" target='_blank'>Read more</a></li>
                    <?php foreach ($tool->links as $link): ?>
                        <li><a class="tab-window-link list_link" href="<?= $link->url; ?>" target='_blank'><?= $link->title; ?></a></li>
                    <?php endforeach; ?>
                </ul>
            </div>
        <?php endforeach; ?>
    </div><!-- the end of class research-tool-blocks -->
</div>
<!-- end part of Research tools -->

<!-- footer navigation -->
<div class="footer-navigation">
    <p>
        <? //=(is_project_researcher($project->id, $user))?l('Manage', 'project/'.$project->path.'/manage', array('attributes'=>array('class'=>'manage_link'))):''?>
        <? //=(is_project_participant($project->id, $user->uid))?l('Help', 'project/'.$project->path.'/content/Help_page', array('attributes'=>array('class'=>'help_link'))):''?>
    </p>
</div>