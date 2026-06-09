# Nonprofit's Pack Defaults

Ships a modpack's curated settings as **self-healing defaults** for Fabric 1.21.11.

A snapshot of `options.txt` and the whole `config/` folder is bundled inside the jar. At **preLaunch**
— before Minecraft and every other mod read their config — the mod restores any file that is
**missing** from the game directory.

- **Strictly additive** — it never overwrites an existing file, so your live tweaks always win.
- Only genuinely missing files (a fresh install, a wiped/reset config, a deleted `options.txt`) are
  filled back in with the intended defaults.
- Excludes machine-specific files (e.g. Sodium's hardware fingerprint) so they regenerate normally.

This directly fixes the "my settings / resource packs / keybinds reset themselves" problem: even if
something wipes a config, the curated default comes back on the next launch.

> The bundled snapshot is a point-in-time copy of the pack's config. To change what the defaults are,
> re-snapshot `options.txt` + `config/` into `src/main/resources/nonprofit-defaults/`, regenerate the
> manifest, and rebuild.

MIT licensed.
