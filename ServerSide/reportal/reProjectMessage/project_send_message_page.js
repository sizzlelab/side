var message_to_list;
$(document).ready(function(){
    $('#select-all-checkbox').click(function(){
        if(this.checked){
            $('.person-input-checkbox').attr('checked','checked');
            $('.person-input-checkbox').parent().css('background-color','#67E46F');
            renew_to_list();
        }else{
            $('.person-input-checkbox').removeAttr('checked');
            $('.person-input-checkbox').parent().css('background-color','#FFF');
            renew_to_list();
        }
    });
    $('.person-input-checkbox').each(function(){
        $(this).bind('click',function(){
            if(this.checked){
                $('#'+this.id+'-td').css('background-color','#67E46F');
                renew_to_list();
            }else{
                $('#'+this.id+'-td').css('background-color','#FFF');
                $('#select-all-checkbox').removeAttr('checked');
                renew_to_list();
            }
        });
    });
    $('#edit-templates').change(function(){
        var tid = $(this).val();
        if(tid>0){
            var url = Drupal.settings.projectmanage.base_path+'message/templates/get/'+tid;
            $.getJSON(url,function(data){
                if(data.title && data.content){
                    $('#edit-title').val(data.title);
                    try{
                        Drupal.ckeditorToggle('edit-body','Switch to plain text editor','Switch to rich text editor',1);
                    }catch(err){}
                    $('#edit-body').val(data.content);
                    try{
                        Drupal.ckeditorToggle('edit-body','Switch to plain text editor','Switch to rich text editor',1);
                    }catch(err){}
                }
            }); 
        }
        
    });
});

function renew_to_list(){
    message_to_list = '';
    $('.person-input-checkbox').each(function(){
        if(this.checked){
            message_to_list+= this.value + ';';
        }
        $('#edit-to-list').val(message_to_list);
    });
}