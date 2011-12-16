<?php
// $Id$
/**
 * @file
 * researcher pages callbacks for SIDE module.
 */

/**
 * Menu callback for researcher/myprojects
 */
function researcher_myprojects_callback(){
    global $user;
    $sql = 'SELECT node.nid AS nid, node.title AS node_title, node_revisions.body AS node_revisions_body, '
               .'node_revisions.format AS node_revisions_format, users.name AS users_name, users.uid AS users_uid '
               .'FROM node node  LEFT JOIN node_revisions node_revisions ON node.vid = node_revisions.vid INNER JOIN users users ON node.uid = users.uid '
               .'WHERE node.type = "researchproject" AND node.uid=%d ORDER BY node.created DESC';
    $rows = array();
    $header = array(
        array('data'=>'Project name'),
        array('data'=>'Description'),
        array('data'=>'Creator'),
    );
    
    $res = db_query($sql, $user->uid);
    while($row = db_fetch_object($res)){
        $rows[] = array(
            l($row->node_title, 'node/'.$row->nid),
            $row->node_revisions_body,
            l($row->users_name, 'user/'.$row->users_uid),
        );
    }
    $content = theme_table($header, $rows);
    return $content;
}

/**
 * Menu callback for researcher/projects/json
 */
function researcher_projects_get_json_callback(){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
    
    //get all project related with this user
    //TODO: make project can be related with serveral user
    //$type = 'researchproject';
    $sql = "SELECT p.project nid, r.name title  FROM {research_projects_persons_list} AS p, {research_projects} AS r WHERE p.user=%d AND role IN ('researcher', 'creator') AND active=1 AND r.id=p.project";
    /*$sql = "SELECT n.nid nid, n.title title, nr.body description FROM {node} AS n "
          ."LEFT JOIN {node_revisions} AS nr ON n.nid=nr.nid AND n.vid=nr.vid "
          ."WHERE type='%s' AND n.uid=%d";*/
          //."WHERE type='%s'";
    $res = db_query($sql,$user->uid);
    //$res = db_query($sql,$type);
    $projects = array();
    while($project=db_fetch_array($res)){
        $projects[] = $project;
    }
    print(json_encode($projects));
}
/**
 * Menu callback for researcher/observations
 */
function researcher_observations_callback(){
    global $user;
    if ($user->uid) {
        $heart_beat_link = l("Heart beat","http://jimu.cs.hut.fi/side/?q=chart");
        $list = "<ul><li>$heart_beat_link</li></ul>";
        return $list;
    }
    else {
        drupal_goto('user');
    } 
}
/**
 * Menu callback for researcher/observations/json
 */
function researcher_observations_get_json_callback(){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
    
    //get all project related with this user
    
    $sql = "SELECT ido_type, name, description FROM {observation_type}";
    $res = db_query($sql,$type);
    $observations = array();
    while($observation=db_fetch_array($res)){
        //get all keynames
        $key_sql = "SELECT ido_keyname id, keyname, unit, datatype, description FROM {observation_keyname} WHERE ido_type=%d";
        $key_res = db_query($key_sql,$observation['ido_type']);
        $keynames = array();
        while($key=db_fetch_array($key_res)){
            $keynames[] = $key;
        }
        $observations[] = array(
            'id' => $observation['ido_type'],
            'name' => $observation['name'],
            'description' => $observation['description'],
            'keynames' => $keynames,
        );
    }
    print(json_encode($observations));
}

/**
 * Menu callback for researcher/projects/persons/json/%
 */
function researcher_projects_get_persons_json_callback($projectId){
    //TODO check access, if current user has the permission to get person info
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    $sql = "SELECT pc.idperson id, u.mail name  FROM {project_code} AS pc "
          ."LEFT JOIN {users} AS u ON u.uid=pc.idperson "
          ."WHERE pc.idproject=%d";
    $res = db_query($sql,$projectId);
    $persons = array();
    while($person=db_fetch_array($res)){
        $persons[] = $person;
    }
    print(json_encode($persons));
}

/**
 * Menu callback for researcher/observations/data/json
 * //NOW only return TEST DATA
 *
 */
