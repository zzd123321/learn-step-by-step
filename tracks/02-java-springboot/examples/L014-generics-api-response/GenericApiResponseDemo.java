public class GenericApiResponseDemo {
    public static void main(String[] args) {
        ApiResponse<OrderDetail> detailResponse = ApiResponse.success(
                new OrderDetail("A1001", "PAID", 99.90)
        );

        ApiResponse<SearchPage> pageResponse = ApiResponse.success(
                new SearchPage(1, 20, 58)
        );

        ApiResponse<Void> errorResponse = ApiResponse.fail(
                "ORDER_NOT_FOUND",
                "Order A404 was not found"
        );

        printOrderDetail(detailResponse);
        printSearchPage(pageResponse);
        printAnyResponse(errorResponse);
    }

    static void printOrderDetail(ApiResponse<OrderDetail> response) {
        OrderDetail detail = response.data();
        System.out.println(response.code() + " | order=" + detail.summaryLine());
    }

    static void printSearchPage(ApiResponse<SearchPage> response) {
        SearchPage page = response.data();
        System.out.println(response.code() + " | page=" + page.summaryLine());
    }

    static void printAnyResponse(ApiResponse<?> response) {
        System.out.println(response.code() + " | message=" + response.message());
    }
}

class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String message, T data) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "OK", "success", data);
    }

    static ApiResponse<Void> fail(String code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }

    boolean success() {
        return success;
    }

    String code() {
        return code;
    }

    String message() {
        return message;
    }

    T data() {
        return data;
    }
}

class OrderDetail {
    private final String id;
    private final String status;
    private final double amount;

    OrderDetail(String id, String status, double amount) {
        this.id = id;
        this.status = status;
        this.amount = amount;
    }

    String summaryLine() {
        return id + " status=" + status + " amount=" + amount;
    }
}

class SearchPage {
    private final int page;
    private final int size;
    private final int total;

    SearchPage(int page, int size, int total) {
        this.page = page;
        this.size = size;
        this.total = total;
    }

    String summaryLine() {
        return "page=" + page + ", size=" + size + ", total=" + total;
    }
}
