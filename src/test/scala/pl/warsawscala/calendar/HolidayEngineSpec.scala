package pl.warsawscala.calendar

import scala.concurrent.Await
import scala.concurrent.duration._
import org.specs2.mock.Mockito
import org.specs2.mutable.Specification
import org.mockito.Mockito._
import org.mockito.Matchers._

class HolidayEngineSpec extends Specification with Mockito {
  "Holiday engine" should {
    "return 0 if no holidays have been used" in {
      val mockCalendar = mock[MyCalendar]
      val holidayEngine = new HolidayEngineImpl(mockCalendar)
      Await.result(holidayEngine.countHolidays(), Duration.Inf) === 0
    }
  }
}
