public class PaymentInheritanceDemo {
    public static void main(String[] args) {
        PaymentMethod card = new CardPayment("CARD-001", "Visa", "4242");
        PaymentMethod wallet = new WalletPayment("WALLET-001", "OpenPay", "frontend-user");

        PaymentMethod[] methods = {card, wallet};

        for (int i = 0; i < methods.length; i++) {
            printPaymentMethod(methods[i]);
        }
    }

    static void printPaymentMethod(PaymentMethod method) {
        System.out.println(method.displayName());
        System.out.println("Provider: " + method.getProvider());
        System.out.println("Online: " + method.canPayOnline());
        System.out.println("---");
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

    boolean canPayOnline() {
        return true;
    }

    String displayName() {
        return "Payment method " + id;
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
}
