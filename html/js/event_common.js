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
	
	window.populateEventInfo = function(info, responses) {
		$('#right-header > h1').html(info.name);
		
		if (!info.owner) {
			info.owner = info.home_team;
		}
		
		$("#owner-pic").attr("src", info.owner.picture.medium);
		$("#owner-name").html(info.owner.name);
		
		var start = new Date();
		start.setISO8601(info.start_time, false);
		
		var end = new Date();
		end.setISO8601(info.end_time, false);

		$("#date-time").html(formatDateRange(start, end));

		var place = $("#place a");
		
		if (!isBlank(info.location.formatted_address)) {
			place.html(info.location.formatted_address);
			place.attr("href", "geo:0,0?q=" + info.location.formatted_address);
		} else {
			place.html(info.location.name);
			place.attr("href", "geo:0,0?q=" + info.location.name);
		}
		
		var going = 0;
		var maybe = 0;
		
		for (var i = 0; i<responses.length; i++) {
			
			if (responses[i].status == "yes") {
				going ++;
			} else 
			if (responses[i].status == "maybe"){
				maybe++;
			}
		}
		
		$("#participants").html(
				"Going (" 
				+ going 
				+ "), Maybe (" 
				+ maybe 
				+ ")" );
		
		var participation = Android.getJSParticipation();
		
		changeParticipation("right-header", participation);
		
		Android.onJSFinishedEventInfoLoading();
	};

	window.jsUpdateData = function() {
		console.log("jsUpdateData()");
		
		var data = Android.getJSData();
					
		console.log(data);
		data = JSON.parse(data);
							
		populateEventInfo(data.event, data.responses);
		populateResponses(data.responses)
	};

	window.populateResponses = function (responses) {
		
		window.responses = responses;

		console.log("RESPONSES:");
		console.log(responses);

		
		var participant_list = $("#participant-list");
		
		if (participant_list.length == 0) {
			Android.onJSFinishedResponsesLoading();
			return;
		}
		
		var participant_list_empty = $("#participant-list-empty");
		
		participant_list.html("");
		
		if (!responses || responses.length == 0) {
			participant_list.hide();
			participant_list_empty.show();
		} else {
			participant_list.show();
			participant_list_empty.hide();
			
			console.log("RESPONSES:" + responses);
			
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
		Android.onJSFinishedResponsesLoading();
	}

	var my_rsvp = $("#my-rsvp");
	
	if (my_rsvp.length > 0) {
		new MBP.fastButton(my_rsvp[0], function() {
			Android.onJSChangeMyParticipation();
		});
	}
	
	var place = $("#place > a");
	
	if (place.length> 0) {
		new MBP.fastButton(place[0], function() {
			
			console.log(place.attr("href"));
			
			Android.jsOpenMap(place.attr("href"));
		});	
	}
	
});
