$(function() {
	console.log("splash!");
	
	
	new MBP.fastButton($("#login")[0], function() {
		console.log("click!");
		Android.onJSFacebookLogin();
	});
		

	Android.onJSFinishedLoading();
	
});
