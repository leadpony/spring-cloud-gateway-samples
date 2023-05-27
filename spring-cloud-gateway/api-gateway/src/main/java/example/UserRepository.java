package example;

import java.net.InetSocketAddress;

import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private static final String API_KEY = "AIzaSyDaGmWKa4JsXZ-HjGw7ISLn_3namBGewQe";

    public User findUserAt(InetSocketAddress address) {
        String id = "user@" + address.getAddress().toString();
        return new User(id, API_KEY);
    }
}
