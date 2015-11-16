#
# Copyright (C) 2015 The Pure Nexus Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

LOCAL_PATH:= $(call my-dir)

LOCAL_MODULE_TAGS := optional

#Include res dir from libraries
appcompat_dir := ../../../$(SUPPORT_LIBRARY_ROOT)/v7/appcompat/res
cardview_dir := ../../../$(SUPPORT_LIBRARY_ROOT)/v7/cardview/res
recyclerview_dir := ../../../$(SUPPORT_LIBRARY_ROOT)/v7/recyclerview/res
design_dir := ../../../$(SUPPORT_LIBRARY_ROOT)/design/res

res_dirs := res $(appcompat_dir) $(cardview_dir) $(recyclerview_dir) $(design_dir)


##################################################
# Build APK
include $(CLEAR_VARS)

LOCAL_PACKAGE_NAME := AicpExtras
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true
LOCAL_PROGUARD_ENABLED := disabled

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-cardview
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v13
LOCAL_STATIC_JAVA_LIBRARIES += android-support-design
LOCAL_STATIC_JAVA_LIBRARIES += libsuperuser
LOCAL_STATIC_JAVA_LIBRARIES += org.cyanogenmod.platform.internal
LOCAL_JAVA_LIBRARIES += org.cyanogenmod.hardware

LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(addprefix $(LOCAL_PATH)/, $(res_dirs))
LOCAL_AAPT_FLAGS := --auto-add-overlay
LOCAL_AAPT_FLAGS += --extra-packages android.support.v7.appcompat:android.support.v7.cardview:android.support.v7.recyclerview:android.support.design

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := libsuperuser:lib/libsuperuser.jar

include $(BUILD_MULTI_PREBUILT)

