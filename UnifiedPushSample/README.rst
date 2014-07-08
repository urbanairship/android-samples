Unified PushSample
====================

The unified PushSample, is an example of how to create two separate version for the Google and Amazon
app store with different versions of the Urban Airship SDK. In a future release, the Urban Airship
SDKs will be unified for both Amazon (ADM) and Google (GCM), but for now this is a stopgap solution to
the problem. This sample is only compatible with Android Studio and the gradle build system. It takes
advantage of the new manifest merger and application flavors.

Under the src directory there are 3 folders - main, google, and amazon. The main directory contains
the common application code and resources. The google and amazon are product flavors that contain any
requires changes in the source code, resources, and manifest.

Most of the APIs are the same between the two Urban Airship SDKs with exceptions of the push token
(APIDs and Channels) and the location module. To bridge the differences between the SDKs, an
UrbanAirshipShim file can be found in each of the flavor's java directories. The shim provides a
common interface for any differences in the APIs.

Android Studio Setup
--------------------

Add Urban Airship Library SDK:
  - Copy Urban Airship Library JAR into UnifiedPushSample/libs/google/
  - Copy the Urban Airship Amazon Library JAR into UnifiedPushSample/libs/amazon/

Android SDK Manager:
  - Install updates
  - Install Android Support Repository and Android Support Library under Extras

Import Project:
 - Open Android Studio to welcome screen
 - Import project, Select the samples root directory

Urban Airship Setup
-------------------

Amazon Setup:
 - Follow setup instructions from the docs - http://docs.urbanairship.com/build/build_push.html#amazon
 - Place the api_key.txt into UnifiedPushSample/src/amazon/assets/

Google Setup:
 - Follow the setup instructions from the docs - http://docs.urbanairship.com/build/build_push.html#android
 - The GCM sender id can be placed in the main assets airshipconfig.properties


