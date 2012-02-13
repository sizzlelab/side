<?php

// $Id$
/**
 * Template to display project manage home page.
 *
 * Fields available:
 * $blocks, array
 */
?>
<div id="project-manage-div-1" class="project-manage-block-div">
<div class="title"><h2>Manage categories</h2></div>
<div class="project-manage-categories-div">
    <?php foreach($blocks['categoris'] as $category): ?>
    <div class="project-manage-categorie-item-div">
        <div class="item-left-div">
            <a href="<?=url($category['link']);?>"><image src="<?=$category['image']; ?>"/></a>
        </div>
        <div class="item-right-div">
            <dl>
                <dt><a href="<?=url($category['link']);?>"><?=$category['name']; ?></a></dt>
                <dd><?=$category['count']; ?></dd>
            </dl>
        </div>
    </div>
    <?php    endforeach;?>
    <div style="clear:both"></div>
</div>
</div>
<?php foreach($blocks['others'] as $key => $b): ?>
<div id="project-manage-div-<?=$key?>" class="project-manage-block-div">
    <div class="title"><h2><?=$b['title']?></h2></div>
    <div class="project-manage-categories-div">
        <?=$b['content']?>
        <p class="more-link"><a href="<?=$b['more']?>">More</a></p>
    </div>
</div>
<?php endforeach; ?>

