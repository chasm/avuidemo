package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import com.vaadin._
import com.vaadin.ui._
import com.vaadin.ui.themes.BaseTheme
import com.vaadin.ui.UriFragmentUtility._
import com.vaadin.terminal.ExternalResource

import java.util.Date

class AgentLoginWindow(caption: String) extends Window(caption) {
  def this() {
    this("Agent Services")
  }
  
  private var currentCode: Option[String] = None
  
  setName("agentLogin")
  setScrollable(true)
  
  // Layout for the full window
  val windowLayout = new VerticalLayout()
  windowLayout.setMargin(false)
  windowLayout.setWidth("100%")
  
  // Layout for the centered page at 960px
  val pageLayout = new VerticalLayout()
  pageLayout.setMargin(false, true, true, true)
  pageLayout.setSpacing(true)
  pageLayout.setWidth("960px")
  windowLayout.addComponent(pageLayout)
  windowLayout.setComponentAlignment(pageLayout, Alignment.TOP_CENTER)
  
  // Handle URI fragments to change tabs
  val urifu: UriFragmentUtility = new UriFragmentUtility()
  pageLayout.addComponent(urifu)

  // Build the page
  var page: LoginPage = new LoginPage("00000000-1111-2222-3333-444444444444") // currentCode.getOrElse("") CHANGE THIS!
  pageLayout.addComponent(new Logo())
  pageLayout.addComponent(page)
  pageLayout.addComponent(new Footer())
  
  // Add the layout
  addComponent(windowLayout)
  
  // Listen for fragments and swap pages as necessary
  val uuidRegex = "(?i:[a-f0-9]{8}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{4}\\-[a-f0-9]{12})".r
  urifu.addListener(new FragmentChangedListener() {
    def fragmentChanged(source: UriFragmentUtility#FragmentChangedEvent) {
      val fragment = source.getUriFragmentUtility().getFragment()
      if (fragment != null) {
        if (uuidRegex.findFirstIn(fragment).isDefined) {
          currentCode = Some(fragment)
          val newPage = new LoginPage("00000000-1111-2222-3333-444444444444") // currentCode.getOrElse("") CHANGE THIS!
          pageLayout.replaceComponent(page, newPage)
          page = newPage
          urifu.setFragment(null, true)
        }
      }
    }
  })
  
  // Page layout
  class LoginPage(code: String) extends VerticalLayout {
    setStyleName("centerLayout")
    setWidth("935px")
    setHeight("460px")
    
    val now: Long = new Date().getTime
    var caption: String = "Error"
    val uhoh: Label = new Label("""
      To log in to Agent Services, please use the coded log in URL that was provided to you.
      If you are using the coded URL for the first time, it may have expired. Please contact
      the systems administrator for help.
    """)
    
    val loginForm = ContentUserDAO.get(code) match {
      case Some(u: ContentUser) =>
        val expires = u.getExpires()
        if (expires > now) {
          caption = "Register"
          new RegistrationForm(u)
        } else if (u.getExpires() == 0) {  
          caption = "Log in"
          new AgentLoginForm(code)
        } else {
          uhoh
        }
      case None => uhoh
    }
    
    val loginPanel = new Panel(caption)
    loginPanel.setWidth("380px")
    loginPanel.addComponent(loginForm)

    setMargin(false)
    addComponent(loginPanel)
    setComponentAlignment(loginPanel, Alignment.MIDDLE_CENTER)
  }
  
  // Class for the page heading with logo and logout link
  class Logo extends CssLayout {
    setStyleName("logo")
    setWidth("915px")

    val lbl = new Label(
      "<h1><span id=\"asterisk\">*</span> Agent Services</h1>",
      Label.CONTENT_XHTML
    )
    addComponent(lbl)
  }
  
  // Class for the footer with copyright
  class Footer extends CssLayout {
    setStyleName("footer")
    setWidth("915px")
  
    val lbl: Label = new Label("copyright 2010 by nonebetwixt.net")
    lbl.addStyleName("copyright")
    addComponent(lbl)
  }
}

