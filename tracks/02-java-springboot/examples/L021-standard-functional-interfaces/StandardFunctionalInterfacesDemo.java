import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class StandardFunctionalInterfacesDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        Predicate<OrderSummary> paidOrder = order -> order.hasStatus("PAID");
        Predicate<OrderSummary> highValueOrder = order -> order.amount() >= 100.0;

        List<OrderSummary> paidOrders = filterOrders(orders, paidOrder);
        System.out.println("Paid order count: " + paidOrders.size());

        List<OrderSummary> paidHighValueOrders = filterOrders(
                orders,
                paidOrder.and(highValueOrder)
        );
        System.out.println("Paid high value order count: " + paidHighValueOrders.size());

        Function<OrderSummary, OrderResponse> toResponse = order -> new OrderResponse(
                order.id(),
                order.status(),
                "$" + order.amount()
        );

        List<OrderResponse> responses = mapOrders(paidOrders, toResponse);

        Consumer<OrderResponse> printResponse = response -> System.out.println(response.line());
        System.out.println("Response lines:");
        consumeAll(responses, printResponse);

        Function<OrderSummary, String> toDisplayText = order -> order.id() + " => " + order.status();
        System.out.println("Display text:");
        consumeAll(mapOrders(orders, toDisplayText), text -> System.out.println(text));
    }

    static List<OrderSummary> filterOrders(
            List<OrderSummary> orders,
            Predicate<OrderSummary> predicate
    ) {
        List<OrderSummary> result = new ArrayList<>();

        for (OrderSummary order : orders) {
            if (predicate.test(order)) {
                result.add(order);
            }
        }

        return result;
    }

    static <R> List<R> mapOrders(
            List<OrderSummary> orders,
            Function<OrderSummary, R> mapper
    ) {
        List<R> result = new ArrayList<>();

        for (OrderSummary order : orders) {
            result.add(mapper.apply(order));
        }

        return result;
    }

    static <T> void consumeAll(List<T> items, Consumer<T> consumer) {
        for (T item : items) {
            consumer.accept(item);
        }
    }

    static List<OrderSummary> loadOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));
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

    String status() {
        return status;
    }

    boolean hasStatus(String expectedStatus) {
        return status.equals(expectedStatus);
    }

    double amount() {
        return amount;
    }
}

class OrderResponse {
    private final String id;
    private final String status;
    private final String amountText;

    OrderResponse(String id, String status, String amountText) {
        this.id = id;
        this.status = status;
        this.amountText = amountText;
    }

    String line() {
        return id + " | status=" + status + " | amount=" + amountText;
    }
}
