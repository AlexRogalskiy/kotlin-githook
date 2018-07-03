#!/usr/bin/env kscript

import Pre_push.Constants.ERROR_EXIT_VALUE
import Pre_push.Constants.NOTHING_TO_STASH_MSG
import Pre_push.Constants.SCRIPT_LOG_TAG
import Pre_push.Constants.SUCCESS_EXIT_VALUE
import Pre_push.ExitStatus
import java.io.File

object Constants{
    const val SCRIPT_LOG_TAG = "Pre-push -"
    const val NOTHING_TO_STASH_MSG = "No local changes to save"
    const val SUCCESS_EXIT_VALUE = 0
    const val ERROR_EXIT_VALUE = -1
}

println("$SCRIPT_LOG_TAG Running oic-form-service pre-push hook")
val hasStashed = stash()
if (hasStashed) {
    println("$SCRIPT_LOG_TAG Stashing uncommited changes")
}

val checkExistStatus = runCheck()

if (hasStashed) {
    println("$SCRIPT_LOG_TAG Unstashing your changes")
    unstash()
}

val exitValue = when {
    checkExistStatus != SUCCESS_EXIT_VALUE -> {
        println("$SCRIPT_LOG_TAG Gradle check failed. I'm sorry but you can't continue with your push")
        ERROR_EXIT_VALUE
    }
    else -> {
        println("$SCRIPT_LOG_TAG Something exploded! I'm sorry but you can't continue with your push")
        ERROR_EXIT_VALUE
    }
}

kotlin.system.exitProcess(exitValue)

fun runCheck(): ExitStatus {
    println("$SCRIPT_LOG_TAG Running gradle check")
    return "gradle check".runCommandWithRedirect()
}

fun stash(): Boolean {
    val stashOutput = """git stash push --include-untracked -m "stash created by pre-push hook"""".runCommand()
    return stashOutput.firstOrNull() != NOTHING_TO_STASH_MSG
}

fun unstash() = "git stash pop -q".runCommand()

fun String.runCommand(dir: File? = null): Sequence<String> =
    ProcessBuilder("/bin/sh", "-c", this)
        .redirectErrorStream(true)
        .directory(dir)
        .start()
        .inputStream.bufferedReader().lineSequence()

// Redirecting output and error to stdout
fun String.runCommandWithRedirect(dir: File? = null): ExitStatus =
    ProcessBuilder("/bin/sh", "-c", this)
        .redirectErrorStream(true)
        .inheritIO()
        .directory(dir)
        .start()
        .waitFor()

typealias ExitStatus = Int