# Urban Airship Sample

Sample application that show case the Urban Airship SDK.

## Setup

1) Update the Android SDK Manager
- Verify Android Support Repository and Google Repository (under Extras) are installed and up-to-date

2) Import the project into Android Studio
- Open Android Studio to welcome screen
- Import project, select the root directory

3) Create an airshipconfig.properties file in the assets directory with your application's config:

```
developmentAppKey = Your Development App Key
developmentAppSecret = Your Development App Secret
productionAppKey = Your Production App Key
productionAppSecret = Your Production App Secret

gcmSender = Your GCM sender ID is your Google API project number (required for GCM)
inProduction = false

# LogLevel is "VERBOSE", "DEBUG", "INFO", "WARN", "ERROR" or "ASSERT"
developmentLogLevel = DEBUG
productionLogLevel = ERROR
```

## Links
- [Urban Airship Android SDK docs](http://docs.urbanairship.com/platform/android.html)
- [Urban Airship Android SDK javadocs](http://docs.urbanairship.com/reference/libraries/android/latest/reference/packages.html)
- [Amazon setup docs](http://docs.urbanairship.com/reference/push-providers/adm.html#set-up-adm)
- [GCM setup docs](http://docs.urbanairship.com/reference/push-providers/gcm.html#android-gcm-setup)
