/** require jQuery **/

$(document).ready(function(){
    $('.delete_link').click(function(){
        if(window.confirm('You are deleting this item.')){
            return true;
        }else{
            return false;
        }
    });
});