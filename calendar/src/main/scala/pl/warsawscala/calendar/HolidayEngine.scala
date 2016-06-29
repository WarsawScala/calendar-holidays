package pl.warsawscala.calendar

import java.time.LocalDate

import scala.concurrent._
import java.time.temporal.ChronoUnit.DAYS

trait HolidayEngine {
  /**
    *
    * @return liczbę zaplanowanych dni urlopowych
    */
  def countHolidays(from: LocalDate, to: LocalDate): Future[Int]

  /**
    * @param year Dla pondanego roku oblicz dni urlopowe. Domyślnie liczy dla aktualnego roku.
    * @return liczba pozostałych dni urlopowych
    */
  def countHolidaysLeftInYear(year: Int = LocalDate.now().getYear): Future[Int]
}

object HolidayEngineImpl {
  val HOLIDAY_TAG = "URLAUB"
}

class HolidayEngineImpl(myCalendar: MyCalendar) extends HolidayEngine {
  import HolidayEngineImpl._
  import ExecutionContext.Implicits.global

  override def countHolidays(from: LocalDate, to: LocalDate): Future[Int] = myCalendar.getEventsFor(from, to).map{ seg =>
    seg.filter( _.tags.contains(HOLIDAY_TAG))
        .map( event => DAYS.between(event.startDate, event.endDateExclusive)) // counts length of event
      .sum.toInt
  }

  override def countHolidaysLeftInYear(year: Int): Future[Int] = {
    val from = LocalDate.of(year, 1, 1)
    val to = LocalDate.of(year, 12, 31)
    countHolidays(from, to)
  }
}
