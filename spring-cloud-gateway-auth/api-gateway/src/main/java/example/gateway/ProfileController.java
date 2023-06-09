package example.gateway;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final ApiUserRepository userRepos;

    public ProfileController(ApiUserRepository userRepos) {
        this.userRepos = userRepos;
    }

    @GetMapping("/profile")
    public String profile(
            Model model,
            @RegisteredOAuth2AuthorizedClient("keycloak") OAuth2AuthorizedClient authorizedClient) {

        ApiUser user = userRepos.findUser(authorizedClient.getPrincipalName());

        model.addAttribute("user", user);

        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        model.addAttribute("accessToken", accessToken);
        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();
        model.addAttribute("refreshToken", refreshToken);

        return "profile";
    }
}
