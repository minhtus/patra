package com.prc391.patra.constant;

public class SecurityConstants {
    //jwt claims name
    public static final String JWT_CLAIMS_AUTHORITY = "authority";

    //jwt information
    public static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000;//1 week
    public static final String SECRET = "ThisIsASecret";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final String HEADER_STRING = "Authorization";

    public static final String GOOGLE_CLIENT_ID = "966521159494-re959v2bbvrcuao876ka95l0ghkenn3j.apps.googleusercontent.com";

    public static final String WRITE_ACCESS = "WRITE";
    public static final String READ_ACCESS = "READ";
    public static final String ADMIN_ACCESS = "ADMIN";
}
