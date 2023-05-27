package example.converters.order;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import example.FlatMapper;

@Component("ordersMapper")
public class OrdersFlatMapper implements FlatMapper<JsonNode, OrderItem> {

    @Override
    public Stream<OrderItem> map(JsonNode body) {
        var orders = (ArrayNode) body;
        return stream(orders)
                .flatMap(order -> {
                    var items = (ArrayNode) order.get("items");
                    return stream(items)
                            .map(item -> new OrderItem(
                                    order.get("orderedAt").asText(),
                                    order.get("customer").get("fullName").asText(),
                                    item.get("author").asText(),
                                    item.get("genre").asText(),
                                    item.get("publisher").asText(),
                                    item.get("title").asText()
                                    )
                            );
                });
    }

    private static Stream<JsonNode> stream(ArrayNode arraNode) {
        return StreamSupport.stream(arraNode.spliterator(), false);
    }
}