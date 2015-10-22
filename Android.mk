LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES :=   \
    android-support-v7_23-appcompat \
    android-support-v4_23           \
    android-support-v7_23-cardview  \
    android-support-annotations_23  \
    android-support-design_23

LOCAL_PACKAGE_NAME := AicpExtras
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

#LOCAL_SDK_VERSION := current

include $(BUILD_PACKAGE)

# Support library v4
include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := 	\
    android-support-v7-appcompat:libs/appcompat-v7-23.1.0-sources.jar         \
    android-support-v7-cardview:libs/cardview-v7-23.1.0-sources.jar           \
    android-support-design_23:libs/design-23.1.0-sources.jar

include $(BUILD_MULTI_PREBUILT)
