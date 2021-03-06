<?php
// $Id$
/**
 * @file
 * Person pages callbacks for SIDE module.
 */

/**
 * Menu callback for person/observations
 */
function person_view_observations_callback(){
    global $user;
    if ($user->uid) {
        $heart_beat_link = l("Heart 
beat","http://jimu.cs.hut.fi/side/?q=person_chart");
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
		$start_time=$_GET['start'];
		$end_time=$_GET['end'];
		$proid=$_GET['proid'];
        $date = $_GET['date'];
		if($_GET['perid']){
			$perid=$_GET['perid'];
		}else{
			$perid=$user->uid;
		}
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
                        $inner_join_part.= ", {observation_record_value} AS orv$k_count ";
                        $where_part.= " AND orv$k_count.ido_record=o.ido_record "
                                          ."AND orv$k_count.ido_keyname=".$keyname->ido_keyname." ";
                        $k_count++;
                    }
                    
                    $get_records_sql = "SELECT time".$select_part." FROM {observation_record} AS o "
                                      .$inner_join_part
                                      ."WHERE o.ido_type=%d AND o.idperson=%d  and o.idproject =%d and o.time >='%s' and o.time <='%s' "
                                      .$where_part
                                      ."ORDER BY o.time";// and time >= "2011-01-01" and time <= '2011-12-31';

                    $r_result = db_query($get_records_sql,$id,$perid,$proid,$start_time,$end_time);
                    //$data['s'] = $get_records_sql;
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

/**
 * Menu callback for person/projects/get/json
 */
function ajax_person_projects_get_json(){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
    $projects = array();
    if ($user->uid) {
        $sql = "SELECT project_code, rp.name name, pc.idproject id FROM {project_code} AS pc, {research_project} AS rp "
              ."WHERE pc.idperson=%d AND rp.idproject=pc.idproject " ;
        $res = db_query($sql,$user->uid);
        while($project=db_fetch_array($res)){
            $projects[] = $project;
        }
    }
    print(json_encode($projects));   
}

/**
 * Menu callback for person/projects/uploadrecoreds
 */
function person_projects_uploadrecoreds_callback(){
    global $user;
    
    $breadcrumb = drupal_get_breadcrumb();
    $breadcrumb[] = l(t('Uploaded observations'), 'person_chart');
    drupal_set_breadcrumb($breadcrumb);
    
    $header = array(
	array('data'=>'Uploaded at'),
	array('data'=>'Project'),
	array('data'=>'Status'),
    );
    $sql = 'SELECT upload_time, file_name, one_time_upload_ID, idperson, username, p.idproject, p.name as project_name, f.status, f.changed FROM {file_upload_records} AS f LEFT JOIN {person} AS u ON uid=idperson LEFT JOIN {research_project} AS p ON p.idproject=f.idproject WHERE idperson='.$user->uid.' ORDER BY upload_time DESC';
    $rows = array();
    $res = pager_query($sql, 30);
    while($r = db_fetch_object($res)){
	$rows[] = array(
	    $r->upload_time,
	    l($r->project_name, 'node/'.$r->idproject),
	    $r->status,
	);
    }
    if(empty($rows)){
	$content.= '<p>No observations uploaded.</p>';
    }else{
	$content.= theme_table($header, $rows);
        $content.= theme('pager');
    }
    return $content;
}
