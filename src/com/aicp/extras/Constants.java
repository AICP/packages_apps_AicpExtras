/*
 * Copyright (C) 2017 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.aicp.extras;

public abstract class Constants {
    /**
     * Package name of AICP OTA
     */
    public static final String AICP_OTA_PACKAGE = "com.aicp.aicpota";

    /**
     * Default activity of AICP OTA
     */
    public static final String AICP_OTA_ACTIVITY = AICP_OTA_PACKAGE + ".MainActivity";

    /**
     * Key for SharedPreferences for selinux switch
     */
    public static final String PREF_SELINUX_MODE = "selinux_mode";

    /**
     * Key for SharedPreferences for selinux switch persistence
     */
    public static final String PREF_SELINUX_PERSISTENCE = "selinux_persistence";

    /**
     * Package name of weather service
     */
    private static final String WEATHER_SERVICE_PACKAGE = "org.omnirom.omnijaws";
}
