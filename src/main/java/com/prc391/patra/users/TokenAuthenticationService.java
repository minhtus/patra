package com.prc391.patra.users;

public class TokenAuthenticationService {
    //for testing only
    public static final long EXPIRATION_TIME = 600 * 1000;//10 minutes

    public static final String SECRET = "ThisIsASecret";

    public static final String TOKEN_PREFIX = "Bearer";

    public static final String HEADER_STRING = "Authorization";

//    public static String buildJWTBaseOnUserInfo(String username) {
//
//    }
}
