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
            l($row->node_title, 'node'.$row->nid),
            $row->node_revisions_body,
            l($row->users_name, 'user'.$row->users_uid),
        );
    }
    return theme_table($header, $rows);
}