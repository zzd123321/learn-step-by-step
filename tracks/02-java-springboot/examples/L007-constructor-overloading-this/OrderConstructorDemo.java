public class OrderConstructorDemo {
    public static void main(String[] args) {
        Order draftOrder = new Order("A1004", 88.00);
        Order importedOrder = new Order("A1005", "PAID", 199.00);
        Order cancelledOrder = new Order("A1006", "CANCELLED", 20.00, "duplicate order");

        System.out.println(draftOrder.summaryLine());
        System.out.println(importedOrder.summaryLine());
        System.out.println(cancelledOrder.summaryLine());
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;
    private String cancelReason;

    Order(String id, double amount) {
        this(id, "CREATED", amount, "");
    }

    Order(String id, String status, double amount) {
        this(id, status, amount, "");
    }

    Order(String id, String status, double amount, String cancelReason) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order id must not be blank");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Order amount must be greater than 0");
        }

        this.id = id;
        this.status = normalizeStatus(status);
        this.amount = amount;
        this.cancelReason = normalizeCancelReason(this.status, cancelReason);
    }

    String summaryLine() {
        String base = "Order " + id + " | status=" + status + " | amount=" + amount;

        if ("CANCELLED".equals(status)) {
            return base + " | cancelReason=" + cancelReason;
        }

        return base;
    }

    private String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "CREATED";
        }

        return rawStatus.trim().toUpperCase();
    }

    private String normalizeCancelReason(String status, String rawReason) {
        if (!"CANCELLED".equals(status)) {
            return "";
        }

        if (rawReason == null || rawReason.isBlank()) {
            return "no reason provided";
        }

        return rawReason.trim();
    }
}
