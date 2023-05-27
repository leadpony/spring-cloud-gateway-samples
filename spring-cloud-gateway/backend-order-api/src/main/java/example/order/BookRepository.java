package example.order;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Repository;

import com.github.javafaker.Faker;

@Repository
public class BookRepository {

    private final Map<String, Book> books;

    public BookRepository() {
        this.books = populate();
    }

    public Book findOne(String id) {
        return books.get(id);
    }

    public Collection<Book> findAll() {
        return books.values();
    }

    Book selectAtRandom(Random random) {
        String id = String.valueOf(random.nextInt(1, books.size() + 1));
        return books.get(id);
    }

    List<Book> selectAtRandom(Random random, int size) {
        return random.ints(size, 1, books.size() + 1)
            .mapToObj(String::valueOf)
            .map(id -> books.get(id))
            .toList();
    }

    private Map<String, Book> populate() {
        var faker = new Faker(new Random());
        return IntStream.range(1, 1000)
            .mapToObj(i -> {
                var book = faker.book();
                return new Book(
                    String.valueOf(i),
                    book.author(),
                    book.genre(),
                    book.publisher(),
                    book.title()
                    );
            })
            .collect(Collectors.toMap(c -> c.id(), Function.identity()));
    }
}
