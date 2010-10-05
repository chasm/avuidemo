package com.nonebetwixt.agent.ui

import com.vaadin._
import com.vaadin.ui._
import com.vaadin.ui.themes.BaseTheme
import com.vaadin.ui.TabSheet._
import com.vaadin.ui.UriFragmentUtility._

class AgentWindow(caption: String) extends Window(caption) {
  def this() {
    this("Agent Services")
  }
  
  setName("agentServices")
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

  // Tabsheet and listener for tab changes
  val tabsheet = new MainTabs(urifu)

  tabsheet.addListener(new TabSheet.SelectedTabChangeListener() {
    def selectedTabChange(event: TabSheet#SelectedTabChangeEvent) {
      urifu.setFragment(event.getTabSheet().getSelectedTab().asInstanceOf[Fragmented].getFragment())
    }
  })
  
  // Build the page
  pageLayout.addComponent(new LogoWithLogout())
  pageLayout.addComponent(tabsheet)
  pageLayout.addComponent(new Footer())
  
  // Add the layout
  addComponent(windowLayout)
  
  class MainTabs(urifu: UriFragmentUtility) extends TabSheet {
    setWidth("935px")

    // Create the tab components
    val invitationManager = new InvitationManager()
    val introductionManager = new IntroductionManager()
    val connectionManager = new ConnectionManager()
    val contentManager = new ContentManager()
    val copManager = new CopManager()
    val accountManager = new AccountManager()

    // Map of the tabs by fragment name
    var tabs: Map[String,Component] = Map(
      invitationManager.getFragment() -> invitationManager,
      introductionManager.getFragment() -> introductionManager,
      connectionManager.getFragment() -> connectionManager,
      contentManager.getFragment() -> contentManager,
      copManager.getFragment() -> copManager,
      accountManager.getFragment() -> accountManager
    )

    // Add the tabs to the tabsheet
    addTab(connectionManager, "Connections", null)
    addTab(contentManager, "Content", null)
    addTab(copManager, "Communities", null)
    addTab(introductionManager, "Introductions", null)
    addTab(invitationManager, "Invitations", null).setVisible(AgentServices.getInstance().isSuperuser())
    addTab(accountManager, "Account", null)
  
    // Listen for fragments and swap pages as necessary
    urifu.addListener(new FragmentChangedListener() {
      def fragmentChanged(source: UriFragmentUtility#FragmentChangedEvent) {
        val fragment = source.getUriFragmentUtility().getFragment()
        println("Found a fragment: " + fragment)
        if (fragment != null) {
          if (fragment == "logout") {
            AgentServices.getInstance().logOut()
          } else if (tabsheet != null && tabs.contains(fragment)) {
            tabsheet.setSelectedTab(tabs(fragment))
          }
        }
      }
    })
  }
  
  // Class for the page heading with logo and logout link
  class LogoWithLogout extends CssLayout with Button.ClickListener {
    setWidth("915px")
    setStyleName("logo")

    val btn: Button = new Button("log out")
    btn.addStyleName(BaseTheme.BUTTON_LINK)
    btn.addStyleName("logout")
    btn.addListener(this)
    addComponent(btn)

    def buttonClick(event: Button#ClickEvent) {
      if (AgentServices.getInstance().isLoggedIn) {
        AgentServices.getInstance().logOut()
      }
    }

    val lbl = new Label(
      "<h1><span id=\"asterisk\">*</span> " + AgentServices.getInstance().getTitle() + "</h1>",
      Label.CONTENT_XHTML
    )
    addComponent(lbl)
  }
  
  // Class for the footer with copyright and loadTestData button
  class Footer extends CssLayout with Button.ClickListener {
    setWidth("915px")
    setStyleName("footer")
  
    val btn: Button = new Button("Copyright 2010 by nonebetwixt.net")
    btn.addStyleName(BaseTheme.BUTTON_LINK)
    btn.addStyleName("testdata")
    btn.addListener(this)
    addComponent(btn)
  
    def buttonClick(event: Button#ClickEvent) {
      TestData.loadTestData(AgentServices.suId)
    }
  }
}





