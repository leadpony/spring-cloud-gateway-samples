package example.order;

public record Customer(
        String id,
        String fullName,
        String firstName,
        String lastName,
        int age,
        String zipCode,
        String address,
        String phoneNumber) {
}
