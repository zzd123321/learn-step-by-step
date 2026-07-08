public class PaymentPolymorphismDemo {
    public static void main(String[] args) {
        PaymentMethod[] methods = {
                new CardPayment("CARD-001", "Visa", "4242"),
                new WalletPayment("WALLET-001", "OpenPay", "frontend-user"),
                new BankTransferPayment("BANK-001", "MockBank")
        };

        double orderAmount = 200.00;

        for (int i = 0; i < methods.length; i++) {
            PaymentResult result = methods[i].pay(orderAmount);
            System.out.println(result.summaryLine());
        }
    }
}

class PaymentMethod {
    private final String id;
    private final String provider;

    PaymentMethod(String id, String provider) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Payment method id must not be blank");
        }

        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("Payment provider must not be blank");
        }

        this.id = id;
        this.provider = provider;
    }

    String getId() {
        return id;
    }

    String getProvider() {
        return provider;
    }

    String displayName() {
        return "Payment method " + id;
    }

    double calculateFee(double amount) {
        return 0.0;
    }

    PaymentResult pay(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than 0");
        }

        double fee = calculateFee(amount);
        return new PaymentResult(displayName(), amount, fee);
    }
}

class CardPayment extends PaymentMethod {
    private final String lastFourDigits;

    CardPayment(String id, String provider, String lastFourDigits) {
        super(id, provider);
        this.lastFourDigits = lastFourDigits;
    }

    @Override
    String displayName() {
        return "Card " + getProvider() + " ending in " + lastFourDigits;
    }

    @Override
    double calculateFee(double amount) {
        return amount * 0.02;
    }
}

class WalletPayment extends PaymentMethod {
    private final String accountName;

    WalletPayment(String id, String provider, String accountName) {
        super(id, provider);
        this.accountName = accountName;
    }

    @Override
    String displayName() {
        return "Wallet " + getProvider() + " account " + accountName;
    }

    @Override
    double calculateFee(double amount) {
        return 1.00;
    }
}

class BankTransferPayment extends PaymentMethod {
    BankTransferPayment(String id, String provider) {
        super(id, provider);
    }

    @Override
    String displayName() {
        return "Bank transfer via " + getProvider();
    }
}

class PaymentResult {
    private final String paymentName;
    private final double amount;
    private final double fee;

    PaymentResult(String paymentName, double amount, double fee) {
        this.paymentName = paymentName;
        this.amount = amount;
        this.fee = fee;
    }

    double totalCharged() {
        return amount + fee;
    }

    String summaryLine() {
        return paymentName
                + " | amount=" + amount
                + " | fee=" + fee
                + " | total=" + totalCharged();
    }
}
