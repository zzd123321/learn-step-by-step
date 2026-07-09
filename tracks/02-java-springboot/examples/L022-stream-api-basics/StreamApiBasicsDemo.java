import java.util.ArrayList;
import java.util.List;

public class StreamApiBasicsDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        List<OrderResponse> paidResponses = orders.stream()
                .filter(order -> order.hasStatus("PAID"))
                .map(order -> new OrderResponse(
                        order.id(),
                        order.status(),
                        "$" + order.amount()
                ))
                .toList();

        System.out.println("Original order count: " + orders.size());
        System.out.println("Paid response count: " + paidResponses.size());
        printResponses(paidResponses);

        List<String> highValueOrderIds = orders.stream()
                .filter(order -> order.amount() >= 100.0)
                .map(order -> order.id())
                .toList();
        System.out.println("High value order ids: " + highValueOrderIds);

        double paidAmountTotal = orders.stream()
                .filter(order -> order.hasStatus("PAID"))
                .mapToDouble(order -> order.amount())
                .sum();
        System.out.println("Paid amount total: " + paidAmountTotal);

        List<OrderResponse> refundedResponses = orders.stream()
                .filter(order -> order.hasStatus("REFUNDED"))
                .map(order -> new OrderResponse(order.id(), order.status(), "$" + order.amount()))
                .toList();
        System.out.println("Refunded response count: " + refundedResponses.size());
    }

    static List<OrderSummary> loadOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));
        orders.add(new OrderSummary("A1004", "CANCELLED", 300.00));
        return orders;
    }

    static void printResponses(List<OrderResponse> responses) {
        for (OrderResponse response : responses) {
            System.out.println(response.line());
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
