package com.nonebetwixt.agent.ui

import com.nonebetwixt.agent.model._

import scala.collection.immutable.ListSet

import com.vaadin.ui._

import com.sleepycat.je.{DatabaseException, Environment, EnvironmentConfig}
import com.sleepycat.persist.{EntityStore, StoreConfig}

import java.util.{UUID, Date, Random}

object TestData {
  
  val rand = new Random
  var notloaded = true
  
  def loadTestData(suId: String) {
    if (notloaded) {
      val su = ContentUserDAO.get(suId).getOrElse(null)
      
      // ContentUsers
      val u1 = new ContentUser(UUID.randomUUID.toString, "Abraham", "Lincoln", "abe@munat.com", "yellow")
      val u2 = new ContentUser(UUID.randomUUID.toString, "Teddy", "Roosevelt", "tedr@munat.com", "chartreuse")
      val u3 = new ContentUser(UUID.randomUUID.toString, "Franklin", "Roosevelt", "franklin@munat.com", "aquamarine")
      val u4 = new ContentUser(UUID.randomUUID.toString, "Jack", "Kennedy", "jackk@munat.com", "maroon")
      val u5 = new ContentUser(UUID.randomUUID.toString, "Barack", "Obama", "barry@munat.com", "violet")
      val users: List[ContentUser] = List(u1, u2, u3, u4, u5)
      println("\nUSERS:")
      users.map(u => {
        println(u.getName() + ": " + u.getId())
      })
      println("")
      ContentUserDAO.put(users)
    
      // ContentTags
      val pa_tag = new ContentTag("Public/Anyone", "PA")
      val an_tag = new ContentTag("Anonymous", "An")
      val aq_tag = new ContentTag("Acquaintance", "Aq")
      val fr_tag = new ContentTag("Friend", "Fr")
      val cf_tag = new ContentTag("Close Friend", "CF")
      val bf_tag = new ContentTag("Best Friend", "BF")
      val co_tag = new ContentTag("Contact", "Co")
      val as_tag = new ContentTag("Associate", "As")
      val tc_tag = new ContentTag("Trusted Colleague", "TC")
      val mr_tag = new ContentTag("Mentor", "Mr")
      val me_tag = new ContentTag("Mentee", "Me")
      val ef_tag = new ContentTag("Extended Family", "EF")
      val if_tag = new ContentTag("Immediate Family", "IF")
      val cc_tag = new ContentTag("Chess Club", "CC")
      val tags: List[ContentTag] = List(
        pa_tag, an_tag, aq_tag, fr_tag, cf_tag, bf_tag, co_tag, as_tag, tc_tag, mr_tag, me_tag, ef_tag, if_tag, cc_tag
      )
      ContentTagDAO.put(tags)
    
      // InternetCnxns
      val su_face = new InternetCnxn(su.getId(), "Facebook", "http://facebook.com/", "last_crusader", "secret")
      val su_link = new InternetCnxn(su.getId(), "LinkedIn", "http://linkedin.com/", "losttemple", "secret")
      val su_none = new InternetCnxn(su.getId(), "EACoP", "http://nonebetwixt.com/", "treasure_hunter", "secret")
      val u1_face = new InternetCnxn(u1.getId(), "Facebook", "http://facebook.com/", "honest_abe", "secret")
      val u1_none = new InternetCnxn(u1.getId(), "EACoP", "http://nonebetwixt.com/", "tallman", "secret")
      val u2_face = new InternetCnxn(u2.getId(), "Facebook", "http://facebook.com/", "big_stick", "secret")
      val u2_none = new InternetCnxn(u2.getId(), "EACoP", "http://nonebetwixt.com/", "old_grizzly", "secret")
      val u2_link = new InternetCnxn(u2.getId(), "LinkedIn", "http://linkedin.com/", "rough_rider", "secret")
      val u3_face = new InternetCnxn(u3.getId(), "Facebook", "http://facebook.com/", "martini", "secret")
      val u3_none = new InternetCnxn(u3.getId(), "EACoP", "http://nonebetwixt.com/", "secret_chair", "secret")
      val u4_face = new InternetCnxn(u4.getId(), "Facebook", "http://facebook.com/", "marilyns", "secret")
      val u4_none = new InternetCnxn(u4.getId(), "EACoP", "http://nonebetwixt.com/", "back_pain", "secret")
      val u5_link = new InternetCnxn(u5.getId(), "LinkedIn", "http://linkedin.com/", "muslim_boy", "secret")
      val u5_none = new InternetCnxn(u5.getId(), "EACoP", "http://nonebetwixt.com/", "hope_for_change", "secret")
      val icnxns: List[InternetCnxn] = List(
        su_face, su_link, su_none, u1_face, u1_none, u2_face, u2_link, u3_face, u3_none, u4_face, u4_none, u5_link, u5_none
      )
      InternetCnxnDAO.put(icnxns)
    
      // CnxnTags
      CnxnTagDAO.put(icnxns.map(c => {
        new CnxnTag(c.getId(), an_tag.getName())
      }))
    
      // AgentCnxns
      val acnxns: List[AgentCnxn] = List(
        new AgentCnxn(su.getId(), u1.getId(), fr_tag.getName()),
        new AgentCnxn(su.getId(), u2.getId(), fr_tag.getName()),
        new AgentCnxn(su.getId(), u3.getId(), cf_tag.getName()),
        new AgentCnxn(su.getId(), u4.getId(), bf_tag.getName()),
        new AgentCnxn(su.getId(), u5.getId(), fr_tag.getName()),
        new AgentCnxn(su.getId(), u4.getId(), tc_tag.getName()),
        new AgentCnxn(su.getId(), u5.getId(), as_tag.getName()),
        new AgentCnxn(u1.getId(), u2.getId(), as_tag.getName()),
        new AgentCnxn(u1.getId(), u3.getId(), as_tag.getName()),
        new AgentCnxn(u1.getId(), u5.getId(), as_tag.getName()),
        new AgentCnxn(u1.getId(), u5.getId(), cc_tag.getName()),
        new AgentCnxn(u2.getId(), u3.getId(), ef_tag.getName()),
        new AgentCnxn(u2.getId(), u5.getId(), as_tag.getName()),
        new AgentCnxn(u3.getId(), u5.getId(), as_tag.getName()),
        new AgentCnxn(u4.getId(), u5.getId(), mr_tag.getName()),
        new AgentCnxn(u5.getId(), u4.getId(), me_tag.getName())
      )
      AgentCnxnDAO.put(acnxns)
    
      // AgentMessages
      val m1 = new AgentMessage(su.getId(), u2.getId(), null, "Hey, what's up?")
      val m2 = new AgentMessage(u2.getId(), su.getId(), m1.getId(), "Not much, how about you?")
      val m3 = new AgentMessage(su.getId(), u2.getId(), m2.getId(), "Just hanging around.")
      val m4 = new AgentMessage(u2.getId(), su.getId(), m3.getId(), "You want a beer?")
      val m5 = new AgentMessage(su.getId(), u2.getId(), m4.getId(), "Sure.")
      val m6 = new AgentMessage(u2.getId(), su.getId(), m5.getId(), "OK, here ya go.")
      val m7 = new AgentMessage(su.getId(), u2.getId(), m6.getId(), "Hey, thanks, man.")
      val mssgs = List(m1, m2, m3, m4, m5, m6, m7)
      AgentMessageDAO.put(mssgs)
    
      // ContentItems
      (su :: users).map(u => {
        val pInfo = new ContentItem(u.getId(), null, "Personal Info", "", "Label", "", 0)
        val pInfo_pa = new ItemTag(pInfo.getId(), pa_tag.getName())
      
        val name = new ContentItem(u.getId(), pInfo.getId(), "Name", "", "Label", "", 0)
        val name_pa = new ItemTag(name.getId(), pa_tag.getName())
      
        val pts = new ContentItem(u.getId(), name.getId(), "Parts", "", "Label", "", 0)
        val pts_pa = new ItemTag(pts.getId(), pa_tag.getName())
      
        val hon = new ContentItem(u.getId(), pts.getId(), "Honorific", "", "String", "", 0)
        val hon_pa = new ItemTag(hon.getId(), pa_tag.getName())
      
        val given = new ContentItem(u.getId(), pts.getId(), "Given", "", "String", "", 1)
        val given_co = new ItemTag(given.getId(), co_tag.getName())
        val given_aq = new ItemTag(given.getId(), aq_tag.getName())
      
        val mid = new ContentItem(u.getId(), pts.getId(), "Middle", "", "String", "", 2)
        val mid_tc = new ItemTag(mid.getId(), tc_tag.getName())
      
        val surn = new ContentItem(u.getId(), pts.getId(), "Surname", "", "String", "", 3)
        val surn_co = new ItemTag(surn.getId(), co_tag.getName())
      
        val suf = new ContentItem(u.getId(), pts.getId(), "Suffix", "", "String", "", 3)
        val suf_pa = new ItemTag(suf.getId(), pa_tag.getName())
      
        val leg = new ContentItem(u.getId(), name.getId(), "Legal Name", "", "String", "", 1)
        val leg_pa = new ItemTag(leg.getId(), pa_tag.getName())
      
        val fn = new ContentItem(u.getId(), name.getId(), "Familiar Name", "", "String", "", 2)
        val fn_as = new ItemTag(fn.getId(), as_tag.getName())
      
        val nn = new ContentItem(u.getId(), name.getId(), "Nickname", "", "String", "", 3)
        val nn_tc = new ItemTag(nn.getId(), tc_tag.getName())
      
        val loc = new ContentItem(u.getId(), pInfo.getId(), "Location", "", "Label", "", 1)
        val loc_pa = new ItemTag(loc.getId(), pa_tag.getName())
      
        val curmet = new ContentItem(u.getId(), loc.getId(), "Current Metro Area", "", "String", "", 0)
        val curmet_co = new ItemTag(curmet.getId(), co_tag.getName())
      
        val curloc = new ContentItem(u.getId(), loc.getId(), "Current Location", "", "String", "", 1)
        val curloc_tc = new ItemTag(curloc.getId(), tc_tag.getName())
      
        val curadd = new ContentItem(u.getId(), loc.getId(), "Current Address", "", "Label", "", 2)
        val curadd_as = new ItemTag(curadd.getId(), as_tag.getName())
      
        val strnum = new ContentItem(u.getId(), curadd.getId(), "Street Number", "", "String", "", 0)
        val strnum_as = new ItemTag(strnum.getId(), as_tag.getName())
      
        val strnam = new ContentItem(u.getId(), curadd.getId(), "Street Name", "", "String", "", 1)
        val strnam_as = new ItemTag(strnam.getId(), as_tag.getName())
      
        val strtyp = new ContentItem(u.getId(), curadd.getId(), "Street Type", "", "String", "", 2)
        val strtyp_as = new ItemTag(strtyp.getId(), as_tag.getName())
      
        val cit = new ContentItem(u.getId(), curadd.getId(), "City", "", "String", "", 3)
        val cit_as = new ItemTag(cit.getId(), as_tag.getName())
      
        val stat = new ContentItem(u.getId(), curadd.getId(), "State", "", "String", "", 4)
        val stat_as = new ItemTag(stat.getId(), as_tag.getName())
      
        val zip = new ContentItem(u.getId(), curadd.getId(), "Zip Code", "", "String", "", 5)
        val zip_as = new ItemTag(zip.getId(), as_tag.getName())
      
        val pl4 = new ContentItem(u.getId(), curadd.getId(), "Plus4", "", "String", "", 6)
        val pl4_as = new ItemTag(pl4.getId(), as_tag.getName())
      
        val cntry = new ContentItem(u.getId(), curadd.getId(), "Country", "US", "String", "", 7)
        val cntry_pa = new ItemTag(cntry.getId(), pa_tag.getName())
        
        val cItems = List(
          pInfo, name, pts, hon, given, mid, surn, suf, leg, fn, nn, loc,
          curmet, curloc, curadd, strnum, strnam, strtyp, cit, stat, zip, pl4, cntry
        )
        
        val iTags = List(
          name_pa, pts_pa, hon_pa, given_co, given_aq, mid_tc, surn_co, suf_pa,
          leg_pa, fn_as, nn_tc, loc_pa, curmet_co, curloc_tc, curadd_as, strnum_as,
          strnam_as, strtyp_as, cit_as, stat_as, zip_as, pl4_as, cntry_pa
        )
        
        ContentItemDAO.put(cItems)
        ItemTagDAO.put(iTags)
      })
      
      // Cops
      val cop = new Cop(su.getId(), "EACoP", "Early Adopters Community of Practice")
      val cop_yes = new Cop(su.getId(), "Another CoP", "This CoP has no fora")
      val cop_no = new Cop(su.getId(), "Inactive CoP", "This CoP is inactive")
      cop_no.setIsActive(false)
      CopDAO.put(List(cop, cop_yes, cop_no))
    
      // Fora
      val f1 = new Forum(cop.getId(), su.getId(), "NoneBetwixt Help", "How to do what you want to do", ListSet.empty)
      val f2 = new Forum(cop.getId(), su.getId(), "NoneBetwixt Feature Requests", "What can Agent Services do for you?", ListSet.empty)
      val f3 = new Forum(cop.getId(), su.getId(), "NoneBetwixt Bug Reports", "What went wrong", ListSet.empty)
      val f_no = new Forum(cop.getId(), su.getId(), "Inactive Forum", "This Forum is inactive", ListSet.empty)
      f_no.setIsActive(false)
      val fora = List(f1, f2, f3, f_no)
      ForumDAO.put(fora)
    
      // Members
      val msu = new Member(cop.getId(), su.getId(), "treasure_hunter")
      val cm1 = new Member(cop.getId(), u1.getId(), "tallman")
      val cm2 = new Member(cop.getId(), u2.getId(), "old_grizzly")
      val cm3 = new Member(cop.getId(), u3.getId(), "secret_chair")
      val cm4 = new Member(cop.getId(), u4.getId(), "back_pain")
      val cm5 = new Member(cop.getId(), u5.getId(), "hope_for_change")
      val cm5_no = new Member(cop_no.getId(), u5.getId(), "inactive_member")
      cm5_no.setIsActive(false)
      val mbrs = List(msu, cm1, cm2, cm3, cm4, cm5, cm5_no)
      MemberDAO.put(mbrs)
    
      // Posts
      val f1t1 = new Post(cop.getId(), msu.getId(), f1.getId(), null, getSubject(), getLorem())
      val f1t1r1 = new Post(cop.getId(), cm1.getId(), f1.getId(), f1t1.getId(), getSubject(), getLorem())
      val f1t1r2 = new Post(cop.getId(), cm4.getId(), f1.getId(), f1t1.getId(), getSubject(), getLorem())
      val f1r1r1 = new Post(cop.getId(), cm1.getId(), f1.getId(), f1t1r1.getId(), getSubject(), getLorem())
      val f1r1r2 = new Post(cop.getId(), cm3.getId(), f1.getId(), f1t1r1.getId(), getSubject(), getLorem())
      val f1t1r3 = new Post(cop.getId(), cm2.getId(), f1.getId(), f1t1.getId(), getSubject(), getLorem())
      val f1t2 = new Post(cop.getId(), msu.getId(), f1.getId(), null, getSubject(), getLorem())
      val f1t3 = new Post(cop.getId(), cm1.getId(), f1.getId(), null, getSubject(), getLorem())
      val f2t1 = new Post(cop.getId(), msu.getId(), f2.getId(), null, getSubject(), getLorem())
      val f2t2 = new Post(cop.getId(), msu.getId(), f2.getId(), null, getSubject(), getLorem())
      val f3t1 = new Post(cop.getId(), cm1.getId(), f3.getId(), null, getSubject(), getLorem())
      val f3t2 = new Post(cop.getId(), msu.getId(), f3.getId(), null, getSubject(), getLorem())
      val f3t3 = new Post(cop.getId(), cm5.getId(), f3.getId(), null, getSubject(), getLorem())
      val f1t4_no = new Post(cop.getId(), cm5.getId(), f1.getId(), null, getSubject(), getLorem())
      f1t4_no.setIsActive(false)
      val posts = List(f1t1, f1t1r1, f1t1r2, f1r1r1, f1r1r2, f1t1r3, f1t2, f1t3, f2t1, f2t2, f3t1, f3t2, f3t3, f1t4_no)
      PostDAO.put(posts)
      
      notloaded = false
    }
  }
  
