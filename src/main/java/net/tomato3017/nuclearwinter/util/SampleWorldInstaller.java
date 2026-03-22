package net.tomato3017.nuclearwinter.util;

import net.neoforged.fml.loading.FMLPaths;
import net.tomato3017.nuclearwinter.NuclearWinter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class SampleWorldInstaller {
    private static final String RESOURCE_PATH = "/data/nuclearwinter/sample_world.zip";
    private static final String WORLD_NAME = "NuclearWinter Sample";

    public static void install() {
        Path savesDir = FMLPaths.GAMEDIR.get().resolve("saves");
        Path targetDir = savesDir.resolve(WORLD_NAME);

        if (Files.exists(targetDir)) {
            NuclearWinter.LOGGER.debug("Sample world already exists at {}, skipping extraction", targetDir);
            return;
        }

        NuclearWinter.LOGGER.info("Extracting sample world to {}", targetDir);

        try (InputStream is = SampleWorldInstaller.class.getResourceAsStream(RESOURCE_PATH)) {
            if (is == null) {
                NuclearWinter.LOGGER.error("Sample world resource not found at {}", RESOURCE_PATH);
                return;
            }

            Files.createDirectories(targetDir);

            try (ZipInputStream zip = new ZipInputStream(is)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    Path entryPath = targetDir.resolve(entry.getName()).normalize();
                    if (!entryPath.startsWith(targetDir)) {
                        NuclearWinter.LOGGER.warn("Skipping zip entry outside target directory: {}", entry.getName());
                        zip.closeEntry();
                        continue;
                    }
                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                    } else {
                        Files.createDirectories(entryPath.getParent());
                        Files.copy(zip, entryPath);
                    }
                    zip.closeEntry();
                }
            }

            NuclearWinter.LOGGER.info("Sample world extracted successfully to {}", targetDir);
        } catch (IOException e) {
            NuclearWinter.LOGGER.error("Failed to extract sample world", e);
        }
    }
}
