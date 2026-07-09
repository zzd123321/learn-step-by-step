import java.util.ArrayDeque;
import java.util.Queue;

public class QueueTaskProcessingDemo {
    public static void main(String[] args) {
        Queue<NotificationTask> tasks = new ArrayDeque<>();

        tasks.offer(new NotificationTask("T1001", "A1001", "PAYMENT_SUCCESS"));
        tasks.offer(new NotificationTask("T1002", "A1002", "ORDER_CANCELLED"));
        tasks.offer(new NotificationTask("T1003", "A1003", "ORDER_SHIPPED"));

        System.out.println("Pending task count: " + tasks.size());
        System.out.println("Next task: " + tasks.peek().summaryLine());

        processAll(tasks);

        System.out.println("Pending task count after processing: " + tasks.size());
        System.out.println("Polling empty queue: " + tasks.poll());
    }

    static void processAll(Queue<NotificationTask> tasks) {
        NotificationTask task = tasks.poll();

        while (task != null) {
            System.out.println("Processing: " + task.summaryLine());
            task = tasks.poll();
        }
    }
}

class NotificationTask {
    private final String taskId;
    private final String orderId;
    private final String eventType;

    NotificationTask(String taskId, String orderId, String eventType) {
        this.taskId = taskId;
        this.orderId = orderId;
        this.eventType = eventType;
    }

    String summaryLine() {
        return taskId + " | order=" + orderId + " | event=" + eventType;
    }
}
