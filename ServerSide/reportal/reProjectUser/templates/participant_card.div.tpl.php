<?php
// $Id$
/**
 * Template to display participant info
 *
 * Fields available:
 * $row
 * $path
 * $selected_tag
 */
?>
<?php foreach($row as $key=>$r): ?>
    <?php if($key=='tags'): ?>
    <div class="participant-card-tags-div">
        <div class="title-div">Tags</div>
        <div class="participant-card-tags-list-div">
            <?php foreach($r['value'] as $tag): ?>
            <div class='participant-tag<?=($tag==$selected_tag?' selected_tag':'');?>' ><a href='<?=url("project/$path/manage/persons/filter/$tag");?>'><?=$tag; ?></a></div>
            <?php endforeach; ?>
        <div style="clear:both"></div>
        </div>
    </div>
    <?php elseif($key=='notes'): ?>
    <div class="participant-card-notes-div">
        <div class="title-div"><?=$r['name']?></div>
        <div class="notes-content-div"><?=nl2br($r['value']);?></div>
        <div style="clear:both"></div>
    </div>
    <?php else: ?>
    <div class="participant-card-<?=$key?>-div">
        <span class="title-span"><?=$r['name']?></span><?=$r['value']?>
    </div>
    <?php endif; ?>
<?php endforeach; ?>




