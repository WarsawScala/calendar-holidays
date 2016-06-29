package pl.warsawscala.calendar

import java.time.LocalDate

import scala.concurrent._
import ExecutionContext.Implicits.global

trait HolidayEngine {
  /**
    *
    * @param from opcjonalna data od (jeżeli brak liczymy od początku bieżącego roku)
    * @param to opcjonalna data do (jęzeli brak liczymy do końca bieżącego roku)
    * @return liczbę zaplanowanych dni urlopowych
    */
  def countHolidays(from: Option[LocalDate] = None, to: Option[LocalDate] = None): Future[Int]

  /**
    *
    * @param year rok dla którego liczymy pozostałe dni wolne (jeżeli brak liczymy dla bieżącego roku)
    * @return liczba pozostałych dni urlopowych
    */
  def countHolidaysLeftInYear(year: Option[Int] = None): Future[Int]
}

class HolidayEngineImpl(myCalendar: MyCalendar) extends HolidayEngine {
  override def countHolidays(from: Option[LocalDate] = None, to: Option[LocalDate] = None): Future[Int] =
    Future(0)
  override def countHolidaysLeftInYear(year: Option[Int] = None): Future[Int] = ???
}
