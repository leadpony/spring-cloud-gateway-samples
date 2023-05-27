package example.order;

import java.util.Collection;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;

    public ApiController(
            BookRepository bookRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository) {
        this.bookRepository = bookRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/books")
    public Collection<Book> books() {
        return bookRepository.findAll();
    }

    @GetMapping("/books/{id}")
    public Book books(@PathVariable("id") String id) {
        return bookRepository.findOne(id);
    }

    @GetMapping("/customers")
    public Collection<Customer> customers() {
        return customerRepository.findAll();
    }

    @GetMapping("/customers/{id}")
    public Customer customers(@PathVariable("id") String id) {
        return customerRepository.findOne(id);
    }

    @GetMapping("/orders")
    public Collection<Order> orders() {
        return orderRepository.findAll();
    }

    @GetMapping("/orders/{id}")
    public Order orders(@PathVariable("id") String id) {
        return orderRepository.findOne(id);
    }
}
