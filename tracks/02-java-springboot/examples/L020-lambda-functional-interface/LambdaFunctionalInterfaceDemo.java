import java.util.ArrayList;
import java.util.List;

public class LambdaFunctionalInterfaceDemo {
    public static void main(String[] args) {
        List<OrderSummary> orders = loadOrders();

        List<OrderSummary> paidOrders = filterOrders(
                orders,
                order -> order.hasStatus("PAID")
        );
        System.out.println("Paid orders:");
        printOrders(paidOrders);

        List<OrderSummary> highValueOrders = filterOrders(
                orders,
                order -> order.amount() >= 100.0
        );
        System.out.println("High value orders:");
        printOrders(highValueOrders);

        double vipDiscount = calculateDiscount(
                orders.get(1),
                order -> order.amount() * 0.15
        );
        System.out.println("VIP discount for A1002: " + vipDiscount);

        double fixedDiscount = calculateDiscount(
                orders.get(2),
                order -> 10.0
        );
        System.out.println("Fixed discount for A1003: " + fixedDiscount);
    }

    static List<OrderSummary> filterOrders(List<OrderSummary> orders, OrderFilter filter) {
        List<OrderSummary> result = new ArrayList<>();

        for (OrderSummary order : orders) {
            if (filter.matches(order)) {
                result.add(order);
            }
        }

        return result;
    }

    static double calculateDiscount(OrderSummary order, DiscountRule rule) {
        return rule.discountFor(order);
    }

    static List<OrderSummary> loadOrders() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "PAID", 20.00));
        return orders;
    }

    static void printOrders(List<OrderSummary> orders) {
        for (OrderSummary order : orders) {
            System.out.println(order.summaryLine());
        }
    }
}

@FunctionalInterface
interface OrderFilter {
    boolean matches(OrderSummary order);
}

@FunctionalInterface
interface DiscountRule {
    double discountFor(OrderSummary order);
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

    double amount() {
        return amount;
    }

    String summaryLine() {
        return id + " | status=" + status + " | amount=" + amount;
    }
}
