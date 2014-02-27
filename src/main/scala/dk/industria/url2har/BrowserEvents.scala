package dk.industria.url2har

import org.openqa.selenium.WebDriver


import org.openqa.selenium.support.events.{AbstractWebDriverEventListener, WebDriverEventListener}


class BrowserEvents extends AbstractWebDriverEventListener {


  override def afterNavigateTo(url: String, driver: WebDriver) = {

    Console.println("After navigate to " + url);

  }


  override def beforeNavigateTo(url: String, driver: WebDriver) = {

    Console.println("Before navigate to " + url);

  }

}
