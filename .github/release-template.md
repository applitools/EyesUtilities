EyesUtilities — reports, downloads, baseline and admin tools for Applitools Eyes. Download the installer for your machine, double-click it, and the GUI opens in your browser. Nothing else to install.

## Which file do I download?

| Your machine | File |
|---|---|
| Windows | `EyesUtilities-{{VERSION}}-Windows.msi` |
| Mac with Apple Silicon | `EyesUtilities-{{VERSION}}-macOS-AppleSilicon.dmg` |
| Mac with Intel | `EyesUtilities-{{VERSION}}-macOS-Intel.dmg` |
| Linux (Debian/Ubuntu) | `EyesUtilities-{{VERSION}}-Linux.deb` |

Not sure which Mac you have? Apple menu → **About This Mac**: "Chip: Apple M…" means Apple Silicon; "Processor: Intel…" means Intel.

## Command line (CLI jar)

Prefer the CLI? Download `EyesUtilities_{{VERSION}}.jar` and run it with Java 8+:

`java -jar EyesUtilities_{{VERSION}}.jar [command] [command specific parameters...]`

Add `--gui` (as the only argument) to open the GUI from the jar instead. See the [README](https://github.com/applitools/EyesUtilities#readme) for every command.

<!-- UNSIGNED-START -->
## First launch

These installers aren't code-signed yet (signed builds are planned), so your computer will warn you the first time you run one:

- **Windows** shows "Windows protected your PC". Click **More info**, then **Run anyway**.
- **macOS** blocks the first launch. Open **System Settings → Privacy & Security**, scroll down, click **Open Anyway** next to EyesUtilities, and confirm.
<!-- UNSIGNED-END -->

## Using the app

Launching EyesUtilities opens a page in your browser. Pick a utility from the **Utility** dropdown, fill in its parameters, and click **▶ Run**. Output streams in live, and downloaded files go to `Downloads/EyesUtilities`. Enter your API key with **🔑 Enter API key**. Keys stay in the open page only and are never written to disk.
