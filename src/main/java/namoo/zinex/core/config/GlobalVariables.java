package namoo.zinex.core.config;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class GlobalVariables {

    // JWT
    public static final String ROLE_CLAIM_KEY = "authorities";
    public static final String NAME_CLAIM_KEY = "username";

}
