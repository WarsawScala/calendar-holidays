package pl.warsawscala.calendar

import java.time.LocalDate

import scala.concurrent._
import java.time.temporal.ChronoUnit.DAYS

trait HolidayEngine {
  /**
    *
    * @param from opcjonalna data od (jeżeli brak liczymy od początku bieżącego roku)
    * @param to opcjonalna data do (jęzeli brak liczymy do końca bieżącego roku)
    * @return liczbę zaplanowanych dni urlopowych
    */
  def countHolidays(from: LocalDate, to: LocalDate): Future[Int]

  /**
    *
    * @param year rok dla którego liczymy pozostałe dni wolne (jeżeli brak liczymy dla bieżącego roku)
    * @return liczba pozostałych dni urlopowych
    */
  def countHolidaysLeftInYear(year: Int ): Future[Int]
}

object HolidayEngineImpl {
  val HOLIDAY_TAG = "URLAUB"
}

class HolidayEngineImpl(myCalendar: MyCalendar) extends HolidayEngine {
  import HolidayEngineImpl._
  /**
    *
    * @param from opcjonalna data od (jeżeli brak liczymy od początku bieżącego roku)
    * @param to   opcjonalna data do (jęzeli brak liczymy do końca bieżącego roku)
    * @return liczbę zaplanowanych dni urlopowych
    */
  override def countHolidays(from: LocalDate, to: LocalDate): Future[Int] = myCalendar.getEventsFor(from, to).map{ seg =>
    seg.filter( _.tags.contains(HOLIDAY_TAG))
        .map( event => DAYS.between(event.startDate, event.endDateExclusive)) // check if -1 is needed
      .sum.toInt
  }

  /**
    *
    * @param year rok dla którego liczymy pozostałe dni wolne (jeżeli brak liczymy dla bieżącego roku)
    * @return liczba pozostałych dni urlopowych
    */
  override def countHolidaysLeftInYear(year: Int): Future[Int] = {
    val from = new LocalDate(year, 1, 1)
    val to = new LocalDate(year, 12, 31)
    countHolidays(from, to)
  }
}
