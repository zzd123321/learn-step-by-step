import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapKeyValueDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "PAID", 99.90));
        orders.add(new OrderSummary("A1002", "CREATED", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));

        Map<String, OrderSummary> ordersById = indexById(orders);

        System.out.println("Order ids: " + ordersById.keySet());
        printOrder(ordersById, "A1002");
        printOrder(ordersById, "A9999");

        Map<String, Integer> statusCounts = countByStatus(orders);
        System.out.println("Status counts: " + statusCounts);

        OrderSummary previous = ordersById.put(
                "A1002",
                new OrderSummary("A1002", "CANCELLED", 149.50)
        );

        System.out.println("Was A1002 replaced: " + (previous != null));
        System.out.println("A1002 after replace: " + ordersById.get("A1002").summaryLine());
    }

    static Map<String, OrderSummary> indexById(List<OrderSummary> orders) {
        Map<String, OrderSummary> result = new LinkedHashMap<>();

        for (OrderSummary order : orders) {
            result.put(order.id(), order);
        }

        return result;
    }

    static Map<String, Integer> countByStatus(List<OrderSummary> orders) {
        Map<String, Integer> counts = new LinkedHashMap<>();

        for (OrderSummary order : orders) {
            int currentCount = counts.getOrDefault(order.status(), 0);
            counts.put(order.status(), currentCount + 1);
        }

        return counts;
    }

    static void printOrder(Map<String, OrderSummary> ordersById, String id) {
        OrderSummary order = ordersById.get(id);

        if (order == null) {
            System.out.println("Order " + id + ": <not found>");
            return;
        }

        System.out.println("Order " + id + ": " + order.summaryLine());
    }
}

class OrderSummary {
    private final String id;
    private final String status;
    private final double amount;

    OrderSummary(String id, String status, double amount) {
        this.id = id;
        this.status = status.trim().toUpperCase(Locale.ROOT);
        this.amount = amount;
    }

    String id() {
        return id;
    }

    String status() {
        return status;
    }

    String summaryLine() {
        return id + " | status=" + status + " | amount=" + amount;
    }
}
