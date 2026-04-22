package io.funky.fangs.keep_it_personal.serialization;

import net.minecraft.server.permissions.PermissionLevel;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableMap;

public class PermissionLevelDeserializer extends StdDeserializer<PermissionLevel> {
    private static final Pattern NUMERIC_PERMISSION_LEVEL_PATTERN = Pattern.compile("[0-4]");
    private static final String ERROR_MESSAGE_TEMPLATE = "Could not deserialize \"%s\" to PermissionLevel";

    /**
     * This is a mapping of each {@link PermissionLevel} from its name to itself for lookup during deserialization.
     */
    private static final Map<String, PermissionLevel> PERMISSION_LEVEL_NAME_MAP = Stream.of(PermissionLevel.values())
            .collect(toUnmodifiableMap(PermissionLevel::getSerializedName, Function.identity()));

    public PermissionLevelDeserializer() {
        // PermissionLevel is an enum and cannot be extended
        super(PermissionLevel.class);
    }

    @Override
    public PermissionLevel deserialize(JsonParser parser, DeserializationContext context) throws JacksonException {
        final var token = parser.getValueAsString();

        PermissionLevel permissionLevel;

        if (NUMERIC_PERMISSION_LEVEL_PATTERN.matcher(token).matches()) {
            try {
                final int numericPermissionLevel = Integer.parseInt(token);
                permissionLevel = PermissionLevel.byId(numericPermissionLevel);
            }
            catch (NumberFormatException ignored) {
                permissionLevel = null;
            }
        }
        else {
            permissionLevel = PERMISSION_LEVEL_NAME_MAP.get(token);
        }

        if (permissionLevel == null) {
            throw new InvalidFormatException(
                    parser,
                    ERROR_MESSAGE_TEMPLATE.formatted(token),
                    token,
                    PermissionLevel.class
            );
        }
        else {
            return permissionLevel;
        }
    }
}
