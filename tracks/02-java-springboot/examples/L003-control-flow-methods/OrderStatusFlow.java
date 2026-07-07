import java.util.Locale;

public class OrderStatusFlow {
    public static void main(String[] args) {
        String[] statuses = args.length > 0
                ? args
                : new String[] {"created", "PAID", "shipped", "cancelled", "unknown"};

        for (int i = 0; i < statuses.length; i++) {
            String status = normalizeStatus(statuses[i]);
            String message = describeStatus(status);
            boolean supportVisible = shouldShowSupport(status);

            System.out.println(formatLine(i + 1, status, message, supportVisible));
        }
    }

    static String normalizeStatus(String rawStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return "UNKNOWN";
        }

        return rawStatus.trim().toUpperCase(Locale.ROOT);
    }

    static String describeStatus(String status) {
        switch (status) {
            case "CREATED":
                return "Order created, waiting for payment";
            case "PAID":
                return "Payment received, waiting for shipment";
            case "SHIPPED":
                return "Package shipped, waiting for delivery";
            case "CANCELLED":
                return "Order cancelled";
            default:
                return "Unknown status, confirm backend mapping";
        }
    }

    static boolean shouldShowSupport(String status) {
        if ("CANCELLED".equals(status)) {
            return true;
        }

        switch (status) {
            case "CREATED":
            case "PAID":
            case "SHIPPED":
                return false;
            default:
                return true;
        }
    }

    static String formatLine(int index, String status, String message, boolean supportVisible) {
        return "#" + index
                + " [" + status + "] "
                + message
                + " | supportVisible=" + supportVisible;
    }
}
