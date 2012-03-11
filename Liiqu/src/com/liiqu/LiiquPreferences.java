package com.liiqu;

public class LiiquPreferences {
	public static final String PRODUCTION_ENVIRONMENT = "https://liiqu.com/";

	public static final String TESTING_ENVIRONMENT = "https://staging.liiqu.com/";

	public static final String COMMON_FILE = "preferences";
	
	public static final String CSRF = "csrf";
	public static final String SESSION_ID = "session id";
	public static final String COOKIE = "cookie";

	public static final String USER_ID = "user id";
	
	
	public static final String ROOT_URL  = LiiquApp.DEBUG 
			? TESTING_ENVIRONMENT 
			: PRODUCTION_ENVIRONMENT ;

	public static final String FACEBOOK_APP_ID = "258571624190728";

}