  private def getSubject(): String = {
    subjects(rand.nextInt(50))
  }
  
  private def getLorem(): String = {
    lorems(rand.nextInt(10))
  }
  
  val subjects: Array[String] = Array(
    "Lorem Ipsum Dolor Sit Amet",
    "Consectetuer Adipiscing Elit",
    "Morbi Commodo",
    "Ipsum Sed Pharetra Gravida",
    "Orci Magna Rhoncus Neque",
    "Id Pulvinar Odio Lorem Non Turpis",
    "Nullam Sit Amet Enim",
    "Suspendisse Id Velit Vitae Ligula Volutpat Condimentum",
    "Aliquam Erat Volutpat",
    "Vivamus Pharetra Posuere Sapien",
    "Nam Consectetuer",
    "Nunc Eget Euismod Ullamcorper",
    "Lectus Nunc Ullamcorper Orci",
    "Fermentum Bibendum Enim Nibh Eget Ipsum",
    "Donec Porttitor Ligula Eu Dolor",
    "Maecenas Vitae Nulla Consequat Libero Cursus Venenatis",
    "Quisque Facilisis Erat a Dui",
    "Nam Malesuada Ornare Dolor",
    "Diam Sit Amet Rhoncus Ornare",
    "Erat Elit Consectetuer Erat",
    "Id Egestas Pede Nibh Eget Odio",
    "Velit Vel Porta Elementum",
    "Magna Diam Molestie Sapien",
    "Non Aliquet Massa Pede Eu Diam",
    "Aliquam Iaculis",
    "Fusce Et Ipsum Et Nulla Tristique Facilisis",
    "Donec Eget Sem Sit Amet Ligula Viverra Gravida",
    "Etiam Vehicula Urna Vel Turpis",
    "Suspendisse Sagittis Ante a Urna",
    "Morbi a Est Quis Orci Consequat Rutrum",
    "Nullam Egestas Feugiat Felis",
    "Integer Adipiscing Semper Ligula",
    "Nunc Molestie",
    "Nisl Sit Amet Cursus Convallis",
    "Sapien Lectus Pretium Metus",
    "Vitae Pretium Enim Wisi Id Lectus",
    "Donec Vestibulum",
    "Neque Id Dignissim Ultrices",
    "Tellus Mauris Dictum Elit",
    "Vel Lacinia Enim Metus Eu Nunc",
    "Proin at Eros Non Eros Adipiscing Mollis",
    "Donec Semper Turpis Sed Diam",
    "Sed Consequat Ligula Nec Tortor",
    "Integer Eget Sem",
    "Ut Vitae Enim Eu Est Vehicula Gravida",
    "Morbi Ipsum Ipsum",
    "Pellentesque Neque",
    "Nulla Luctus Erat Vitae Libero",
    "Integer Nec Enim",
    "Phasellus Aliquam Enim Et Tortor"
  )
  
