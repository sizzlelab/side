<?php
// $Id$
/**
 * Template to display participants list table
 *
 * Fields available:
 * $rows
 */
?>
<table class="participants-list-table" cellpadding=1>
    <!-- header part -->
    <tr>
        
    </tr>
    
    <?php foreach($rows as $r):?>
    <tr>
        <td><?=$r->name;?></td>
        <td><?=$r->email?></td>
        <td><?=$r->name?></td>
    </tr>
    <?php endforeach; ?>
        
</table>