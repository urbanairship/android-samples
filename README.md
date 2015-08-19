# Urban Airship Samples

Sample applications that show case the Urban Airship SDK.

## Android Studio Setup


1) Update the Android SDK Manager
- Verify Android Support Repository and Google Repository (under Extras) are installed and up-to-date

2) Import the project into Android Studio
- Open Android Studio to welcome screen
- Import project, select the `samples` root directory, not the individual samples directories.

3) Update each sample's AirshipConfig.properties file with your application's configuration
- [Amazon setup docs](http://docs.urbanairship.com/reference/push-providers/adm.html#set-up-adm)
- [GCM setup docs](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup)


## Eclipse Setup

### Push Sample:

1) Update the Android SDK Manager
- Verify Android Support Library and Google Play Services (under Extras) are installed and up-to-date

2) Download the Android/Amazon library from [Developer Resources](http://urbanairship.com/resources/developer-resources)

3) Import projects
- Eclipse -> Import Android Project -> Samples/PushSample
- Eclipse -> Import Android Project -> urbanairship-lib (from download)

4) Add the Urban Airship library project as a dependency
- Follow [Reference Library Project](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject)

5) Add the v4 support library and the v7 cardview library as a dependency
- Follow [Support Library Setup](http://developer.android.com/tools/support-library/setup.html)

6) (GCM Only) Set up Google Play services
- Follow [Google Play Services Setup](http://developer.android.com/google/play-services/setup.html#Setup)

7) Update AirshipConfig.properties file with your application's configuration
- [Amazon setup docs](http://docs.urbanairship.com/reference/push-providers/adm.html#set-up-adm)
- [GCM setup docs](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup)

8) Replace AndroidManifest.xml with Eclipse_AndroidManifest.xml

### Rich Push Sample:

1) Update the Android SDK Manager
- Verify Android Support Library and Google Play Services (under Extras) are installed and up-to-date

2) Download the Android/Amazon library from [Developer Resources](http://urbanairship.com/resources/developer-resources)

3) Import projects
- Eclipse -> Import Android Project -> Samples/RichPushSample
- Eclipse -> Import Android Project -> urbanairship-lib (from download)

4) Add the Urban Airship library project as a dependency
- Follow [Reference Library Project](http://developer.android.com/tools/projects/projects-eclipse.html#ReferencingLibraryProject)

5) Add the v4 support library, v7 appcompat library, and the v7 cardview library as dependencies
- Follow [Support Library Setup](http://developer.android.com/tools/support-library/setup.html)

6) (GCM Only) Set up Google Play services
- Follow [Google Play Services Setup](http://developer.android.com/google/play-services/setup.html#Setup)

7) Update AirshipConfig.properties file with your application's configuration
- [Amazon setup docs](http://docs.urbanairship.com/reference/push-providers/adm.html#set-up-adm)
- [GCM setup docs](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup)

8) Replace AndroidManifest.xml with Eclipse_AndroidManifest.xml

## Further Reading

For more information on using Rich Push for Android see the [tutorial](http://docs.urbanairship.com/topic-guides/android-rp-tutorial.html)
