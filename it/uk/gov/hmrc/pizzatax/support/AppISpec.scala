package uk.gov.hmrc.pizzatax.support

import org.scalatestplus.play.guice.GuiceOneAppPerSuite

abstract class AppISpec extends BaseISpec with GuiceOneAppPerSuite {
  override def commonStubs(): Unit = {}
}
