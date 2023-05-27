package example.order;

import java.util.Date;
import java.util.List;

public record Order(
        String id,
        Date orderedAt,
        Customer customer,
        List<Book> items) {
}
