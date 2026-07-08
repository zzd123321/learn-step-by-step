public class ExceptionBasicsDemo {
    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        OrderController controller = new OrderController(orderService);

        String[] requestIds = {"A1001", "   ", "A404"};

        for (int i = 0; i < requestIds.length; i++) {
            ApiResponse response = controller.getOrder(requestIds[i]);
            System.out.println(response.summaryLine());
        }
    }
}

class OrderController {
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    ApiResponse getOrder(String rawOrderId) {
        try {
            Order order = orderService.findById(rawOrderId);
            return ApiResponse.success(order.summaryLine());
        } catch (InvalidRequestException exception) {
            return ApiResponse.fail("BAD_REQUEST", exception.getMessage());
        } catch (OrderNotFoundException exception) {
            return ApiResponse.fail("ORDER_NOT_FOUND", exception.getMessage());
        }
    }
}

class OrderService {
    Order findById(String rawOrderId) {
        String orderId = normalizeOrderId(rawOrderId);

        if ("A1001".equals(orderId)) {
            return new Order(orderId, "PAID", 99.90);
        }

        throw new OrderNotFoundException("Order " + orderId + " was not found");
    }

    private String normalizeOrderId(String rawOrderId) {
        if (rawOrderId == null || rawOrderId.isBlank()) {
            throw new InvalidRequestException("Order id must not be blank");
        }

        return rawOrderId.trim().toUpperCase();
    }
}

class Order {
    private final String id;
    private final String status;
    private final double amount;

    Order(String id, String status, double amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    String summaryLine() {
        return "Order " + id + " | status=" + status + " | amount=" + amount;
    }
}

class ApiResponse {
    private final boolean success;
    private final String code;
    private final String message;

    private ApiResponse(boolean success, String code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    static ApiResponse success(String message) {
        return new ApiResponse(true, "OK", message);
    }

    static ApiResponse fail(String code, String message) {
        return new ApiResponse(false, code, message);
    }

    String summaryLine() {
        return "success=" + success + " | code=" + code + " | message=" + message;
    }
}

class InvalidRequestException extends RuntimeException {
    InvalidRequestException(String message) {
        super(message);
    }
}

class OrderNotFoundException extends RuntimeException {
    OrderNotFoundException(String message) {
        super(message);
    }
}
