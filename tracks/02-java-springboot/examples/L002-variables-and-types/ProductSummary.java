public class ProductSummary {
    public static void main(String[] args) {
        String productName = "Wireless Mouse";
        int stock = 12;
        double price = 99.90;
        boolean available = stock > 0;
        char currencySymbol = '$';
        final double discountRate = 0.10;

        double discountedPrice = price * (1 - discountRate);

        System.out.println("Product: " + productName);
        System.out.println("Stock: " + stock);
        System.out.println("Available: " + available);
        System.out.println("Original price: " + currencySymbol + price);
        System.out.println("Discounted price: " + currencySymbol + discountedPrice);

        stock = stock - 1;
        System.out.println("Stock after one order: " + stock);
    }
}
