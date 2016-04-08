Releasing
========

 1. Change the version in `build.gradle` to a non-SNAPSHOT version.
 2. Update the `CHANGELOG.md` for the impending release.
 3. `git commit -am "Prepare for release X.Y.Z."` (where X.Y.Z is the new version)
 4. `git tag -a X.Y.Z -m "Version X.Y.Z"` (where X.Y.Z is the new version)
 5. `./gradlew bintrayUpload`
 6. Update the `build.gradle` to the next SNAPSHOT version.
 7. `git commit -am "Prepare next development version."`
 8. `git push && git push --tags`
