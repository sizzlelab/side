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