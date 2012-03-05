$(function() {
	window.changeParticipation = function (uid, participation) {
		console.log("changeParticipation(" + uid + ", " + participation + ")");

		var rsvpElement = $("#" + uid + " button");

		var rsvpTitle = participation != "yes"
			? (participation == "maybe" ? "Maybe Attending" : "Not Attending") 
			: "Attending";		
			
		rsvpElement.removeClass("rsvp-yes rsvp-maybe rsvp-no rsvp-not-replied");
		
		rsvpElement.addClass("rsvp-" + participation);
		
		if (uid != "right-header") {
			return;
		}
		
		rsvpElement.find("span.btn-text").html(rsvpTitle);
	};
	
	window.changePicture = function (id, path) {
		
		console.log("changePicture(" + id + ", " + path + ")")
		$("#" + id).attr("src", path);
	};

	window.jsUpdateData = function() {
		console.log("jsUpdateData()");
		
		var data = Android.getJSData();
					
		data = JSON.parse(data);
			
		window.responses = data.responses;
		data = data.event;
		
		if (!data) {
			$("#participant-list").hide();
			$("#participant-list-empty").show();
			
			Android.onJSFinishedLoading();
			return;
		}

		$('#right-header > h1').html(data.name);
		
		if (!data.owner) {
			data.owner = data.home_team;
		}
		
		$("#owner-pic").attr("src", data.owner.picture.medium);
		
		var start = new Date();
		start.setISO8601(data.start_time, false);
		
		var end = new Date();
		end.setISO8601(data.end_time, false);

		$("#date-time").html(formatDateRange(start, end));

		if (!isBlank(data.location.formatted_address)) {
			$("#place a").html(data.location.formatted_address);		
		} else {
			$("#place a").html(data.location.name);
		}
		
		
		var participation = Android.getJSParticipation();
		
		changeParticipation("right-header", participation);
		
		window.mapUri = getGeoURI(data);
		
		list.html("");
		
		if (!responses || responses.length == 0) {
			$("#participant-list").hide();
			$("#participant-list-empty").show();
		} else {
			$("#participant-list").show();
			$("#participant-list-empty").hide();			
			
			$.each(responses, populateResponse);
		}
	};

	var list = $("#participant-list");
	var template = $("#participant-list li:first-child");
	
	window.populateResponse = function (index, response) {
		var element = template.clone();
		
		var uid = "user-" + response.user.id;
		
		element.attr("id", uid);
		
		var upic = response.user.picture.medium;
		
		var picture = element.find("img");
		picture.attr("id", "pic-" + uid);
		
		console.log("pic-" + uid);
		
		if (upic != undefined) {
			picture.attr("src", upic);
		}
		
		var name = response.user.name;
		element.find(".name").html(name);
		
		var statusClass = !response.status || response.status == null 
			? "rsvp-not-replied"
			: "rsvp-" + response.status;
		
		var statusButton = element.find("button.status");
		
		statusButton.addClass(statusClass);
		
		new MBP.fastButton(statusButton[0], function() {
			Android.onJSChangeParticipation(uid, name, upic);
		});
		
		element.show();
		list.append(element);

		
		if ((window.responses.length -1) != index) {
			return;
		}
		// on last element
		Android.onJSFinishedLoading();
	}
	
	Date.prototype.setISO8601 = function (string, useTimeZoneOffset) {
	    var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
	        "(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
	        "(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";

	    var d = string.match(new RegExp(regexp));

	    var offset = 0;
	    var date = new Date(d[1], 0, 1);

	    if (d[3]) { date.setMonth(d[3] - 1); }
	    if (d[5]) { date.setDate(d[5]); }
	    if (d[7]) { date.setHours(d[7]); }
	    if (d[8]) { date.setMinutes(d[8]); }
	    if (d[10]) { date.setSeconds(d[10]); }
	    if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
	    if (d[14]) {
	        offset = (Number(d[16]) * 60) + Number(d[17]);
	        offset *= ((d[15] == '-') ? 1 : -1);
	    }

	    if (useTimeZoneOffset) {
	    	offset -= date.getTimezoneOffset();
	    }
	     
	    time = (Number(date) + (offset * 60 * 1000));
	    this.setTime(Number(time));
	}
		
	var short_month_names = new Array(
			"Jan",
			"Feb",
			"Mar",
			"Apr",
			"May",
			"Jun",
			"Jul",
			"Aug",
			"Sep",
			"Oct",
			"Nov",
			"Dec");
	
	function formatTime(time) {
		
		var result = "";
		
		if (time.getHours() > 12) {
			result += (time.getHours() - 12) + ":";
		} else {
			result += time.getHours() + ":";
		}
		
		if (time.getMinutes() < 10) {
			result += "0";
		}
		
		result += time.getMinutes();
		
		if (time.getHours() >= 12) {
			result += "pm";
		} else {
			result += "am";
		}
		
		return result;
	}
	
	function formatDateRange(start, end) {
		
		var result = '';
		
		result += short_month_names[start.getMonth()] + " ";
		result += start.getDate() + ", ";
		result += formatTime(start);
		result += " - ";
		result += formatTime(end);
			
		return result;
	}
	
	function isBlank(str) {
	    return (!str || /^\s*$/.test(str));
	}
		
	function getGeoURI(data) {
		if (!isBlank(data.location.formatted_address)) {
			return "geo:0,0?q=" + data.location.formatted_address;
		} else {
			return "geo:0,0?q=" + data.location.name;
		}		
	}

	
	new MBP.fastButton($("#my-rsvp")[0], function() {
		Android.onJSChangeMyParticipation();
	});
		
	new MBP.fastButton($("#place > a")[0], function() {
		
		console.log(window.mapUri);
		
		Android.jsOpenMap(window.mapUri);
	});	
	
});
