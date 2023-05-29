package example.gateway;

import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * A wrapper class of {@link OidcReactiveOAuth2UserService}.
 */
@Service
public class GatewayOidcUserService extends OidcReactiveOAuth2UserService {

    private final ApiUserRepository userRepos;

    public GatewayOidcUserService(ApiUserRepository userRepos) {
        this.userRepos = userRepos;
    }

    @Override
    public Mono<OidcUser> loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        var oidcUser = super.loadUser(userRequest);
        return oidcUser.map(u -> {
            userRepos.findUser(u.getSubject());
            return u;
        });
    }
}
