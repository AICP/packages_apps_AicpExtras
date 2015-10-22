LOCAL_PATH:= $(call my-dir)

# We need to add some special AAPT flags to generate R classes
# for resources that are included from the libraries.
include $(CLEAR_VARS)
LOCAL_PACKAGE_NAME := AicpExtras

LOCAL_MODULE_TAGS := optional

LOCAL_SDK_VERSION := current

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_STATIC_JAVA_LIBRARIES := \
        android-support-v4 \
        android-support-v7-appcompat \
        android-support-v7-cardview \
        android-support-annotations \
        android-support-design
LOCAL_RESOURCE_DIR = \
        $(LOCAL_PATH)/res \
        frameworks/support/v7/appcompat/res \
        frameworks/support/v7/cardview/res \
        frameworks/support/design/res \

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_AAPT_FLAGS := \
        --auto-add-overlay \
        --extra-packages android.support.v7.appcompat \
        --extra-packages android.support.v7.cardview \
        --extra-packages android.support.annotations \
        --extra-packages android.support.design.* 
include $(BUILD_PACKAGE)
