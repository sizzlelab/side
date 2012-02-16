<?php
// $Id$
/**
 * Template to display project introduction as a block.
 *
 * Fields available:
 * $block: contents of the block
 * $module_path: the path of module folder
 */
?>
<div class="project-intro-block-content">
    <div class="block-content" onclick="window.location.href='<?=$block['read_more'];?>';">
        <?=$block['content'];?>
    </div>
    <div class="block-footer">
        <ul>
            <li><a href="<?=$block['read_more'];?>">Read more</a></li>
        </ul>
    </div>
</div>