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

# IoT Hub Device Client classes to keep
-keep class com.microsoft.azure.sdk.iot.device.hsm.parser.* { *; }
-keep class com.microsoft.azure.sdk.iot.device.twin.* { *; }
-keep class com.microsoft.azure.sdk.iot.device.edge.DirectMethodRequest { *; }
-keep class com.microsoft.azure.sdk.iot.device.edge.DirectMethodResponse { *; }
-keep class com.microsoft.azure.sdk.iot.device.FileUploadCompletionNotification { *; }
-keep class com.microsoft.azure.sdk.iot.device.FileUploadSasUriRequest { *; }
-keep class com.microsoft.azure.sdk.iot.device.FileUploadSasUriResponse { *; }

# IoT Hub Service Client classes to keep
-keep class com.microsoft.azure.sdk.iot.service.configurations.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.digitaltwin.serialization.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.digitaltwin.models.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.jobs.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.messaging.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.messaging.FeedbackStatusCode { *; }
-keep class com.microsoft.azure.sdk.iot.service.methods.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.query.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.registry.serializers.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.registry.ImportMode { *; }
-keep class com.microsoft.azure.sdk.iot.service.registry.StorageAuthenticationType { *; }
-keep class com.microsoft.azure.sdk.iot.service.twin.* { *; }
-keep class com.microsoft.azure.sdk.iot.service.exceptions.ErrorMessageParser { *; }
-keep class com.microsoft.azure.sdk.iot.service.exceptions.ErrorMessage { *; }

# DPS Device Client classes to keep
-keep class com.microsoft.azure.sdk.iot.provisioning.device.internal.parser.* { *; }

# DPS Service Client classes to keep
-keep class com.microsoft.azure.sdk.iot.provisioning.service.configs.* { *; }
-keep class com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ErrorMessageParser { *; }
-keep class com.microsoft.azure.sdk.iot.provisioning.service.exceptions.ErrorMessage { *; }
