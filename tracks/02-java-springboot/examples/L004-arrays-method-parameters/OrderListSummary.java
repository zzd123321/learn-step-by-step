public class OrderListSummary {
    public static void main(String[] args) {
        double[] orderAmounts = {99.90, 149.50, 20.00, 300.00};

        printOrderAmounts(orderAmounts);

        double total = sum(orderAmounts);
        double average = average(orderAmounts);
        double max = max(orderAmounts);

        System.out.println("Total amount: " + total);
        System.out.println("Average amount: " + average);
        System.out.println("Max amount: " + max);

        applyDemoMutation(orderAmounts);
        System.out.println("First amount after method call: " + orderAmounts[0]);
    }

    static void printOrderAmounts(double[] amounts) {
        for (int i = 0; i < amounts.length; i++) {
            System.out.println("Order #" + (i + 1) + ": " + amounts[i]);
        }
    }

    static double sum(double[] amounts) {
        double total = 0.0;

        for (int i = 0; i < amounts.length; i++) {
            total = total + amounts[i];
        }

        return total;
    }

    static double average(double[] amounts) {
        if (amounts.length == 0) {
            return 0.0;
        }

        return sum(amounts) / amounts.length;
    }

    static double max(double[] amounts) {
        if (amounts.length == 0) {
            return 0.0;
        }

        double currentMax = amounts[0];

        for (int i = 1; i < amounts.length; i++) {
            if (amounts[i] > currentMax) {
                currentMax = amounts[i];
            }
        }

        return currentMax;
    }

    static void applyDemoMutation(double[] amounts) {
        if (amounts.length > 0) {
            amounts[0] = amounts[0] + 1.0;
        }
    }
}
