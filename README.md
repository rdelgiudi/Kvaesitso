# Kvaesitso + Smartspacer: auto-rebase pipeline

Daily GitHub Actions job that rebases your `smartspacer` branch onto the latest
upstream Kvaesitso release, builds a signed APK, and publishes it as a GitHub
Release. If the rebase conflicts, it opens an issue instead.

## Branch layout

| Branch        | Contents                                              |
|---------------|-------------------------------------------------------|
| `ci` (default)| Only this README and the workflow                     |
| `smartspacer` | Upstream Kvaesitso + your feature commits on top      |

The workflow must sit on the default branch for the schedule to fire. Keeping
upstream code off the default branch also stops upstream's own workflows
(nightly build, F-Droid trigger) from running in your fork.

## One-time setup

1. **Fork** MM2-0/Kvaesitso on GitHub.

2. **Create the `ci` branch** with only these files and make it the default:
   ```sh
   git clone https://github.com/<you>/Kvaesitso.git && cd Kvaesitso
   git switch --orphan ci
   cp -r /path/to/this/folder/{README.md,.github} .
   git add . && git commit -m "Rebase & build pipeline"
   git push -u origin ci
   ```
   Then on GitHub: Settings → General → Default branch → `ci`.

3. **Create the feature branch** from the latest release tag:
   ```sh
   git remote add upstream https://github.com/MM2-0/Kvaesitso.git
   git fetch upstream --tags
   git switch -c smartspacer v1.41.0   # latest vX.Y.Z
   # ...your Smartspacer commits...
   git push -u origin smartspacer
   ```

4. **Make a signing key** (keep a backup — losing it means uninstall/reinstall
   to update):
   ```sh
   keytool -genkeypair -v -keystore kvaesitso.jks -alias kvaesitso \
     -keyalg RSA -keysize 4096 -validity 10000
   base64 -w0 kvaesitso.jks > kvaesitso.jks.b64
   ```

5. **Add repository secrets** (Settings → Secrets and variables → Actions):

   | Secret                 | Value                          |
   |------------------------|--------------------------------|
   | `KEYSTORE`             | contents of `kvaesitso.jks.b64`|
   | `KEYSTORE_PASSWORD`    | keystore password              |
   | `SIGNING_KEY_ALIAS`    | `kvaesitso`                    |
   | `SIGNING_KEY_PASSWORD` | key password                   |

6. **Enable Actions** in the fork (Actions tab → "I understand…") and run the
   workflow once by hand with *force_build* ticked.

## On the phone

Add the repo to [Obtainium](https://github.com/ImranR98/Obtainium) as a GitHub
source; it picks up each new release automatically.

The APK uses upstream's `nightly` build type, so its package is
`de.mm20.launcher2.nightly`. It installs alongside the official
`de.mm20.launcher2.release`; you can export settings from one and import into
the other.

## Behaviour

- **Upstream unchanged** → nothing happens.
- **New upstream release** → rebase, push (old state saved as a
  `backup/smartspacer-<timestamp>` tag), build, release.
- **Conflict** → rebase aborted, branch untouched, issue opened listing the
  conflicting files. Resolve locally (commands are in the issue) and push; the
  next run builds it.
- **Manual run** → can track `main` instead of releases, or force a rebuild.
