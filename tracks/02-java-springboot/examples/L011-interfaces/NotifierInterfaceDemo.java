public class NotifierInterfaceDemo {
    public static void main(String[] args) {
        Notifier[] notifiers = {
                new EmailNotifier("noreply@example.com"),
                new SmsNotifier("10690000"),
                new InboxNotifier()
        };

        NotificationMessage message = new NotificationMessage(
                "frontend-user",
                "Order paid",
                "Your order A1008 has been paid successfully."
        );

        for (int i = 0; i < notifiers.length; i++) {
            SendResult result = notifiers[i].send(message);
            System.out.println(result.summaryLine());
        }
    }
}

interface Notifier {
    SendResult send(NotificationMessage message);

    default boolean supportsRetry() {
        return true;
    }
}

class EmailNotifier implements Notifier {
    private final String fromAddress;

    EmailNotifier(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    @Override
    public SendResult send(NotificationMessage message) {
        return new SendResult(
                "EMAIL",
                message.receiver(),
                "from=" + fromAddress + ", title=" + message.title(),
                supportsRetry()
        );
    }
}

class SmsNotifier implements Notifier {
    private final String senderNumber;

    SmsNotifier(String senderNumber) {
        this.senderNumber = senderNumber;
    }

    @Override
    public SendResult send(NotificationMessage message) {
        return new SendResult(
                "SMS",
                message.receiver(),
                "sender=" + senderNumber + ", content=" + message.content(),
                supportsRetry()
        );
    }
}

class InboxNotifier implements Notifier {
    @Override
    public SendResult send(NotificationMessage message) {
        return new SendResult(
                "INBOX",
                message.receiver(),
                "title=" + message.title(),
                supportsRetry()
        );
    }

    @Override
    public boolean supportsRetry() {
        return false;
    }
}

class NotificationMessage {
    private final String receiver;
    private final String title;
    private final String content;

    NotificationMessage(String receiver, String title, String content) {
        if (receiver == null || receiver.isBlank()) {
            throw new IllegalArgumentException("Receiver must not be blank");
        }

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title must not be blank");
        }

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Content must not be blank");
        }

        this.receiver = receiver;
        this.title = title;
        this.content = content;
    }

    String receiver() {
        return receiver;
    }

    String title() {
        return title;
    }

    String content() {
        return content;
    }
}

class SendResult {
    private final String channel;
    private final String receiver;
    private final String detail;
    private final boolean retrySupported;

    SendResult(String channel, String receiver, String detail, boolean retrySupported) {
        this.channel = channel;
        this.receiver = receiver;
        this.detail = detail;
        this.retrySupported = retrySupported;
    }

    String summaryLine() {
        return channel
                + " -> " + receiver
                + " | " + detail
                + " | retrySupported=" + retrySupported;
    }
}
