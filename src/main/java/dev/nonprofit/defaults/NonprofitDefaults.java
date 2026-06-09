package dev.nonprofit.defaults;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Restores the pack's curated default settings when they're missing — before the game reads them.
 *
 * <p>A snapshot of {@code options.txt} and the {@code config/} folder is bundled in this jar under
 * {@code /nonprofit-defaults/files/...}, indexed by {@code /nonprofit-defaults/manifest.txt}. On
 * {@link PreLaunchEntrypoint preLaunch} (which runs before Minecraft and every other mod loads its
 * config) we walk the manifest and copy any file that does not already exist in the game directory.
 *
 * <p>It is strictly additive: an existing file is never touched, so the player's own changes always
 * win. Only genuinely missing files — a fresh install, a wiped/reset config, a deleted options.txt —
 * are filled back in with the intended defaults. Fully guarded; any failure is logged, never fatal.
 */
public final class NonprofitDefaults implements PreLaunchEntrypoint {

    public static final Logger LOGGER = LoggerFactory.getLogger("nonprofit-defaults");

    private static final String FILES_ROOT = "/nonprofit-defaults/files/";
    private static final String MANIFEST   = "/nonprofit-defaults/manifest.txt";

    @Override
    public void onPreLaunch() {
        try {
            Path gameDir = FabricLoader.getInstance().getGameDir();
            int restored = 0, present = 0, failed = 0;

            for (String rel : readManifest()) {
                Path target = gameDir.resolve(rel);
                if (Files.exists(target)) { present++; continue; }
                try (InputStream in = NonprofitDefaults.class.getResourceAsStream(FILES_ROOT + rel)) {
                    if (in == null) continue;
                    if (target.getParent() != null) Files.createDirectories(target.getParent());
                    Files.copy(in, target);
                    restored++;
                } catch (Exception e) {
                    failed++;
                    LOGGER.warn("nonprofit-defaults: could not restore '{}': {}", rel, e.toString());
                }
            }

            if (restored > 0)
                LOGGER.info("nonprofit-defaults: restored {} missing default file(s); {} already present{}.",
                        restored, present, failed > 0 ? (", " + failed + " failed") : "");
            else
                LOGGER.info("nonprofit-defaults: all {} default file(s) already present — nothing to restore.", present);
        } catch (Throwable t) {
            LOGGER.error("nonprofit-defaults: restore pass failed", t);
        }
    }

    private static List<String> readManifest() {
        List<String> out = new ArrayList<>();
        try (InputStream in = NonprofitDefaults.class.getResourceAsStream(MANIFEST)) {
            if (in == null) return out;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = r.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) out.add(line);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("nonprofit-defaults: could not read manifest: {}", e.toString());
        }
        return out;
    }
}
