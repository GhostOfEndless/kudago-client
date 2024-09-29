package org.example.logging.utils

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.pattern.color.ANSIConstants
import ch.qos.logback.core.pattern.color.ForegroundCompositeConverterBase

class LogbackHighlightCompositeConverterEx : ForegroundCompositeConverterBase<ILoggingEvent>() {

    override fun getForegroundColorCode(event: ILoggingEvent): String {
        val level = event.level
        return when (level.toInt()) {
            Level.ERROR_INT -> ANSIConstants.BOLD + ANSIConstants.RED_FG
            Level.WARN_INT -> ANSIConstants.BOLD + ANSIConstants.YELLOW_FG
            Level.INFO_INT -> ANSIConstants.BOLD + ANSIConstants.CYAN_FG
            Level.DEBUG_INT -> ANSIConstants.BOLD + ANSIConstants.GREEN_FG
            else -> ANSIConstants.BOLD + ANSIConstants.DEFAULT_FG
        }
    }
}