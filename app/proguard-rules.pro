# Device admin receiver and accessibility service are looked up by the
# system via the class names declared in AndroidManifest.xml (device admin
# broadcasts, accessibility service binding). AGP keeps manifest-registered
# components automatically, but these are kept explicitly for clarity.
-keep class com.naimul.touchcontrol.LockAdminReceiver { *; }
-keep class com.naimul.touchcontrol.PowerAccessibilityService { *; }
