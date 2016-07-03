package pl.warsawscala.calendar

import java.time.{LocalDate, ZoneId}
import java.text.SimpleDateFormat
import java.util.Date

import org.joda.time.DateTime
import play.api.libs.json.{JsArray, JsValue}

import scala.concurrent.Future
import play.api.libs.ws.WSClient

// import play.api.libs.ws.DefaultWSClientConfig
import play.api.libs.ws.ning.{NingAsyncHttpClientConfigBuilder, NingWSClient, NingWSClientConfig}

import scala.concurrent.Future
import scala.util.{Try, Success, Failure}

case class PlannedEvent(startDate: LocalDate, endDateExclusive: LocalDate, tags: Seq[String])

case class GoogleEvent(startDate: LocalDate, endDate: LocalDate, summary: String)

trait MyCalendar {
  def getEventsFor(from: LocalDate, to: LocalDate): Future[Seq[PlannedEvent]] // ???
}

case class MyCalendarImpl(code: String, client: WSClient) extends MyCalendar {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getEventsFor(from: LocalDate, to: LocalDate): Future[Seq[PlannedEvent]] = {
    for {
      token <- getAuthToken
      events <- getCalendarEntries(token, from, to)
    } yield events
  }

  def getAuthToken: Future[String] = {
    val postData: Map[String, Seq[String]] = Map("code" -> code,
      "client_id" -> "468805955202-t2ahu8an02kmvc13pk15adp8to1nitc0.apps.googleusercontent.com",
      "client_secret" -> "YQig_XAAns1PlEdS7XNXqwB6",
      "redirect_uri" -> "http://localhost:9000/oauth2callback",
      "grant_type" -> "authorization_code") map { case (key, value) => (key, Seq(value)) }


    client.url("https://www.googleapis.com/oauth2/v4/token")
      .withHeaders(("Content-Type", "application/x-www-form-urlencoded"))
      .post(postData) map {
      response => (response.json \ "access_token").as[String]
    }
  }

  def getCalendarEntries(authToken: String, from: LocalDate, to: LocalDate): Future[Seq[PlannedEvent]] = {
    val rfc3999format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    client.url("https://www.googleapis.com/calendar/v3/calendars/primary/events")
      .withQueryString("access_token" -> authToken)
      .withQueryString("timeMin" -> rfc3999format.format(Date.from(from.atStartOfDay(ZoneId.systemDefault()).toInstant)))
      .withQueryString("timeMax" -> rfc3999format.format(Date.from(to.atStartOfDay(ZoneId.systemDefault()).toInstant)))
      .get() map {
      response =>
        val items = (response.json \ "items").as[List[JsValue]] filter { item =>
          Try(DateTime.parse((item \ "start" \ "date").as[String])) match {
            case Success(v) =>
              true
            case Failure(e) =>
              false
          }
        } map { item =>
          val startDate = DateTime.parse((item \ "start" \ "date").as[String])
          val endDate = DateTime.parse((item \ "end" \ "date").as[String])
          val summary = (item \ "summary").as[String]
          GoogleEvent(startDate.toDate.toInstant.atZone(ZoneId.systemDefault()).toLocalDate, endDate.toDate.toInstant.atZone(ZoneId.systemDefault()).toLocalDate, summary)
        }
        ParseEvents(items)
    }
  }

  def ParseEvents(EventsList: List[GoogleEvent]): List[PlannedEvent] = {
    EventsList.map(e => PlannedEvent(e.startDate, e.endDate, ParseTags(e.summary)))
  }

  def ParseTags(summary: String): List[String] = {
    summary.split("#").zipWithIndex.filter(_._2 % 2 == 1).map(_._1).toList
  }
}

case class MyCalendarStub() extends MyCalendar {

  import scala.concurrent.ExecutionContext.Implicits.global

  def getEventsFor(from: LocalDate, to: LocalDate): Future[Seq[PlannedEvent]] = {
    Future {
      val myPlannedEvent: PlannedEvent = PlannedEvent(LocalDate.now(), LocalDate.now().plusDays(1), List("URLAUB", "icecreamdays"))
      val myPlannedEvent2: PlannedEvent = PlannedEvent(LocalDate.now(), LocalDate.now().plusDays(1), List("grilldays"))
      val myPlannedEvent3: PlannedEvent = PlannedEvent(LocalDate.now(), LocalDate.now().plusDays(2), List("URLAUB"))
      List(myPlannedEvent, myPlannedEvent2, myPlannedEvent3)
    }
  }
}

object MyCalendar {
  def apply(code: String, client: WSClient): MyCalendar = MyCalendarImpl(code, client)
}

