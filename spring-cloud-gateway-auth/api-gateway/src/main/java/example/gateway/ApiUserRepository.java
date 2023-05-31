package example.gateway;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Repository;

@Repository
public class ApiUserRepository {

    private static final String API_KEY = "apikey123";

    private final Map<String, ApiUser> users = new ConcurrentHashMap<>();
    private final Map<String, ApiUser> keyOwners = new ConcurrentHashMap<>();

    public ApiUser findUser(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return users.computeIfAbsent(id, this::addUser);
    }

    public ApiUser findUserByApiKey(String apiKey) {
        return keyOwners.get(apiKey);
    }

    private ApiUser addUser(String id) {
        var user = new ApiUser(id, API_KEY);
        keyOwners.put(user.apiKey(), user);
        return user;
    }
}
