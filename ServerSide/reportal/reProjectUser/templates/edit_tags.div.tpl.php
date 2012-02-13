<?php
// $Id$
/**
 * Template to display edit tags div
 *
 * Fields available:
 * $project
 */
?>
<div id='edit-tags-div' title='Edit tags' style='display:none;' >
    <div id='participants-tags-div'>
        <div class='participant-tag used-tag'>Male</div><div class='participant-tag'>Male</div><div class='participant-tag'>Male</div>
        <div style='clear:both'></div>
    </div>
    <div id='participants-tags-loading-div' class='loading-div' style='display:none;'></div>
    <hr style='width: 98%; border:0; height: 2px; background-color:#777;'/>
    <div id='new-tags-div' class='form-item'>
        <form action='' method='POST'>
            <input type="hidden" value="" name="participant-input" id="edit-participant-input" />
            <p>New tag: <input type='text' name='new-tag' id="new-tag-input" size='20' class="form-text" /><input type='button' value='Add' class="form-submit" onclick="onclick_add_new_tag();" /></p>
        </form>
    </div>
    <div id='all-tags-div'>
        <div class='participant-tag'>Male</div><div class='participant-tag'>Male</div><div class='participant-tag'>Male</div>
        <div style='clear:both;'></div>
    </div>
    <div id='all-tags-loading-div' class='loading-div' style='display:none;'></div>
    <p><a href="<?=url("project/{$project->path}/manage/persons/tags/manage"); ?>" class="edit_link">Manage tags</a></p>
</div>