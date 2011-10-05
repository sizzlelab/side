<?php
// $Id$
/**
 * @file
 * Admin page callbacks for SIDE module.
 */

/**
 * Menu callback for admin/content/observations
 */
function list_observation_types_callback(){
    //get list of observation types
    $sql = "SELECT ido_type, name, description FROM {observation_type} ORDER BY ido_type";
    $res = pager_query($sql, 30);
    
    $header = array(
        "Observation type", "Description", "Actions",
    );
    
    while ($types = db_fetch_array($res)){
        $observation_edit_link = "admin/content/observations/edit/".$types['ido_type'];
        $observation_delete_link = "admin/content/observations/delete/".$types['ido_type'];
        $admin_keys_link = "admin/content/observations/".$types['ido_type']."/keys";
        
        $action = "";
        if(user_access("Edit observation type")){
            $action.= l('Edit',$observation_edit_link)." ";
        }
        
        if(user_access("Delete observation type")){
            $action.= l('Delete',$observation_delete_link)." ";
        }
        $action.= l('Keys',$admin_keys_link);
        
        $rows[] = array(
            array('data' => l($types['name'],$observation_edit_link)),
            array('data' => t($types['description'])),
            array('data' => $action),
        );
    }
    $create_observation_link = "admin/content/observations/create";
    $content = "<p>".l("+ Create new observation type",$create_observation_link)."</p>";
    $content.= theme('table', $header, $rows);
    $content.= theme('pager');
    return $content;
}


/**
 * Menu callback for admin/content/observations/create
 */
function create_observation_type_callback(){
    return drupal_get_form("observation_type_form");
}
/**
 * observation type form
 */
function observation_type_form(&$form_state,$o_id=0){
    if($o_id!=0){
        //get observation type
        $get_sql = "SELECT ido_type, name, description FROM {observation_type} WHERE ido_type=%d";
        $observation = db_fetch_object(db_query($get_sql,$o_id));
    }
    $form["id"] = array(
        "#type" => 'hidden',
        "#value" => isset($observation->ido_type)?$observation->ido_type:0,
    );
    $form["name"] = array(
        "#type" => "textfield",
        "#title" => t("Name"),
        "#description" => t('Name for observation type'),
        "#required" => TRUE,
        "#size" => "60",
        "#maxlength" => 45,
        "#default_value" => isset($observation->name)?$observation->name:"",        
    );
    $form["description"] = array(
        "#type" => "textarea",
        "#title" => t("Description"),
        "#description" => t('Short description for observation type'),
        '#cols' => 60,
        '#rows' => 5,
        "#default_value" => isset($observation->description)?$observation->description:"",  
    );
    $form["submit"] = array(
        "#type" => "submit",
        "#value" => "Save",
    );
    return $form;
}

/**
 * observation type form submit
 */
function observation_type_form_submit($form, &$form_state){
    
    $id = $form_state['values']['id'];
    $name = $form_state['values']["name"];
    $description = $form_state['values']["description"];
    
    if($id==0){
        //Insert new observation type to {observation_type} table
        $insert_sql = "INSERT INTO {observation_type}(name,description) VALUES('%s','%s')";
        db_query($insert_sql,$name,$description);
    }else{
        //Update record
        $update_sql = "UPDATE {observation_type} SET name='%s', description='%s' WHERE ido_type=%d";
        db_query($update_sql,$name,$description,$id);
    }
    drupal_goto('admin/content/observations');
}


/**
 * Menu callback for admin/content/observations/edit/%d
 */
function edit_observation_type_callback($o_id){
    return drupal_get_form("observation_type_form",$o_id);
}

/**
 * Menu callback for admin/content/observations/delete/%d
 */
function delete_observation_type_callback($o_id){
    //get observation info
    $sql = "SELECT name FROM {observation_type} WHERE ido_type=%d";
    $name = db_result(db_query($sql,$o_id));
    $text = "<p>You are delete this observation type: ".$name."</p>";
    $cancel_link = "admin/content/observations";
    return $text.drupal_get_form("observation_delete_form",$o_id,$cancel_link);
}

/**
 * observation delete form
 */
function observation_delete_form(&$form_state,$id,$cancel_link){
    $form['id'] = array(
        "#type" => "hidden",
        "#value" => $id,
    );
    $form['submit'] = array(
        "#type" => "submit",
        "#value" => "Delete it",
        '#suffix' => l(" Or cancel?",$cancel_link),
    );
    return $form;
}
/**
 * Handle submit action for delele form
 */
function observation_delete_form_submit($form, &$form_state){
    $id = $form_state['values']['id'];
    $delete_sql = "DELETE FROM {observation_type} WHERE ido_type=%d";
    db_query($delete_sql,$id);
    drupal_goto('admin/content/observations');
}

/**
 * Menu callback for admin/content/observations/%/keys
 */
function observation_type_keys_callback($o_id){
    //get observation info
    $get_observation_sql = "SELECT name FROM {observation_type} WHERE ido_type=%d";
    $o_name = db_result(db_query($get_observation_sql,$o_id));
    
    $content = "<h2>Observation type: ".$o_name."</h2>";
    
    $new_key_link = "admin/content/observations/".$o_id."/keys/create";
    $content.= "<p>".l("+ New key",$new_key_link)."</p>";
    
    //get keynames
    $get_keys_sql = "SELECT ido_keyname, keyname, unit, datatype, description FROM {observation_keyname} WHERE ido_type=%d ORDER BY ido_keyname";
    $res = db_query($get_keys_sql,$o_id);
    
    $header = array(
        "Keyname", "Unit", "Datatype", "Description", "Actions"
    );
    
    while ($types = db_fetch_array($res)){
        
        $key_edit_link = "admin/content/observations/$o_id/keys/edit/".$types["ido_keyname"];
        $key_delete_link = "admin/content/observations/$o_id/keys/delete/".$types["ido_keyname"];
        $action = "";
        if(user_access("Edit observation type key")){
            $action.= l('Edit',$key_edit_link)." ";
        }
        
        if(user_access("Delete observation type key")){
            $action.= l('Delete',$key_delete_link);
        }
        
        $rows[] = array(
            array('data' => t($types['keyname'])),
            array('data' => t($types['unit'])),
            array('data' => t($types['datatype'])),
            array('data' => t($types['description'])),
            array('data' => $action),
        );
    }
    
    $content.= theme('table', $header, $rows);
    return $content;
    
}

