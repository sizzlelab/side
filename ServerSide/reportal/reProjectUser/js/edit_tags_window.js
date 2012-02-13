$(document).ready(function(){
    $('#edit-tags-div').dialog({
        autoOpen: false,
        modal: true,
        width: 400,
        close: function(){
            location.reload();  
        }
    });
});

/**
 * Link Onclick for Edit tags
 */
function onclick_edit_tags(id){
    $('#edit-tags-div').dialog('open');
    $('#edit-participant-input').val(id);
    get_tags_from_participant(id);
    return false;
}

/**
 * Button onclick for Add new tag
 */
function onclick_add_new_tag(){
    //get tag from textfield
    var tag = $('#new-tag-input').val();
    if(tag==''){
        alert('Please input a new tag on text field.');
        $('#new-tag-input').select();
    }else{
        var id = $('#edit-participant-input').val();
        var url = Drupal.settings.reProjectUser.base_path+'tagsadd/'+id+'/'+tag;
        start_refresh_tags();
        $.getJSON(url, function(data){
            load_tags_in_window(data);
        });
    }
}

/**
 * Div onclick for add exist tag
 */
function onclick_add_exist_tag(tag){
    var id = $('#edit-participant-input').val();
    var url = Drupal.settings.reProjectUser.base_path+'tagsadd/'+id+'/'+tag;
    start_refresh_tags();
    $.getJSON(url, function(data){
        load_tags_in_window(data);
    });
}

/**
 * Div onclick for remove tag
 */
function onclick_remove_tag(tag){
    if(window.confirm('You are removing tag "'+tag+'".')){
        var id = $('#edit-participant-input').val();
        var url = Drupal.settings.reProjectUser.base_path+'tagsremove/'+id+'/'+tag;
        start_refresh_tags();
        $.getJSON(url, function(data){
            load_tags_in_window(data);
        });   
    }
}

/**
 * Start refresh tags
 */
function start_refresh_tags(){
    $('#participants-tags-div').empty();
    $('#participants-tags-loading-div').css('display','block');
    $('#all-tags-div').empty();
    $('#all-tags-loading-div').css('display','block');
}

/**
 * Refresh tags
 */
function load_tags_in_window(data){
    if(data.tags){
        var tags = '';
        for(key in data.tags){
            tags+= "<div class='participant-tag used-tag' onclick='onclick_remove_tag("+'"'+data.tags[key]+'"'+")'>"+data.tags[key]+"</div>";
        }
        tags+= "<div style='clear:both'></div>";
        $('#participants-tags-div').html(tags);
        $('#participants-tags-loading-div').css('display','none');
    }
    if(data.all){
        var all_tags = '';
            for(key in data.all){
            all_tags+= "<div class='participant-tag' onclick='onclick_add_exist_tag("+'"'+data.all[key].tag+'"'+")'>"+data.all[key].tag+"</div>";
        }
        all_tags+= "<div style='clear:both'></div>";
        $('#all-tags-div').html(all_tags);        
        $('#all-tags-loading-div').css('display','none');
    }
}

/**
 * get tags
 */
function get_tags_from_participant(id){
    var url = Drupal.settings.reProjectUser.base_path+'tagsget/'+id;
    start_refresh_tags();
    $.getJSON(url, function(data){
        load_tags_in_window(data);
    });
}