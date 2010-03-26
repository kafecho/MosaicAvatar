package org.kafecho.mosaicavatar

import org.apache.commons.logging.{LogFactory, Log}

/**
 * A utility class that measures and logs the execution time of a function passed as a parameter.
 * The function may return an object of type T or may return nothing ( a Unit )
 */

object TimeUtil{

  val log : Log = LogFactory.getLog(getClass)

  def timeThis[T] ( description : String)( f: => T) : T = timeThis(log,description)(f)

  def timeThis[T] ( aLog : Log, description : String)( f: => T) : T = {
    if (aLog.isInfoEnabled) aLog.info("Executing " + description + " ....")

    val start = System.currentTimeMillis
    val output : T = f
    val stop  = System.currentTimeMillis

    if (aLog.isInfoEnabled) aLog.info("It took "  + (stop - start) + " msecs to execute " + description )

    return output
  }
}