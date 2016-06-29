package pl.warsawscala.rest.controller

import java.util.Date
import javax.inject.Singleton

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import pl.warsawscala.rest.Helpers
import play.api.libs.ws._
import play.api.Configuration
import play.api.mvc.{Action, Controller, EssentialAction}
import pl.warsawscala.calendar
import pl.warsawscala.calendar.MyCalendar

import scala.util.Random

@Singleton
class GoogleCalendarController @Inject() (config: Configuration,
                                          ws: WSClient) extends Controller with StrictLogging {

  var stateMap = Map[String, (DateTime, DateTime)]()

  val START = "start"
  val END = "end"
  val CODE = "code"
  val STATE = "state"
  val PROMPT = "prompt"
  val SCOPE = "scope"
  val RESPONSE_TYPE = "response_type"
  val CLIENT_ID = "client_id"
  val NONCE = "nonce"
  val REDIRECT_URI = "redirect_uri"
  val SELECT_ACCOUNT = "select_account"

  def holidays(): EssentialAction = Action {
    r =>
      val startTime = r.queryString.get(START).map(str => DateTime.parse(str.head)).getOrElse(DateTime.now().withMonthOfYear(1).withTimeAtStartOfDay().withDayOfMonth(1))
      val endTime = r.queryString.get(END).map(str => DateTime.parse(str.head)).getOrElse(DateTime.now().withMonthOfYear(12).withTimeAtStartOfDay().withDayOfMonth(31))
      logger.debug(s"$startTime $endTime")
      r.queryString.get(STATE) match {
        case Some(state) =>
          NotFound(s"$state")
        case None =>
          val tempState = Random.nextString(6)
          stateMap = stateMap + (tempState -> (startTime, endTime))

          val params = Map(
            RESPONSE_TYPE -> config.getString("restapi.oAuth2.responseType").getOrElse(""),
            CLIENT_ID -> config.getString("restapi.oAuth2.clientId").getOrElse(""),
            NONCE -> config.getString("restapi.oAuth2.nonce").getOrElse(""),
            REDIRECT_URI -> config.getString("restapi.oAuth2.redirectURI").getOrElse(""),
            SCOPE -> config.getString("restapi.oAuth2.scope").getOrElse(""),
            STATE -> tempState,
            PROMPT -> SELECT_ACCOUNT
          )

          val uri = config.getString("restapi.oAuth2.authURL").getOrElse("") + "?" + Helpers.encodeParam(params)
          Redirect(uri)
      }
  }

  def callback() = Action { request =>
    request.queryString.get(CODE) match {
      case Some(code) => {
        val state = request.queryString.get(STATE).get.head
        val timeBounds = stateMap.getOrElse(state, (DateTime.now(), DateTime.now()))
        val curCalendar = MyCalendar.apply(code.head)
        val start: Date = timeBounds._1.toDate()
        curCalendar.getEventsFor(timeBounds._1, timeBounds._2)
        ???
      }
      case None => Ok("Cant't get authCode")
    }
  }
}
