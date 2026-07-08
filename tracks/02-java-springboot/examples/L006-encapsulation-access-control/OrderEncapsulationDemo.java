public class OrderEncapsulationDemo {
    public static void main(String[] args) {
        Order order = new Order("A1003", 199.00);

        System.out.println(order.summaryLine());
        System.out.println("Can ship before payment: " + order.canShip());

        order.ship();
        System.out.println("After trying to ship before payment: " + order.summaryLine());

        order.pay();
        order.ship();
        System.out.println("After payment and shipment: " + order.summaryLine());

        order.cancel("customer changed address");
        System.out.println("After trying to cancel shipped order: " + order.summaryLine());
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;
    private String cancelReason;

    Order(String id, double amount) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than 0");
        }

        this.id = id;
        this.amount = amount;
        this.status = "CREATED";
        this.cancelReason = "";
    }

    String getId() {
        return id;
    }

    String getStatus() {
        return status;
    }

    double getAmount() {
        return amount;
    }

    String getCancelReason() {
        return cancelReason;
    }

    boolean canShip() {
        return "PAID".equals(status);
    }

    void pay() {
        if (!"CREATED".equals(status)) {
            return;
        }

        status = "PAID";
    }

    void ship() {
        if (!canShip()) {
            return;
        }

        status = "SHIPPED";
    }

    void cancel(String reason) {
        if ("SHIPPED".equals(status)) {
            return;
        }

        status = "CANCELLED";
        cancelReason = reason == null || reason.isBlank() ? "no reason provided" : reason;
    }

    String summaryLine() {
        String base = "Order " + id + " | status=" + status + " | amount=" + amount;

        if ("CANCELLED".equals(status)) {
            return base + " | cancelReason=" + cancelReason;
        }

        return base;
    }
}
