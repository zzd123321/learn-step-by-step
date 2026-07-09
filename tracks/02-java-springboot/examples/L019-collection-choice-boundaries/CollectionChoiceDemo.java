import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class CollectionChoiceDemo {
    public static void main(String[] args) {
        List<String> requestedOrderIds = new ArrayList<>();
        requestedOrderIds.add("A1003");
        requestedOrderIds.add("A1001");
        requestedOrderIds.add("A1003");
        requestedOrderIds.add("A9999");
        requestedOrderIds.add("A1002");

        System.out.println("Raw request ids: " + requestedOrderIds);

        Set<String> uniqueOrderIds = uniqueIds(requestedOrderIds);
        System.out.println("Unique request ids: " + uniqueOrderIds);

        List<OrderSummary> repositoryOrders = loadOrdersFromRepository();
        Map<String, OrderSummary> ordersById = indexById(repositoryOrders);

        List<OrderSummary> responseOrders = buildResponseInRequestOrder(uniqueOrderIds, ordersById);
        System.out.println("Response order list:");
        printOrders(responseOrders);

        Queue<AuditTask> auditTasks = createAuditTasks(responseOrders);
        System.out.println("Audit queue size: " + auditTasks.size());
        processAuditTasks(auditTasks);
    }

    static Set<String> uniqueIds(List<String> ids) {
        return new LinkedHashSet<>(ids);
    }

    static List<OrderSummary> loadOrdersFromRepository() {
        List<OrderSummary> orders = new ArrayList<>();
        orders.add(new OrderSummary("A1001", "CREATED", 99.90));
        orders.add(new OrderSummary("A1002", "PAID", 149.50));
        orders.add(new OrderSummary("A1003", "CANCELLED", 20.00));
        return orders;
    }

    static Map<String, OrderSummary> indexById(List<OrderSummary> orders) {
        Map<String, OrderSummary> result = new LinkedHashMap<>();

        for (OrderSummary order : orders) {
            result.put(order.id(), order);
        }

        return result;
    }

    static List<OrderSummary> buildResponseInRequestOrder(
            Set<String> uniqueOrderIds,
            Map<String, OrderSummary> ordersById
    ) {
        List<OrderSummary> result = new ArrayList<>();

        for (String id : uniqueOrderIds) {
            OrderSummary order = ordersById.get(id);

            if (order == null) {
                System.out.println("Missing order id: " + id);
                continue;
            }

            result.add(order);
        }

        return result;
    }

    static Queue<AuditTask> createAuditTasks(List<OrderSummary> orders) {
        Queue<AuditTask> tasks = new ArrayDeque<>();

        for (OrderSummary order : orders) {
            if (order.needsAudit()) {
                tasks.offer(new AuditTask("AUDIT-" + order.id(), order.auditReason()));
            }
        }

        return tasks;
    }

    static void processAuditTasks(Queue<AuditTask> tasks) {
        AuditTask task = tasks.poll();

        while (task != null) {
            System.out.println("Processing audit: " + task.summaryLine());
            task = tasks.poll();
        }
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

    boolean needsAudit() {
        return "CANCELLED".equals(status) || amount >= 100.0;
    }

    String auditReason() {
        if ("CANCELLED".equals(status)) {
            return "CANCELLED_ORDER";
        }

        return "HIGH_AMOUNT";
    }

    String summaryLine() {
        return id + " | status=" + status + " | amount=" + amount;
    }
}

class AuditTask {
    private final String taskId;
    private final String reason;

    AuditTask(String taskId, String reason) {
        this.taskId = taskId;
        this.reason = reason;
    }

    String summaryLine() {
        return taskId + " | reason=" + reason;
    }
}
