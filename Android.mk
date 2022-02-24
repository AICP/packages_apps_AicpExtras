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

GENERATED_AE_FRAGMENT_LIST = $(TARGET_OUT_INTERMEDIATES)/AicpExtrasCodeGeneration/src/com/aicp/extras/search/AeFragmentList.java

# Auto-generate AeFragmentList for searchable fragments
$(GENERATED_AE_FRAGMENT_LIST): $(LOCAL_PATH)/gather_search_fragments.sh $(LOCAL_PATH)/AeFragmentList.java $(foreach dir, res/xml/ src/com/aicp/extras/fragments/, $(wildcard $(LOCAL_PATH)/$(dir)/*))
	bash $< $@

LOCAL_MODULE_TAGS := optional

LOCAL_PROGUARD_ENABLED := disabled

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_GENERATED_SOURCES += $(GENERATED_AE_FRAGMENT_LIST)

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res

LOCAL_STATIC_ANDROID_LIBRARIES := \
    androidx.core_core \
    androidx.preference_preference \
    androidx.appcompat_appcompat \
    androidx.cardview_cardview \
    androidx.recyclerview_recyclerview \
    com.google.android.material_material \
    SettingsLib \
    AicpGear-preference \
    AicpGear-util

LOCAL_STATIC_JAVA_LIBRARIES := \
    particles \
    glide

# Apache http for stats

LOCAL_JAVA_LIBRARIES := org.apache.http.legacy
LOCAL_USES_LIBRARIES := org.apache.http.legacy
LOCAL_USE_AAPT2 := true

LOCAL_PACKAGE_NAME := AicpExtras

LOCAL_PRIVATE_PLATFORM_APIS := true
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_REQUIRED_MODULES := privapp_whitelist_com.aicp.extras-ext.xml

include frameworks/base/packages/SettingsLib/common.mk

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := privapp_whitelist_com.aicp.extras-ext.xml
LOCAL_MODULE_CLASS := ETC
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_PATH := $(TARGET_OUT_SYSTEM_EXT_ETC)/permissions
LOCAL_SYSTEM_EXT_MODULE := true
LOCAL_SRC_FILES := $(LOCAL_MODULE)
include $(BUILD_PREBUILT)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := particles:lib/LeonidsLib-1.3.2.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under, $(LOCAL_PATH))
