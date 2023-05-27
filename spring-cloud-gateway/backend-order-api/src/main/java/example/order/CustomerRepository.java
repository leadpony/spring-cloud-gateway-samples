package example.order;

import com.github.javafaker.Faker;
import java.util.Collection;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Repository;

@Repository
public class CustomerRepository {

    private final Map<String, Customer> customers;

    public CustomerRepository() {
        this.customers = populate();
    }

    public Customer findOne(String id) {
        return customers.get(id);
    }

    public Collection<Customer> findAll() {
        return customers.values();
    }

    Customer selectAtRandom(Random random) {
        String id = String.valueOf(random.nextInt(1, customers.size() + 1));
        return customers.get(id);
    }

    private Map<String, Customer> populate() {
        var faker = new Faker(new Random());
        return IntStream.range(1, 1000)
            .mapToObj(i -> {
                var name = faker.name();
                var address = faker.address();
                return new Customer(
                    String.valueOf(i),
                    name.fullName(),
                    name.firstName(),
                    name.lastName(),
                    faker.number().numberBetween(16, 100),
                    address.zipCode(),
                    address.fullAddress(),
                    faker.phoneNumber().phoneNumber()
                    );
            })
            .collect(Collectors.toMap(c -> c.id(), Function.identity()));
    }
}
