import java.util.ArrayList;
import java.util.List;

public class StreamSortDistinctLimitDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        List<OrderSummary> topTwoByAmount = orders.stream()
                .sorted((left, right) -> Double.compare(right.amount(), left.amount()))
                .limit(2)
                .toList();

        System.out.println("Top 2 orders by amount:");
        printOrders(topTwoByAmount);

        List<String> distinctStatuses = orders.stream()
                .map(order -> order.status())
                .distinct()
                .toList();
        System.out.println("Distinct statuses: " + distinctStatuses);

        List<String> firstTwoPaidOrderIds = orders.stream()
                .filter(order -> order.hasStatus("PAID"))
                .map(order -> order.id())
                .limit(2)
                .toList();
        System.out.println("First 2 paid order ids: " + firstTwoPaidOrderIds);

        System.out.println("Original order list:");
        printOrders(orders);
    }

    static List<OrderSummary> loadOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));
        orders.add(new OrderSummary("A1004", "CANCELLED", 300.00));
        orders.add(new OrderSummary("A1005", "PAID", 88.00));
        return orders;
    }

    static void printOrders(List<OrderSummary> orders) {
        for (OrderSummary order : orders) {
            System.out.println(order.summaryLine());
        }
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

    String summaryLine() {
        return id + " | status=" + status + " | amount=" + amount;
    }
}
