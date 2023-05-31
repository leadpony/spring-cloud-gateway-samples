package example.gateway;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;

public class ApiUserSessionRegistry {

    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public String getSessionId(String userId) {
        return sessions.get(userId);
    }

    public void registerSession(String sessionId, String userId) {
        sessions.put(userId, sessionId);
    }

    public void unregisterSession(String sessionId) {
        for (var entry : sessions.entrySet()) {
            if (entry.getValue().equals(sessionId)) {
                sessions.remove(entry.getKey());
            }
        }
    }
}
