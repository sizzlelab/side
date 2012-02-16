<?php
// $Id$
/**
 * Template for invite page div
 *
 * Fields available:
 * $person, object
 * $invite_message, string
 */
?>
<div class="invite-dialog-div">
    <p class="title">Hello<?=isset($person->username)?' '.$person->username:'';?>,</p>
    <p class="message"><?=$invite_message;?></p>
    <div class='invite-forms'>
    <?php if(isset($person->exiting_user_join_form) ): ?>
        <?=$person->exiting_user_join_form; ?>
    <?php endif; ?>
    <?php if(isset($person->login_to_join_form) ): ?>
        <?=$person->login_to_join_form; ?>
    <?php endif; ?>
     <?php if(isset($person->register_to_join_form) ): ?>
        <?=$person->register_to_join_form; ?>
    <?php endif; ?>
    </div>
</div>