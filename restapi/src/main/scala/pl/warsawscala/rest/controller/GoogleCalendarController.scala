package pl.warsawscala.rest.controller

import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Singleton

import com.google.inject.Inject
import com.typesafe.scalalogging.StrictLogging
import org.joda.time.DateTime
import pl.warsawscala.rest.Helpers
import play.api.Configuration
import play.api.mvc.{Action, Controller, EssentialAction}

@Singleton
class GoogleCalendarController @Inject() (config: Configuration) extends Controller with StrictLogging {

  val counter = new AtomicInteger()

  var map = Map[Int, (DateTime, DateTime)]()

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
          val id = state.head.toInt
          NotFound(s"$id")
        case None =>
          //val redirect_uri = domain + s"?start=$startTime&end=$endTime"
          val params = Map(
            RESPONSE_TYPE -> config.getString("restapi.oAuth2.responseType").getOrElse(""),
            CLIENT_ID -> config.getString("restapi.oAuth2.clientId").getOrElse(""),
            NONCE -> config.getString("restapi.oAuth2.nonce").getOrElse(""),
            REDIRECT_URI -> config.getString("restapi.oAuth2.redirectURI").getOrElse(""),
            //REDIRECT_URI    -> redirect_uri,
            SCOPE -> config.getString("restapi.oAuth2.scope").getOrElse(""),
            STATE -> config.getString("restapi.clientName").getOrElse(""),
            PROMPT -> SELECT_ACCOUNT
          )

          val uri = config.getString("restapi.oAuth2.authURL").getOrElse("") + "?" + Helpers.encodeParam(params)
          val state = counter.incrementAndGet();
          map = map + (state ->(startTime, endTime))
          Redirect(uri)
      }
  }

  def callback() = Action { request =>
    request.queryString.get(CODE).getOrElse("") match {
      case Some(seq) => {
        Redirect(routes.GoogleCalendarController.holidays()).withSession {
          CODE -> ???
        }
      }
      case None => Ok("Cant't get authCode")
    }
  }
}
