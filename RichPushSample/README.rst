RichPushSample
==============

Introduction
------------

RichPushSample is an example implementation of Rich Push for Android, using the latest Urban Airship client library.  You can use this sample for playing with Rich Push, or as a reference point for integrating Rich Push into your own project. 

Dependencies
------------

Because of the UI involved, RichPushSample requires a few dependencies.  Be sure to install these first before working with the app.

- Urban Airship Android Library (3.0)
- ActionBarSherlock_ (4.2.0+) 
- `Android Support v4 Library Revision 13`_

.. _ActionBarSherlock: http://actionbarsherlock.com/
.. _`Android Support v4 Library Revision 13`: http://developer.android.com/tools/extras/support-library.html 

Installation
------------

RichPushSample uses a combination of JAR files and library projects for its dependencies.  As with the existing PushSample app, the JAR files must be installed in the libs/ subdirectory:

- Copy Urban Airship Library JAR into RichPushSample/libs/
- Copy Android Support Library JAR into RichPushSample/libs/

ActionBarSherlock is used in this case as an Android library project: 

- Install ActionBarSherlock project parallel to RichPushSample in the filesystem
- Double check that the library reference is set up correctly in Eclipse 

  - Navigate to RichPushSample -> Properties -> Android
  - ActionBarSherlock should show up in the "Library" section with a green checkmark

If you have updated to ADT 22, you may need to include the Android Private Libraries:
  - Right-click on RichPushSample project -> Properties > Java Build Path > Order and Export and check the Android Private Libraries


Further Reading
---------------

For more information on using Rich Push for Android see our documentation_ site.

.. _documentation: http://docs.urbanairship.com
