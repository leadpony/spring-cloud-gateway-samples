package example.echo;

import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @RequestMapping("/*")
    public EchoResponse echo(
            @RequestHeader MultiValueMap<String, String> headers,
            @RequestBody(required = false) String body) {
        return new EchoResponse(headers, body);
    }
}
