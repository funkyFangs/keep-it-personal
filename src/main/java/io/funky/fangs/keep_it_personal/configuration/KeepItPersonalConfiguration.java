package io.funky.fangs.keep_it_personal.configuration;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import io.funky.fangs.keep_it_personal.exception.KeepItPersonalException;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import static io.funky.fangs.keep_it_personal.KeepItPersonalModInitializer.MOD_ID;
import static io.funky.fangs.keep_it_personal.configuration.PermissionsConfiguration.DEFAULT_PERMISSION_LEVEL;
import static io.funky.fangs.keep_it_personal.configuration.PreferencesConfiguration.DEFAULT_DISABLED;
import static io.funky.fangs.keep_it_personal.configuration.PreferencesConfiguration.DEFAULT_ENABLED;
import static java.util.stream.Collectors.joining;

public record KeepItPersonalConfiguration(
        PreferencesConfiguration preferences,
        PermissionsConfiguration permissions
) {
    private static final Logger LOGGER = LoggerFactory.getLogger(KeepItPersonalConfiguration.class);
    private static final String CONFIGURATION_ERROR_MESSAGE = "Unable to load configuration for " + MOD_ID;
    private static final File CONFIGURATION_FILE = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(MOD_ID + ".toml")
            .toFile();
    private static final String DEFAULT_CONFIGURATION_FILE = """
            [preferences]
            enabled = [%s]
            disabled = [%s]
            
            [permissions]
            permissionLevel = %d
            """.stripIndent().trim().formatted(
                    toString(DEFAULT_ENABLED),
                    toString(DEFAULT_DISABLED),
                    DEFAULT_PERMISSION_LEVEL
            );

    private static String toString(Set<DeathPreference> preferences) {
        return preferences == null
                ? null
                : preferences.stream().map(DeathPreference::toString).collect(joining(", "));
    }

    private static KeepItPersonalConfiguration INSTANCE;

    public KeepItPersonalConfiguration() {
        this(new PreferencesConfiguration(), new PermissionsConfiguration());
    }

    public static KeepItPersonalConfiguration getInstance() {
        try {
            if (INSTANCE == null) {
                if (!CONFIGURATION_FILE.exists()) {
                    LOGGER.atDebug().log("Creating new configuration file: {}", CONFIGURATION_FILE.getName());
                    if (CONFIGURATION_FILE.createNewFile()) {
                        try (PrintWriter writer = new PrintWriter(CONFIGURATION_FILE)) {
                            writer.println(DEFAULT_CONFIGURATION_FILE);
                        }
                    }
                }
                INSTANCE = new TomlMapper().readerFor(KeepItPersonalConfiguration.class).readValue(CONFIGURATION_FILE);
            }

            return INSTANCE;
        }
        catch (IOException ioException) {
            LOGGER.atError().setCause(ioException).log(CONFIGURATION_ERROR_MESSAGE);
            throw new KeepItPersonalException(CONFIGURATION_ERROR_MESSAGE);
        }
    }
}
