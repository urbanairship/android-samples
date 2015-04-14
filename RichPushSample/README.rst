RichPushSample
==============

RichPushSample is an example implementation of Rich Push for Android, using the
latest Urban Airship client library. You can use this sample for playing with
Rich Push, or as a reference point for integrating Rich Push into your own project.

Source files
------------

AbstractInboxFragment.java
   An abstract class that implements the basic functionality for a rich push inbox.

AddTagsPreference.java
   DialogPreference to add the tags.

HomeFragment.java
   Simple place holder fragment for the home screen of the application.

InboxFragment.java
   An AbstractInboxFragment that shows the inbox with an action mode.

MainActivity.java
   The main application activity.

MessageActivity.java
   A simple Activity that displays a MessagePagerFragment.  This is only used on phones.

MessageFragment.java
   A Fragment that contains a WebView that displays the contents of the RichPushMessage.

MessageFragmentAdapter.java
   A FragmentAdapter for a ViewPager that displays rich push messages.

MessagePagerFragment.java
   A Fragment that shows MessageFragments in a view pager.

ParseDeepLinkActivity.java
   An activity that handles parsing deep links.

PushPreferencesActivity.java
   Displays the preferences.

PushReceiver.java
   Broadcast receiver to handle all push notifications.

RichPushApplication.java
   The main Rich Push application.

RichPushNotificationFactory.java
   A custom push notification builder to create inbox style notifications for rich push messages.

RichPushWidgetProvider.java
   The widget provider for the rich push inbox.

RichPushWidgetService.java
   Service that provides the factory to be bound to the collection service.

SetAliasPreference.java
   DialogPreference to set the alias.

SetNamedUserPreference.java
   DialogPreference to set the named user.

ViewBinderArrayAdapter.java
   A generic base adapter that binds items to views using the ViewBinder interface.


Resources
---------

layout/activity_landing_page.xml
   Custom landing page layout.

layout/activity_main.xml
   Layout for the MainActivity.

layout/activity_message.xml
   Layout for the MessageActivity.

layout/cab_selection_dropdown.xml
   The select all/deselect all action item in the Inbox CAB.

layout/dialog_add_tags.xml
   Layout for add tags dialog.

layout/fragment_home.xml
   Layout for the HomeFragment.

layout/fragment_inbox.xml
   Layout for the InboxFragment.

layout/fragment_message.xml
   Layout for MessageFragments.

layout/fragment_message_pager.xml
   Layout for the MessagePagerFragment.

layout/inbox_list_item.xml
   Layout for the inbox line items.

layout/navigation_item.xml
   Layout for the navigation items.

layout/tag_preference_item.xml
   Layout for the tag preference.

layout/widget_item.xml
   Layout for an inbox line item in the large widget layout.

layout/widget_layout_small.xml
   The small widget inbox layout with an unread message count.

layout/widget_layout.xml
   The large widget inbox layout with a list of messages.

menu/inbox_actions_menu.xml
   Menu of items that appear in the action bar when a message is selected in the InboxActivity.

menu/main_menu.xml
   Menu items that appear in the action bar in the MainActivity.

menu/message_activity.xml
   Menu items that appear in the action bar in the MessageActivity.

menu/selection.xml
   Menu items that appears for the select/deselect all action item.

values/colors.xml
   Defines the default colors for the app.

values/dimens.xml
   Defines the default layout dimensions for the widget margin.

values-v14/dimens.xml
   Defines the api 14 and above layout dimensions for the widget margin.

values/strings.xml
   Defines all the string resources.

values/styles.xml
   Defines the application's style.

xml/advanced_preferences.xml
   Defines the preferences for APID, Rich Push User, setting tags and aliases.

xml/analytics_preferences.xml
   Defines the preferences for sending usage data.

xml/location_preferences.xml
   Defines the preferences for location settings.

xml/push_preferences.xml
   Defines the preferences for push settings.

xml/widgetinfo.xml
   Defines the inbox widget information for devices api 10 and below.

xml-v11/widgetinfo.xml
   Defines the inbox widget information for devices api 11 and above.

