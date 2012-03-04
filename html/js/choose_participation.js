$(function() {
	$("header img").attr("src", Android.getUserPicture());
	$("header > h1").html(Android.getUserName());
	
	$("#participation-option-page li").each(function() {
		var choice = this.id;
		new MBP.fastButton(this, function() {
			console.log("#participation-option-page > li pressed");
			Android.onParticipanceChoice(choice);
		});
	});	

	Android.onFinishedLoading();
});
