$(function() {
	console.log("splash!");
	
	
	new MBP.fastButton($("#login")[0], function() {
		console.log("click!");
		Android.onJSFacebookLogin();
	});
		
	
	window.jsShowProgress = function() {
		$("#progress").show();
		$("#login").hide();
	};
	
	window.jsHideProgress = function() {
		$("#progress").hide();
		$("#login").show();
	};
	
	Android.onJSFinishedLoading();
	
});
