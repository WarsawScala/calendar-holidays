package pl.warsawscala.rest.controller

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import play.api.Configuration
import play.api.mvc.{Action, Controller, EssentialAction}

@Singleton
class GoogleCalendarController @Inject() (config: Configuration) extends Controller with StrictLogging {

  val counter = new AtomicInteger()

  var map = Map[Int, (DateTime, DateTime)]()


  val scope = config.getString("restapi.config.scope").getOrElse("")
  val clientId = config.getString("restapi.config.clientId").getOrElse("")
  val domain = config.getString("restapi.config.domain").getOrElse("")
  val START = "start"
  val END = "end"
  val CODE = "code"
  val STATE = "state"

  def holidays(): EssentialAction = Action {
    r =>
      val startTime = r.queryString.get(START).map(str => DateTime.parse(str.head)).getOrElse(DateTime.now().withMonthOfYear(1).withTimeAtStartOfDay().withDayOfMonth(1))
      val endTime = r.queryString.get(END).map(str => DateTime.parse(str.head)).getOrElse(DateTime.now().withMonthOfYear(12).withTimeAtStartOfDay().withDayOfMonth(31))
      logger.debug(s"$startTime $endTime")
      r.queryString.get(STATE) match {
        case Some(state) =>
          val id = state.head.toInt
          NotFound(s"$id")
        case None =>
          val state = counter.incrementAndGet();
          val redirect_uri = domain + s"?start=$startTime&end=$endTime"
          map = map + (state -> (startTime,endTime))
//          Ok(s"$startTime $endTime")

          val uri = s"https://accounts.google.com/o/oauth2/v2/auth?scope=$scope&state=$state&redirect_uri=$redirect_uri%2Fcode&,response_type=code&client_id=$clientId"
          Redirect(uri)
      }


  }

}
