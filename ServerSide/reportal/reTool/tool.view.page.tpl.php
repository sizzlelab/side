<?php
// $Id$
/**
 * Template to display research tool page.
 *
 * Fields available:
 * $tool, the tool object
 */
?>
<p><a class="edit_link" href="<?=url('research/tools/view/'.$tool->id.'/edit'); ?>">Edit</a></p>
<div class="tool-mark-info-div"><?=$tool->projects; ?> projects using this tool</div>
<div class="tool-logo-div"><img src="<?=$tool->logo; ?>" alt="<?=$tool->name; ?>"/></div>
<div class="tool-introduction-div"><?=$tool->introduction; ?></div>

<h1>Links</h1>
<p>
    <a class="edit_link" href="<?=url('research/tools/view/'.$tool->id.'/links/edit'); ?>">Edit</a>
</p>
<div class="tool-links-div">
    <?php foreach($tool->links as $link): ?>
    <dl>
        <dt><?=$link->title;?></dt><dd><a href="<?=$link->url;?>" class="outer_link" target="_blank"><?=$link->url;?></a> </dd>
    </dl>
    <?php endforeach; ?>
</div>
