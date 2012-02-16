<?php
// $Id$
/**
 * Template for personlist.
 *
 * Fields available:
 * $list, the array list of person info
 * $type, 'mail' or 'sms'
 */
?>
<div style="padding-left:8px;"><input type="checkbox" id="select-all-checkbox" value="all" />Send to all</div>
<div class="project-person-list-div">
    <table>
        <?php
            $td=1;
            foreach($list as $p){
                if($td==1){
                    echo "<tr>";
                }
        ?>
        <td class='project-person-td' id='project-person-<?=$p->id; ?>-td' >
                <?php if($type=='mail'): ?>
                    <?php if(!empty($p->email)): ?>
                        <input class="person-input-checkbox" type="checkbox" name='person-<?=$p->id; ?>' value='<?=$p->email?>' id="project-person-<?=$p->id; ?>"> <label for="project-person-<?=$p->id; ?>"><?=$p->name; ?></label>
                        <div>(<?=$p->email; ?>)</div>
                    <?php else: ?>
                        <div>
                            <input disabled="disabled" type="checkbox" name='person-<?=$p->id; ?>' value='<?=$p->email?>'> <?=$p->name; ?>
                        </div>
                        <div>(No email address.)</div>
                    <?php endif; ?>
                <?php elseif($type=='sms'): ?>
                    <?php if(!empty($p->phone)): ?>
                        <input class="person-input-checkbox" type="checkbox" name='person-<?=$p->id; ?>' value='<?=$p->phone?>' id="project-person-<?=$p->id; ?>"> <label for="project-person-<?=$p->id; ?>"><?=$p->name; ?></label>
                        <div>(<?=$p->phone; ?>)</div>
                    <?php else: ?>
                        <div>
                            <input disabled="disabled" type="checkbox" name='person-<?=$p->id; ?>' value='<?=$p->phone?>'> <?=$p->name; ?>
                        </div>
                        <div>(No phone number.)</div>
                    <?php endif; ?>
                <?php endif ?>
        </td>
        <?php
                if($td==4){
                    echo "</tr>";
                    $td=0;
                }
                $td++;
            }
            if($td>1){
                while($td<=4){
                    echo "<td></td>";
                    $td++;
                }
                echo "</tr>";
            }
           
        ?>
    </table>
    
</div>

