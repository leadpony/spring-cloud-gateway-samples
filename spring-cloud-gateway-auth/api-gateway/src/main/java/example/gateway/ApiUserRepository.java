package example.gateway;

import java.util.Map;
import java.util.HashMap;

import org.springframework.stereotype.Repository;

@Repository
public class ApiUserRepository {

    private static final String API_KEY = "apikey123";

    private final Map<String, ApiUser> users = new HashMap<>();

    public ApiUser findUser(String id) {
        if (!users.containsKey(id)) {
            users.put(id,  new ApiUser(id, API_KEY));
        }
        return users.get(id);
    }

    public ApiUser findUserByKey(String key) {
        if (API_KEY.equals(key)) {
            var it = users.values().iterator();
            if (it.hasNext()) {
                return it.next();
            }
        }
        return null;
    }
}
