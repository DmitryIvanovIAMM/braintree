package utils

import play.api.Logger

trait Logging {

  private val log = Logger(getClass.getName.stripSuffix("$"))

  protected final def debug(msg: => String): Unit = if (log.isDebugEnabled) log.debug(msg)
  protected final def info(msg: => String): Unit = if (log.isInfoEnabled) log.info(msg)
  protected final def warn(msg: => String): Unit = if (log.isWarnEnabled) log.warn(msg)
  protected final def error(msg: => String): Unit = if (log.isErrorEnabled) log.error(msg)
  protected final def error(msg: => String, ex: => Throwable): Unit = if (log.isErrorEnabled) log.error(msg, ex)

}
