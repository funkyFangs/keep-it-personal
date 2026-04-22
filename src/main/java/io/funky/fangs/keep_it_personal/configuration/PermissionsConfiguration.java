package io.funky.fangs.keep_it_personal.configuration;

import net.minecraft.server.permissions.PermissionLevel;

public record PermissionsConfiguration(
        PermissionLevel permissionLevel
) {
    public static final PermissionLevel DEFAULT_PERMISSION_LEVEL = PermissionLevel.ALL;

    public PermissionsConfiguration() {
        this(DEFAULT_PERMISSION_LEVEL);
    }

    @Override
    public PermissionLevel permissionLevel() {
        return permissionLevel == null ? DEFAULT_PERMISSION_LEVEL : permissionLevel;
    }
}
