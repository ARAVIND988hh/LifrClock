# LifeClock Android starter

A native Kotlin Android live-wallpaper starter project.

## Open
1. Install Android Studio.
2. Choose **Open** and select the `LifeClock` folder.
3. Allow Gradle sync to finish.
4. Connect an Android phone or start an emulator.
5. Run the `app` configuration.
6. Select your date of birth and tap **Set LifeClock wallpaper**.
7. Confirm the Android live-wallpaper preview.

## Behavior
- Uses the date of birth saved locally in SharedPreferences.
- Uses DOB + 60 calendar years as the reference end date.
- Calculates lived days, remaining days, percentage, and remaining weeks.
- Refreshes the wallpaper once per minute while visible. The day-based values therefore stay current without frequent network access.
- No account, analytics, or server is included.

## Important notes
The 60-year lifespan is an illustrative personal target, not a medical or actuarial prediction.
This is a starter implementation. Test on a physical device and adjust typography/positioning for different screen sizes and lock-screen clock layouts before release.


## Build an APK with GitHub Actions

The workflow at `.github/workflows/android.yml` builds a debug APK on pushes, pull requests, and manual runs. The APK is available under the workflow run's **Artifacts** section. Push a version tag such as `v1.0.0` to publish the APK as a GitHub Release asset.

Example:
```bash
git tag v1.0.0
git push origin v1.0.0
```

The debug APK is suitable for testing and direct installation. Before a public production release, configure a private Android signing key in GitHub Actions secrets and build a signed release APK or Android App Bundle.