/**
 * Menu callback for admin/content/observations/%/keys/create
 */
function create_observation_type_key_callback($o_id){
    return drupal_get_form("observation_type_key_form",$o_id);
}

/**
 * Observation type key form
 */
function observation_type_key_form(&$form_state,$o_id,$key_id=0){
    if($key_id!=0){
        //get keyname infor
        $get_key_sql = "SELECT keyname, unit, datatype, description FROM {observation_keyname} WHERE ido_keyname=%d";
        $key = db_fetch_object(db_query($get_key_sql,$key_id));
    }
    
    $form['o_id'] = array(
        "#type" => "hidden",
        "#value" => $o_id,
    );
    $form['key_id'] = array(
        "#type" => "hidden",
        "#value" => $key_id,
    );
    $form['keyname'] = array(
        "#type" => "textfield",
        "#title" => t("Keyname"),
        "#description" => t('Name for key'),
        "#required" => TRUE,
        "#size" => "60",
        "#maxlength" => 45,
        "#default_value" => isset($key->keyname)?$key->keyname:"",         
    );
    $form['unit'] = array(
        "#type" => "textfield",
        "#title" => t("Unit"),
        "#description" => t('Unit for observation value'),
        "#required" => TRUE,
        "#size" => "60",
        "#maxlength" => 45,
        "#default_value" => isset($key->unit)?$key->unit:"",         
    );
    $datatype_options = drupal_map_assoc(array(
        "Int", "Float","Double", "String", "Boolean", "Real", "Datetime",  
    ));
    $form['datatype'] = array(
        "#type" => "select",
        "#title" => t("Datatype"),
        "#options" => $datatype_options,
        "#default_value" => isset($key->datatype)?$key->datatype:"String",
    );
    $form['description'] = array(
        "#type" => "textarea",
        "#title" => t("Description"),
        "#description" => t('Short description for key'),
        '#cols' => 60,
        '#rows' => 5,
        "#default_value" => isset($key->description)?$key->description:"",  
    );
    $form["submit"] = array(
        "#type" => "submit",
        "#value" => "Save",
    );
    return $form;
}


/**
 * Handle submit for observation type key form
 */
function observation_type_key_form_submit($form, &$form_state){
    $o_id = $form_state['values']['o_id'];
    $k_id = $form_state['values']['key_id'];
    $keyname = $form_state['values']['keyname'];
    $unit = $form_state['values']['unit'];
    $datatype = $form_state['values']['datatype'];
    $description = $form_state['values']['description'];
    
    if($k_id==0){
        //Insert new key
        $insert_sql = "INSERT INTO {observation_keyname}(ido_type,keyname,unit,datatype,description) VALUES(%d,'%s','%s','%s','%s')";
        db_query($insert_sql,$o_id,$keyname,$unit,$datatype,$description);
    }else{
        //Update key
        $update_sql = "UPDATE {observation_keyname} "
                      ."SET keyname='%s', unit='%s', datatype='%s', description='%s' "
                      ."WHERE ido_keyname=%d";
        db_query($update_sql,$keyname,$unit,$datatype,$description,$k_id);
    }
    
    drupal_goto('admin/content/observations/'.$o_id.'/keys');
}

/**
 * Menu callback for admin/content/observations/%/keys/edit/%
 */
function edit_observation_type_key_callback($o_id,$k_id){
    return drupal_get_form("observation_type_key_form",$o_id,$k_id);
}

/**
 * Menu callback for admin/content/observations/%/keys/delete/%
 */
function delete_observation_type_key_callback($o_id,$k_id){
    //get key info
    $sql = "SELECT keyname FROM {observation_keyname} WHERE ido_keyname=%d";
    $name = db_result(db_query($sql,$k_id));
    $text = "<p>You are delete this key: ".$name."</p>";
    $cancel_link = "admin/content/observations/".$o_id."/keys";
    return $text.drupal_get_form("observation_key_delete_form",$k_id,$o_id,$cancel_link);
}

/**
 * delete form
 */
function observation_key_delete_form(&$form_state,$id,$o_id,$cancel_link){
    $form['id'] = array(
        "#type" => "hidden",
        "#value" => $id,
    );
    $form['o_id'] = array(
        "#type" => "hidden",
        "#value" => $o_id,
    );
    $form['submit'] = array(
        "#type" => "submit",
        "#value" => "Delete it",
        '#suffix' => l(" Or cancel?",$cancel_link),
    );
    return $form;
}

/**
 * Handle submit action for delele form
 */
function observation_key_delete_form_submit($form, &$form_state){
    $id = $form_state['values']['id'];
    $o_id = $form_state['values']['o_id'];
    $delete_sql = "DELETE FROM {observation_keyname} WHERE ido_keyname=%d";
    db_query($delete_sql,$id);
    drupal_goto("admin/content/observations/".$o_id."/keys");
}