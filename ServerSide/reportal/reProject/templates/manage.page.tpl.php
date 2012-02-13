<?php
// $Id$
/**
 * Template to display project manage page.
 *
 * Fields available:
 * $project, the project object
 */
?>
<div id="project-manage-div">
<div class="left-navigation-div">
    <?php foreach($project->navigation as $key => $n): ?>
    <div class="manage-item-tag-div<?=($project->path_category==$key?' selected':'')?>">
        <div class='manage-category-div'><a href="<?=url($n['url'])?>" class='<?=$n['class']?> manage-item-a'><?=$n['title']?></a></div>
        <?php if(isset($n['sub_items'] )): ?>
        <ul class='manage-item-sub-ul'>
        <?php foreach($n['sub_items'] as $sub_k => $sub_n): ?>
            <li <?=(($project->path_category_subitem==$sub_k&&$project->path_category==$key)?'class="selected"':'')?>><a href="<?=url($sub_n['url'])?>" class='<?=$sub_n['class']?> manage-subitem-a'><?=$sub_n['title']?></a></li>
        <?php endforeach; ?>
        </ul>
        <?php endif;?>
    </div>
    <?php endforeach; ?>
</div>
<div class="right-content-div">
    <h2 class="sub-title"><?=$project->subtitle; ?></h2>
    <?=$project->right_part; ?>
</div>
<div style="clear:both"></div>
</div>