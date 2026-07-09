import java.util.ArrayList;
import java.util.List;

public class ListCollectionDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = new ArrayList<>();

        orders.add(new OrderSummary("A1001", "PAID", 99.90));
        orders.add(new OrderSummary("A1002", "CREATED", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));

        System.out.println("Total orders: " + orders.size());
        System.out.println("First order: " + orders.get(0).summaryLine());

        System.out.println("All orders:");
        printOrders(orders);

        List<OrderSummary> paidOrders = filterByStatus(orders, "PAID");
        System.out.println("Paid orders:");
        printOrders(paidOrders);

        List<OrderSummary> emptyResult = filterByStatus(orders, "CANCELLED");
        System.out.println("Cancelled order count: " + emptyResult.size());
    }

    static void printOrders(List<OrderSummary> orders) {
        if (orders.isEmpty()) {
            System.out.println("<empty>");
            return;
        }

        for (OrderSummary order : orders) {
            System.out.println(order.summaryLine());
        }
    }

    static List<OrderSummary> filterByStatus(List<OrderSummary> orders, String status) {
        List<OrderSummary> result = new ArrayList<>();

        for (OrderSummary order : orders) {
            if (order.hasStatus(status)) {
                result.add(order);
            }
        }

        return result;
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

    boolean hasStatus(String expectedStatus) {
        return status.equals(expectedStatus);
    }

    String summaryLine() {
        return id + " | status=" + status + " | amount=" + amount;
    }
}
