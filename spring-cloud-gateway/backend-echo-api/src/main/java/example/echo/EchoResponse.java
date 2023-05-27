package example.echo;

import java.util.List;
import java.util.Map;

/**
 * A response from the echo controller.
 * @param headers the headers found in the HTTP request.
 * @param body the request body or {@code null} if not exists.
 */
public record EchoResponse(
        Map<String, List<String>> headers,
        String body) {
}
