package io.u11.alerts2discord.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun logger() : Logger {
    return logger(StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).callerClass)
}

fun logger(name: String) : Logger {
    return LoggerFactory.getLogger(name)
}

fun logger(clazz: Class<*>) : Logger {
    return LoggerFactory.getLogger(clazz)
}

