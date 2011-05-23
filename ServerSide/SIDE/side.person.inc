<?php
// $Id$
/**
 * @file
 * Person page callbacks for SIDE module.
 */

/**
 * Menu callback for person/observations
 */
function person_view_observations_callback(){
    global $user;
    if ($user->uid) {
        $heart_beat_link = l("Heart beat","");
        $list = "<ul><li>$heart_beat_link</li></ul>";
        return $list;
    }
    else {
        drupal_goto('user');
    } 
}

/**
 * Menu callback for person/projects
 */
function person_view_projects_callback(){
    global $user;
    if ($user->uid) {
        //get all projects
        $sql = "SELECT project_code, rp.name name, pc.idproject id FROM {project_code} AS pc "
               ."LEFT JOIN {research_project} AS rp ON rp.idproject=pc.idproject "
               ."WHERE idperson=%d" ;
        $res = db_query($sql,$user->uid);
        
        $header = array(
            "Project Name", "Project Code"  
        );
        
        while($project=db_fetch_object($res)){
            $project_link = "node/".$project->id;
            $rows[] = array(
                array(
                    'data'=>l(
                        $project->name,
                        $project_link,
                        array('attributes' => array('target' => '_blank',))
                    )
                ),  
                array('data'=>t($project->project_code)),
            );
        }
        $content= theme('table', $header, $rows);
        return $content;
    }
    else {
        drupal_goto('user');
    } 
}

/**
 * Menu callback for person/profile
 */
function person_edit_profile_callback(){
    global $user;
    if ($user->uid) {
        
    }
    else {
        drupal_goto('user');
    } 
}

/**
 * Menu callback for person/observations/get/json
 */
function person_observations_get_json_callback(){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
    if ($user->uid) {
        $data['person'] = array(
            'id' => $user->uid,
        );
        $data['observations'] = array();
        //get observation type id e.g. type=1,2,3,
        $id_list = splite_observation_type_list($_GET['type']);
        
        //get date
        $date = $_GET['date'];
        foreach($id_list as $id){
            
            //get observation info
            $get_observation_sql = "SELECT name FROM {observation_type} WHERE ido_type=%d";
            $observation = db_result(db_query($get_observation_sql,$id));
            if($observation){
                //get keynames
                $get_keynames_sql = "SELECT ido_keyname, keyname, datatype,unit FROM {observation_keyname} WHERE ido_type=%d";
                $k_res = db_query($get_keynames_sql,$id);
                if($k_res){                    
                    $k_count = 1;
                    while($keyname=db_fetch_object($k_res)){
                        $select_part.= ", orv$k_count.value ".$keyname->keyname;
                        $left_join_part.= "LEFT JOIN {observation_record_value} AS orv$k_count "
                                          ."ON orv$k_count.ido_record=o.ido_record "
                                          ."AND orv$k_count.ido_keyname=".$keyname->ido_keyname." ";
                        $k_count++;
                    }
                    
                    $get_records_sql = "SELECT time".$select_part." FROM {observation_record} AS o "
                                      .$left_join_part
                                      ."WHERE o.ido_type=%d AND o.idperson=%d "
                                      ."ORDER BY o.time";
                    $r_result = db_query($get_records_sql,$id,$user->uid);
                    $data['s'] = $get_records_sql;
                    while($record=db_fetch_array($r_result)){
                        $records[] = $record;
                    }
                    
                    $data['observations'][] = array(
                        'id' => $id,
                        'name' => $observation,
                        'records' => $records,
                    );
                }
            }
            
            
        }     
        
    }
    else {
        $data = array();
    }
    
    print(json_encode($data));   
    
}