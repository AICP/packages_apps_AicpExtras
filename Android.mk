#  Copyright (C) 2015 The Android Open Source Project
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/appcompat/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/cardview/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/preference/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v7/recyclerview/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/v14/preference/res
LOCAL_RESOURCE_DIR += $(SUPPORT_LIBRARY_ROOT)/design/res

LOCAL_ASSET_DIR := $(LOCAL_PATH)/assets


LOCAL_STATIC_JAVA_LIBRARIES := particles
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-cardview
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-preference
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v14-preference
LOCAL_STATIC_JAVA_LIBRARIES += android-support-design

# Apache http for stats
LOCAL_JAVA_LIBRARIES := org.apache.http.legacy

LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat:android.support.v7.cardview:android.support.v7.preference:android.support.v7.recyclerview:android.support.v14.preference:android.support.design

LOCAL_PACKAGE_NAME := AicpExtras

LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := particles:lib/LeonidsLib-1.3.2.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))
