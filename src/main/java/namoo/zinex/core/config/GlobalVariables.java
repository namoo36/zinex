package namoo.zinex.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalVariables {

    // JWT
    public static final String ROLE_CLAIM_KEY = "authorities";
    public static final String NAME_CLAIM_KEY = "username";
    public static final String SESSION_ID_CLAIM_KEY = "sessionId";

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String REFRESH_TOKEN_COOKIE_PATH = "/api/auth";
}
