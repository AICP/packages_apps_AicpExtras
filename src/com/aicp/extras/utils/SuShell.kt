/*
 * Copyright (C) 2026 AICP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.aicp.extras.utils

import android.util.Log
import java.io.*

class SuShell private constructor() {

    companion object {
        private val TAG = SuShell::class.java.simpleName

        // WARNING: setting this to true will dump passwords to logcat
        // set to false for release
        const val DEBUG = false

        @Throws(SuDeniedException::class)
        @JvmStatic
        fun runWithSuCheck(vararg commands: String): ArrayList<String> {
            val suTestScript = "#!/system/bin/sh\necho "
            val suTestScriptValid = "AICPSuPermsOk"

            // Direkt nicht-nullable Array bauen
            val commandsWithCheck = arrayOf(suTestScript + suTestScriptValid, *commands)

            // Run mit Shell explizit angeben
            val output = run("su", *commandsWithCheck)

            if (output.isNotEmpty() && output[0].trim() == suTestScriptValid) {
                if (DEBUG) Log.d(TAG, "Superuser command auth confirmed")
                output.removeAt(0)
                return output
            } else {
                if (DEBUG) Log.d(TAG, "Superuser command auth refused")
                throw SuDeniedException()
            }
        }

        @JvmStatic
        fun runWithShell(vararg commands: String): ArrayList<String> =
            run("/system/bin/sh", *commands)

        @JvmStatic
        fun runWithSu(vararg commands: String): ArrayList<String> =
            run("su", *commands)

        @JvmStatic
        fun run(shell: String, commands: ArrayList<String>): ArrayList<String> =
            run(shell, *commands.toTypedArray())

        @JvmStatic
        fun run(shell: String, vararg commands: String): ArrayList<String> {
            val output = ArrayList<String>()
            try {
                val process = Runtime.getRuntime().exec(shell)

                BufferedOutputStream(process.outputStream).use { shellInput ->
                    BufferedReader(InputStreamReader(process.inputStream)).use { shellOutput ->
                        for (command in commands) {
                            if (DEBUG) Log.i(TAG, "command: $command")
                            shellInput.write("$command 2>&1\n".toByteArray())
                        }

                        shellInput.write("exit\n".toByteArray())
                        shellInput.flush()

                        var line: String?
                        while (shellOutput.readLine().also { line = it } != null) {
                            line?.let {
                                if (DEBUG) Log.d(TAG, "command output: $it")
                                output.add(it)
                            }
                        }
                    }
                }

                process.waitFor()
            } catch (e: IOException) {
                Log.e(TAG, "Error: ${e.message}", e)
                throw RuntimeException(e)
            } catch (e: InterruptedException) {
                Log.e(TAG, "Error: ${e.message}", e)
                throw RuntimeException(e)
            }

            return output
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getCommandOutput(command: String): String {
            val output = StringBuilder()
            if (DEBUG) Log.d(TAG, "Getting output for command: $command")

            val process = Runtime.getRuntime().exec(command)
            BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
            }
            return output.toString()
        }

        @JvmStatic
        fun detectValidSuInPath(): Boolean {
            val pathToTest = System.getenv("PATH")?.split(":") ?: emptyList()
            for (path in pathToTest) {
                val su = File("$path/su")
                if (su.exists()) {
                    if (DEBUG) Log.d(TAG, "Found su at ${su.absolutePath}")
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun findInPath(cmd: String): Boolean {
            val pathToTest = System.getenv("PATH")?.split(":") ?: emptyList()
            for (path in pathToTest) {
                val cmdFile = File(path, cmd)
                if (cmdFile.exists()) {
                    if (DEBUG) Log.d(TAG, "Found $cmd at ${cmdFile.absolutePath}")
                    return true
                }
            }
            return false
        }
    }

    class SuDeniedException : Exception()
}
