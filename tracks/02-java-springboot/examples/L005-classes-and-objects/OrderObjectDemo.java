public class OrderObjectDemo {
    public static void main(String[] args) {
        Order firstOrder = new Order("A1001", "PAID", 99.90);
        Order secondOrder = new Order("A1002", "CREATED", 149.50);

        System.out.println(firstOrder.summaryLine());
        System.out.println(secondOrder.summaryLine());

        if (firstOrder.canShip()) {
            firstOrder.markShipped();
        }

        Order[] orders = {firstOrder, secondOrder};

        System.out.println("After shipment:");
        printOrders(orders);
        System.out.println("Paid order count: " + countPaidOrders(orders));
    }

    static void printOrders(Order[] orders) {
        for (int i = 0; i < orders.length; i++) {
            System.out.println(orders[i].summaryLine());
        }
    }

    static int countPaidOrders(Order[] orders) {
        int count = 0;

        for (int i = 0; i < orders.length; i++) {
            if (orders[i].isPaid()) {
                count++;
            }
        }

        return count;
    }
}

class Order {
    private final String id;
    private String status;
    private final double amount;

    Order(String id, String status, double amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    boolean isPaid() {
        return "PAID".equals(status);
    }

    boolean canShip() {
        return isPaid();
    }

    void markShipped() {
        if (!canShip()) {
            return;
        }

        status = "SHIPPED";
    }

    String summaryLine() {
        return "Order " + id + " | status=" + status + " | amount=" + amount;
    }
}
