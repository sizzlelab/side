/* This file require jquery */
$(document).ready(function(){
   $('body').append('<div id="person-info-card-dialog" style="display:none; position: absolute;"></div>');
   $('.show_person_card').hover(
        //mouse enter
        function(e){
            $('#person-info-card-dialog').addClass('person-info-loading-img');
            $('#person-info-card-dialog').text('');
            $('#person-info-card-dialog')
                .css({
                    'top': (e.pageY-100)+'px',
                    'left': (e.pageX+14)+'px'
                    })
                .show('fast');
            url = $(this).attr('href')+'/ajax/personal_info';
            $.getJSON(url, function(data){
                $('#person-info-card-dialog').removeClass('person-info-loading-img');
                content = '<img src="'+data.picture+'" id="head-photo-image">'
                         +'<dl>'
                         +'<dt>Name</dt><dd>'+data.name+'</dd>'
                         +'<dt>Email</dt><dd>'+data.mail+'</dd>'
                         +'<dt>Phone</dt><dd>'+data.phone+'</dd>'
                         +'</dl>';
                         //+'<p style="text-align:center"><a href="'+data.profile_url+'">More about this person</a></p>';

                $('#person-info-card-dialog').html(content);
            });
        },
        //mouse move out
        function(){
            $('#person-info-card-dialog').css('display','none');
            $('#person-info-card-dialog').text('');
        }
    ); 
});


