package com.prc391.patra.config.security;

public class SecurityConstants {
    //jwt claims name
    public static final String JWT_CLAIMS_AUTHORITY = "authority";
    public static final String JWT_CLAIMS_CURR_MEMBER_ID = "curr_member_id";

    //jwt information
    //for testing only
    public static final long EXPIRATION_TIME = 600 * 1000;//10 minutes

    public static final String SECRET = "ThisIsASecret";

    public static final String TOKEN_PREFIX = "Bearer";

    public static final String HEADER_STRING = "Authorization";
}
