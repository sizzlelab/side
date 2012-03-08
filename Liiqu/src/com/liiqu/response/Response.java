package com.liiqu.response;

public class Response {

	long liiquEventId;
	
	long liiquUserId;

	String json;

	public long getLiiquEventId() {
		return liiquEventId;
	}

	public void setLiiquEventId(long liiquEventId) {
		this.liiquEventId = liiquEventId;
	}

	public long getLiiquUserId() {
		return liiquUserId;
	}

	public void setLiiquUserId(long liiquUserId) {
		this.liiquUserId = liiquUserId;
	}

	public String getJson() {
		return json;
	}

	public void setJson(String json) {
		this.json = json;
	}
	
	public void setLiiquIds(String liiquId) {
		final int dividerIndex = liiquId.indexOf('_');
		liiquEventId = Long.parseLong(liiquId.substring(0, dividerIndex));
		liiquUserId = Long.parseLong(liiquId.substring(dividerIndex + 1));
	}
}
