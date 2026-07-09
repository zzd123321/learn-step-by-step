import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StreamMatchValidationDemo {
    public static void main(String[] args) {
        List<OrderSummary> validOrders = loadValidOrders();
        List<OrderSummary> invalidOrders = loadInvalidOrders();

        System.out.println("Valid orders:");
        printValidationResult(validOrders);

        System.out.println("Invalid orders:");
        printValidationResult(invalidOrders);
    }

    static void printValidationResult(List<OrderSummary> orders) {
        boolean hasCancelledOrder = orders.stream()
                .anyMatch(order -> order.hasStatus("CANCELLED"));

        boolean allAmountsValid = orders.stream()
                .allMatch(order -> order.amount() > 0.0);

        boolean noPendingOrder = orders.stream()
                .noneMatch(order -> order.hasStatus("PENDING"));

        boolean noDuplicateIds = hasNoDuplicateIds(orders);

        System.out.println("Has cancelled order: " + hasCancelledOrder);
        System.out.println("All amounts valid: " + allAmountsValid);
        System.out.println("No pending order: " + noPendingOrder);
        System.out.println("No duplicate ids: " + noDuplicateIds);
        System.out.println("Can submit: " + (!hasCancelledOrder && allAmountsValid && noPendingOrder && noDuplicateIds));
    }

    static boolean hasNoDuplicateIds(List<OrderSummary> orders) {
        Set<String> seenIds = new HashSet<>();

        return orders.stream()
                .allMatch(order -> seenIds.add(order.id()));
    }

    static List<OrderSummary> loadValidOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "SHIPPED", 20.00));
        return orders;
    }

    static List<OrderSummary> loadInvalidOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A2001", "CREATED", 99.90));
        orders.add(new OrderSummary("A2002", "CANCELLED", 149.50));
        orders.add(new OrderSummary("A2002", "PENDING", 0.00));
        return orders;
    }
}

class OrderSummary {
    private final String id;
    private final String status;
    private final double amount;

    OrderSummary(String id, String status, double amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    String id() {
        return id;
    }

    boolean hasStatus(String expectedStatus) {
        return status.equals(expectedStatus);
    }

    double amount() {
        return amount;
    }
}
