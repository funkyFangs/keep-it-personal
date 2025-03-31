package io.funky.fangs.keep_it_personal.configuration;

public record PermissionsConfiguration(
        Integer permissionLevel
) {
    public static final int DEFAULT_PERMISSION_LEVEL = 0;

    public PermissionsConfiguration() {
        this(DEFAULT_PERMISSION_LEVEL);
    }
}
