import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

public class SetUniquenessDemo {
    public static void main(String[] args) {
        Set<String> tags = new LinkedHashSet<>();

        tags.add("java");
        tags.add("spring");
        boolean duplicateTagAdded = tags.add("java");
        tags.add("api");

        System.out.println("Tags: " + tags);
        System.out.println("Was duplicate tag added: " + duplicateTagAdded);
        System.out.println("Tag count: " + tags.size());

        Set<String> blockedUserIds = new HashSet<>();
        blockedUserIds.add("u1001");
        blockedUserIds.add("u1002");

        System.out.println("Can u1002 comment: " + canComment(blockedUserIds, "u1002"));
        System.out.println("Can u2001 comment: " + canComment(blockedUserIds, "u2001"));

        Set<RolePermission> permissions = new LinkedHashSet<>();
        permissions.add(new RolePermission("order", "read"));
        permissions.add(new RolePermission("order", "write"));
        boolean duplicatePermissionAdded = permissions.add(new RolePermission("ORDER", "READ"));

        System.out.println("Permission count: " + permissions.size());
        System.out.println("Duplicate permission added: " + duplicatePermissionAdded);
        System.out.println("Permissions:");

        for (RolePermission permission : permissions) {
            System.out.println(permission.label());
        }
    }

    static boolean canComment(Set<String> blockedUserIds, String userId) {
        return !blockedUserIds.contains(userId);
    }
}

class RolePermission {
    private final String module;
    private final String action;

    RolePermission(String module, String action) {
        this.module = normalize(module);
        this.action = normalize(action);
    }

    String label() {
        return module + ":" + action;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof RolePermission)) {
            return false;
        }

        RolePermission permission = (RolePermission) other;
        return module.equals(permission.module) && action.equals(permission.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(module, action);
    }

    private static String normalize(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
