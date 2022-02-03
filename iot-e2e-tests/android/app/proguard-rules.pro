# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\v-askhur.REDMOND\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# JSON parser classes don't mix well with minification because JSON parser classes use reflection
# based on the name of the field to create the JSON, and minification changes those names. As a result
# all the message payload serializing and deserializing breaks. These lines explicitly exclude
# the packages where we keep our JSON parsing classes from minification to avoid this issue
-keep class com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.* { *; }
-keep class com.microsoft.azure.sdk.iot.deps.serializer.* { *; }
-keep class com.microsoft.azure.sdk.iot.provisioning.service.configs.* { *; }
-keep class com.microsoft.azure.sdk.iot.deps.twin.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.jobs.registry.ImportMode { *; }
-keep class com.microsoft.azure.sdk.iot.service.AuthenticationMechanism { *; }
-keep class com.microsoft.azure.sdk.iot.device.edge.MethodRequest { *; }
-keep class com.microsoft.azure.sdk.iot.device.hsm.parser.ErrorResponse { *; }
-keep class com.microsoft.azure.sdk.iot.device.hsm.parser.SignRequest { *; }