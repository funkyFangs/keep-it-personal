package io.funky.fangs.keep_it_personal.configuration;

import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import io.funky.fangs.keep_it_personal.domain.DeathPreference;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

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
            """.trim().formatted(
                    DEFAULT_ENABLED.stream().map(DeathPreference::toString).collect(joining(", ")),
                    DEFAULT_DISABLED.stream().map(DeathPreference::toString).collect(joining(", ")),
                    DEFAULT_PERMISSION_LEVEL
            );

    private static KeepItPersonalConfiguration INSTANCE;

    public KeepItPersonalConfiguration() {
        this(new PreferencesConfiguration(), new PermissionsConfiguration());
    }

    public static KeepItPersonalConfiguration getInstance() {
        if (INSTANCE == null) {
            try {
                if (!CONFIGURATION_FILE.exists()) {
                    LOGGER.atDebug().log("Creating new configuration file: {}", CONFIGURATION_FILE.getName());
                    CONFIGURATION_FILE.createNewFile();
                    try (PrintWriter writer = new PrintWriter(CONFIGURATION_FILE)) {
                        writer.println(DEFAULT_CONFIGURATION_FILE);
                    }
                }
                INSTANCE = new TomlMapper().readerFor(KeepItPersonalConfiguration.class).readValue(CONFIGURATION_FILE);
            }
            catch (IOException e) {
                LOGGER.atError().setCause(e).log("Unable to open configuration file");
                INSTANCE = new KeepItPersonalConfiguration();
            }
        }

        return INSTANCE;
    }
}
