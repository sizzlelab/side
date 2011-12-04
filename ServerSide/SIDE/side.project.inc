<?php

/**
 * Menu callback for projects
 */
function side_projects_list_display(){
    $sql = 'SELECT node.nid AS nid, node.title AS node_title, node_revisions.body AS node_revisions_body, '
               .'node_revisions.format AS node_revisions_format, users.name AS users_name, users.uid AS users_uid '
               .'FROM node node  LEFT JOIN node_revisions node_revisions ON node.vid = node_revisions.vid INNER JOIN users users ON node.uid = users.uid '
               .'WHERE node.type = "researchproject" ORDER BY node.created DESC';
    $rows = array();
    $header = array(
        array('data'=>'Project name'),
        array('data'=>'Description'),
        array('data'=>'Creator'),
    );
    
    $res = pager_query($sql, 10);
    while($row = db_fetch_object($res)){
        $rows[] = array(
            l($row->node_title, 'node/'.$row->nid),
            $row->node_revisions_body,
            l($row->users_name, 'user/'.$row->users_uid),
        );
    }
    $content = theme_table($header, $rows);
    $content.= theme('pager', NULL, 10, 0);
    return $content;
}

	/**
 * Menu callback for project/date/get/json
 */
function ajax_project_date_get_json(){
    drupal_set_header('Content-Type: text/plain; charset: utf-8');
    global $user;
	if($_GET['perid']){
			$perid=$_GET['perid'];
		}else{
			$perid=$user->uid;
		}
		$proid=$_GET['proid'];
    $projects = array();
    $month_start="2011-11-00";
	$month_end="2011-12-00";
        $sql = "SELECT distinct DAYOFMONTH(time)FROM {observation_record} AS o, {,observation_record_value} AS ov "
              ."WHERE o.ido_record=ov.ido_record AND o.idproject=%d AND o.idperson=%d AND o.time between '%s' and '%s' and ov.value !=0" ;
        echo $sql;
		$res = db_query($sql,$proid,$perid,$month_start,$month_end);
		
        while($project=db_fetch_array($res)){
            $projects[] = $project;
        }
    print(json_encode($projects));   
}