function researcher_observations_data_get_json_callback(){    
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
    
    //TODO: check the permisstion for current user
    
    $data['observations'] = array();
    
    //get observation type id
    $id = $_GET['type'];
    
    //get time period
    $start = trim($_GET['start']);
    $end = trim($_GET['end']);
    
    //get project info
    $projectId = $_GET['proid'];
    $personId = $_GET['perid'];
    
    //get average
    $ave = $_GET['ave'];
    
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
                $select_part.= ", AVG(orv$k_count.value) ".$keyname->keyname;
                $o_value_part.= ", observation_record_value AS orv$k_count ";
                $where_part.= " AND orv$k_count.ido_record=o.ido_record "
                             ."AND orv$k_count.ido_keyname=".$keyname->ido_keyname." ";
                $k_count++;
            }
            $get_records_sql = "SELECT YEAR(time) year, MONTH(time) month, DAYOFMONTH(time) day, HOUR(time) hour, MINUTE(time) min  ".$select_part." FROM observation_record AS o "
                                .$o_value_part
                                ."WHERE o.ido_type=%d AND o.idperson=%d AND o.idproject=%d AND o.time>'%s' AND o.time<'%s' "
                                .$where_part
                                ."GROUP BY DATE_FORMAT(o.time, '%Y-%m-%d %H:%i') "
                                ."ORDER BY o.time ";

			//print($get_records_sql);
            $r_result = db_query($get_records_sql,$id,$personId,$projectId,$start,$end);
            
            //TODO: the data type should be transformed automatically 
            if($id==0){
                //for heart beat    
                while($record_array=db_fetch_array($r_result)){
                    $record[] = "Date.UTC({$record_array['year']}, ".($record_array['month']-1).", {$record_array['day']}, {$record_array['hour']}, {$record_array['min']})";
                    $record[] = (int)$record_array['heartrate'];
                    $records[] = $record;
                    unset($record);
                }    
            } if($id==3){
			//$get_records_sql=SELECT time, orv1.value diastolic, orv2.value systolic FROM {observation_record} AS o LEFT JOIN {observation_record_value} AS orv1 ON orv1.ido_record=o.ido_record AND orv1.ido_keyname=31 LEFT JOIN {observation_record_value} AS orv2 ON orv2.ido_record=o.ido_record AND orv2.ido_keyname=30 WHERE o.ido_type=%d AND o.idperson=%d ORDER BY o.time;
			//$r_result = db_query($get_records_sql,$id,$personId,$projectId,$start,$end);
			   //for heart beat    
                while($record_array=db_fetch_array($r_result)){
                    $record2[]=$record[] = "Date.UTC({$record_array['year']}, {$record_array['month']}, {$record_array['day']}, {$record_array['hour']}, {$record_array['min']})";
                    $record[] = (int)$record_array['diastolic'];
					$record2[]=(int)$record_array['systolic'];
                    $records[]['diastolic'] = $record;
					$records[]['systolic'] = $record2;
                    unset($record);
					unset($record2);
                }    
            }else{
                 while($record_array=db_fetch_array($r_result)){
                    $record[] = "Date.UTC({$record_array['year']}, ".($record_array['month']-1).", {$record_array['day']}, {$record_array['hour']}, {$record_array['min']})";
                    $record[] = (int)$record_array['Glucose'];
                    $records[] = $record;
                    unset($record);
                }
            }
                    
            $data['observations'][] = array(
                'id' => $id,
                'name' => $observation,
                'records' => $records,
                //'sql' => $get_records_sql,
            );
        }
    }
    
    print(json_encode($data));
    
}

/**
 * Menu callback for researcher/projects/uploadrecoreds
 */
function researcher_projects_uploadrecoreds_callback(){
    global $user;
    
    $header = array(
	array('data'=>'Upload time'),
	array('data'=>'Project'),
	array('data'=>'User'),
	array('data'=>'Status'),
    );
		
    //get researcher's project
    $get_projects = "SELECT project FROM {research_projects_persons_list} AS p WHERE p.user=%d AND role IN ('researcher', 'creator') AND active=1";
    $res = db_query($get_projects,$user->uid);
    $pid = db_result($res);
    if($pid){
	$where_part = "( f.idproject=$pid ";
	while($pid = db_result($res)){
	    $where_part.= " OR f.idproject=$pid ";
	}
        $where_part.= ' ) ';
	$sql = 'SELECT upload_time, file_name, one_time_upload_ID, idperson, p.idproject, p.name as project_name, u.mail as mail, f.status, f.changed '
		           .'FROM {file_upload_records} AS f LEFT JOIN {users} AS u ON uid=idperson LEFT JOIN {research_project} AS p ON p.idproject=f.idproject '
			   .'WHERE '.$where_part.' ORDER BY upload_time DESC';
	$rows = array();
	$res = pager_query($sql, 30);
        while($r = db_fetch_object($res)){
            $rows[] = array(
		$r->upload_time,
		l($r->project_name, 'node/'.$r->idproject),
		l($r->mail, 'user/'.$r->idperson),
		$r->status,
	    );
	}
    }
    if(empty($rows)){
	$content.= '<p>(No uploading records.)</p>';
    }else{
	$content.= theme_table($header, $rows);
        $content.= theme('pager');
    }
    return $content;
}