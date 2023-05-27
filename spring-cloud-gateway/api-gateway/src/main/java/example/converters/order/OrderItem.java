package example.converters.order;

public record OrderItem(
        String orderedAt,
        String customerName,
        String author,
        String genre,
        String publisher,
        String title
        ) {
}
