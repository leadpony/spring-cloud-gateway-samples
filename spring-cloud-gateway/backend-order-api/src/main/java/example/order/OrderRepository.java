package example.order;

import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Repository;

import com.github.javafaker.Faker;

@Repository
public class OrderRepository {

    private final Map<String, Order> orders;


    public OrderRepository(BookRepository books, CustomerRepository customers) {
        this.orders = populate(books, customers);
    }

    public Order findOne(String id) {
        return orders.get(id);
    }

    public Collection<Order> findAll() {
        return orders.values();
    }

    private Map<String, Order> populate(BookRepository books, CustomerRepository customers) {
        var random = new Random();
        var faker = new Faker(random);
        return IntStream.range(1, 50000)
                .mapToObj(i -> {
                    var customer = customers.selectAtRandom(random);
                    var items = books.selectAtRandom(random, random.nextInt(1, 20));
                    return new Order(
                        String.valueOf(i),
                        faker.date().past(100, TimeUnit.DAYS),
                        customer,
                        items
                        );
                })
                .collect(Collectors.toMap(c -> c.id(), Function.identity()));
    }
}