  val lorems: Array[String] = Array(
    "Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Morbi commodo, ipsum sed pharetra gravida, orci magna rhoncus neque, id pulvinar odio lorem non turpis. Nullam sit amet enim. Suspendisse id velit vitae ligula volutpat condimentum. Aliquam erat volutpat. Sed quis velit. Nulla facilisi. Nulla libero. Vivamus pharetra posuere sapien. Nam consectetuer. Sed aliquam, nunc eget euismod ullamcorper, lectus nunc ullamcorper orci, fermentum bibendum enim nibh eget ipsum. Donec porttitor ligula eu dolor. Maecenas vitae nulla consequat libero cursus venenatis. Nam magna enim, accumsan eu, blandit sed, blandit a, eros.",
    "Quisque facilisis erat a dui. Nam malesuada ornare dolor. Cras gravida, diam sit amet rhoncus ornare, erat elit consectetuer erat, id egestas pede nibh eget odio. Proin tincidunt, velit vel porta elementum, magna diam molestie sapien, non aliquet massa pede eu diam. Aliquam iaculis. Fusce et ipsum et nulla tristique facilisis. Donec eget sem sit amet ligula viverra gravida. Etiam vehicula urna vel turpis. Suspendisse sagittis ante a urna. Morbi a est quis orci consequat rutrum. Nullam egestas feugiat felis. Integer adipiscing semper ligula. Nunc molestie, nisl sit amet cursus convallis, sapien lectus pretium metus, vitae pretium enim wisi id lectus. Donec vestibulum. Etiam vel nibh. Nulla facilisi. Mauris pharetra. Donec augue. Fusce ultrices, neque id dignissim ultrices, tellus mauris dictum elit, vel lacinia enim metus eu nunc.",
    "Proin at eros non eros adipiscing mollis. Donec semper turpis sed diam. Sed consequat ligula nec tortor. Integer eget sem. Ut vitae enim eu est vehicula gravida. Morbi ipsum ipsum, porta nec, tempor id, auctor vitae, purus. Pellentesque neque. Nulla luctus erat vitae libero. Integer nec enim. Phasellus aliquam enim et tortor. Quisque aliquet, quam elementum condimentum feugiat, tellus odio consectetuer wisi, vel nonummy sem neque in elit. Curabitur eleifend wisi iaculis ipsum. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. In non velit non ligula laoreet ultrices. Praesent ultricies facilisis nisl. Vivamus luctus elit sit amet mi. Phasellus pellentesque, erat eget elementum volutpat, dolor nisl porta neque, vitae sodales ipsum nibh in ligula. Maecenas mattis pulvinar diam. Curabitur sed leo.",
    "Nulla facilisi. In vel sem. Morbi id urna in diam dignissim feugiat. Proin molestie tortor eu velit. Aliquam erat volutpat. Nullam ultrices, diam tempus vulputate egestas, eros pede varius leo, sed imperdiet lectus est ornare odio. Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Proin consectetuer velit in dui. Phasellus wisi purus, interdum vitae, rutrum accumsan, viverra in, velit. Sed enim risus, congue non, tristique in, commodo eu, metus. Aenean tortor mi, imperdiet id, gravida eu, posuere eu, felis. Mauris sollicitudin, turpis in hendrerit sodales, lectus ipsum pellentesque ligula, sit amet scelerisque urna nibh ut arcu. Aliquam in lacus. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nulla placerat aliquam wisi. Mauris viverra odio. Quisque fermentum pulvinar odio. Proin posuere est vitae ligula. Etiam euismod. Cras a eros.",
    "Nunc auctor bibendum eros. Maecenas porta accumsan mauris. Etiam enim enim, elementum sed, bibendum quis, rhoncus non, metus. Fusce neque dolor, adipiscing sed, consectetuer et, lacinia sit amet, quam. Suspendisse wisi quam, consectetuer in, blandit sed, suscipit eu, eros. Etiam ligula enim, tempor ut, blandit nec, mollis eu, lectus. Nam cursus. Vivamus iaculis. Aenean risus purus, pharetra in, blandit quis, gravida a, turpis. Donec nisl. Aenean eget mi. Fusce mattis est id diam. Phasellus faucibus interdum sapien. Duis quis nunc. Sed enim.",
    "Pellentesque vel dui sed orci faucibus iaculis. Suspendisse dictum magna id purus tincidunt rutrum. Nulla congue. Vivamus sit amet lorem posuere dui vulputate ornare. Phasellus mattis sollicitudin ligula. Duis dignissim felis et urna. Integer adipiscing congue metus. Nam pede. Etiam non wisi. Sed accumsan dolor ac augue. Pellentesque eget lectus. Aliquam nec dolor nec tellus ornare venenatis. Nullam blandit placerat sem. Curabitur quis ipsum. Mauris nisl tellus, aliquet eu, suscipit eu, ullamcorper quis, magna. Mauris elementum, pede at sodales vestibulum, nulla tortor congue massa, quis pellentesque odio dui id est. Cras faucibus augue.",
    "Suspendisse vestibulum dignissim quam. Integer vel augue. Phasellus nulla purus, interdum ac, venenatis non, varius rutrum, leo. Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Duis a eros. Class aptent taciti sociosqu ad litora torquent per conubia nostra, per inceptos hymenaeos. Fusce magna mi, porttitor quis, convallis eget, sodales ac, urna. Phasellus luctus venenatis magna. Vivamus eget lacus. Nunc tincidunt convallis tortor. Duis eros mi, dictum vel, fringilla sit amet, fermentum id, sem. Phasellus nunc enim, faucibus ut, laoreet in, consequat id, metus. Vivamus dignissim. Cras lobortis tempor velit. Phasellus nec diam ac nisl lacinia tristique. Nullam nec metus id mi dictum dignissim. Nullam quis wisi non sem lobortis condimentum. Phasellus pulvinar, nulla non aliquam eleifend, tortor wisi scelerisque felis, in sollicitudin arcu ante lacinia leo.",
    "Pellentesque habitant morbi tristique senectus et netus et malesuada fames ac turpis egestas. Vestibulum tortor quam, feugiat vitae, ultricies eget, tempor sit amet, ante. Donec eu libero sit amet quam egestas semper. Aenean ultricies mi vitae est. Mauris placerat eleifend leo. Quisque sit amet est et sapien ullamcorper pharetra. Vestibulum erat wisi, condimentum sed, commodo vitae, ornare sit amet, wisi. Aenean fermentum, elit eget tincidunt condimentum, eros ipsum rutrum orci, sagittis tempus lacus enim ac dui. Donec non enim in turpis pulvinar facilisis. Ut felis.",
    "Cras sed ante. Phasellus in massa. Curabitur dolor eros, gravida et, hendrerit ac, cursus non, massa. Aliquam lorem. In hac habitasse platea dictumst. Cras eu mauris. Quisque lacus. Donec ipsum. Nullam vitae sem at nunc pharetra ultricies. Vivamus elit eros, ullamcorper a, adipiscing sit amet, porttitor ut, nibh. Maecenas adipiscing mollis massa. Nunc ut dui eget nulla venenatis aliquet. Sed luctus posuere justo. Cras vehicula varius turpis. Vivamus eros metus, tristique sit amet, molestie dignissim, malesuada et, urna.",
    "Cras dictum. Maecenas ut turpis. In vitae erat ac orci dignissim eleifend. Nunc quis justo. Sed vel ipsum in purus tincidunt pharetra. Sed pulvinar, felis id consectetuer malesuada, enim nisl mattis elit, a facilisis tortor nibh quis leo. Sed augue lacus, pretium vitae, molestie eget, rhoncus quis, elit. Donec in augue. Fusce orci wisi, ornare id, mollis vel, lacinia vel, massa."
  )
}