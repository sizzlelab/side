$(function() {
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
	
	window.formatTime = function(time) {
		
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
	
	window.formatDateRange = function (start, end) {
		
		var result = '';
		
		result += short_month_names[start.getMonth()] + " ";
		result += start.getDate() + ", ";
		result += formatTime(start);
		result += " - ";
		result += formatTime(end);
			
		return result;
	}
	
	window.isBlank = function (str) {
	    return (!str || /^\s*$/.test(str));
	}
});