public class RequestParameterDemo {
    public static void main(String[] args) {
        SearchRequest firstRequest = SearchRequest.fromRawParams(" 2 ", "10", "  spring boot ");
        SearchRequest secondRequest = SearchRequest.fromRawParams(null, "", " ALL ");
        SearchRequest thirdRequest = SearchRequest.fromRawParams("abc", "200", "   ");

        printRequest("first", firstRequest);
        printRequest("second", secondRequest);
        printRequest("third", thirdRequest);
    }

    static void printRequest(String label, SearchRequest request) {
        System.out.println(label + ": " + request.summaryLine());
    }
}

class SearchRequest {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    private final int page;
    private final int size;
    private final String keyword;

    private SearchRequest(int page, int size, String keyword) {
        this.page = page;
        this.size = size;
        this.keyword = keyword;
    }

    static SearchRequest fromRawParams(String rawPage, String rawSize, String rawKeyword) {
        Integer parsedPage = parseInteger(rawPage);
        Integer parsedSize = parseInteger(rawSize);

        int safePage = parsedPage == null || parsedPage < 1 ? DEFAULT_PAGE : parsedPage;
        int safeSize = parsedSize == null || parsedSize < 1 ? DEFAULT_SIZE : parsedSize;

        if (safeSize > MAX_SIZE) {
            safeSize = MAX_SIZE;
        }

        String safeKeyword = normalizeKeyword(rawKeyword);

        return new SearchRequest(safePage, safeSize, safeKeyword);
    }

    static Integer parseInteger(String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return null;
        }

        try {
            return Integer.valueOf(rawValue.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    static String normalizeKeyword(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.isBlank()) {
            return "";
        }

        String keyword = rawKeyword.trim();

        if ("ALL".equalsIgnoreCase(keyword)) {
            return "";
        }

        return keyword;
    }

    String summaryLine() {
        String keywordText = keyword.isEmpty() ? "<empty>" : keyword;
        return "page=" + page + ", size=" + size + ", keyword=" + keywordText;
    }
}
