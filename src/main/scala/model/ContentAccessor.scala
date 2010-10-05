package com.nonebetwixt.agent.model

import java.io.File
import java.util.{Date, UUID}

import com.sleepycat.je.DatabaseException
import com.sleepycat.persist.{EntityStore, PrimaryIndex, SecondaryIndex}

class ContentAccessor(store: EntityStore) {
  // The User
  val contentUsersById: PrimaryIndex[String,ContentUser] =
    store.getPrimaryIndex(classOf[String], classOf[ContentUser])
  
  // Content items
  val contentItemsById: PrimaryIndex[String,ContentItem] =
    store.getPrimaryIndex(classOf[String], classOf[ContentItem])
  val contentItemsByUser: SecondaryIndex[String,String,ContentItem] =
    store.getSecondaryIndex(contentItemsById, classOf[String], "userId")
  val contentItemsByParent: SecondaryIndex[String,String,ContentItem] =
    store.getSecondaryIndex(contentItemsById, classOf[String], "parentId")
  
  // Tags
  val contentTagsByName: PrimaryIndex[String,ContentTag] =
    store.getPrimaryIndex(classOf[String], classOf[ContentTag])
  val contentTagsByAbbr: SecondaryIndex[String,String,ContentTag] =
    store.getSecondaryIndex(contentTagsByName, classOf[String], "abbr")
  
  // Tags to Items
  val itemTagsById: PrimaryIndex[String,ItemTag] =
    store.getPrimaryIndex(classOf[String], classOf[ItemTag])
  val itemTagsByItemId: SecondaryIndex[String,String,ItemTag] =
    store.getSecondaryIndex(itemTagsById, classOf[String], "itemId")
  val itemTagsByTagName: SecondaryIndex[String,String,ItemTag] =
    store.getSecondaryIndex(itemTagsById, classOf[String], "tagName")
  
  // Tags to InternetCnxns
  val cnxnTagsById: PrimaryIndex[String,CnxnTag] =
    store.getPrimaryIndex(classOf[String], classOf[CnxnTag])
  val cnxnTagsByCnxnId: SecondaryIndex[String,String,CnxnTag] =
    store.getSecondaryIndex(cnxnTagsById, classOf[String], "cnxnId")
  val cnxnTagsByTagName: SecondaryIndex[String,String,CnxnTag] =
    store.getSecondaryIndex(cnxnTagsById, classOf[String], "tagName")
    
  // Cops
  val copsById: PrimaryIndex[String,Cop] =
    store.getPrimaryIndex(classOf[String], classOf[Cop])
  val copsByName: SecondaryIndex[String,String,Cop] =
    store.getSecondaryIndex(copsById, classOf[String], "name")
  val copsByIsActive: SecondaryIndex[Boolean,String,Cop] =
    store.getSecondaryIndex(copsById, classOf[Boolean], "isActive")
  
  // Members
  val membersById: PrimaryIndex[String,Member] =
    store.getPrimaryIndex(classOf[String], classOf[Member])
  val membersByCopId: SecondaryIndex[String,String,Member] =
    store.getSecondaryIndex(membersById, classOf[String], "copId")
  val membersByUserId: SecondaryIndex[String,String,Member] =
    store.getSecondaryIndex(membersById, classOf[String], "userId")
  
  // Fora
  val foraById: PrimaryIndex[String,Forum] =
    store.getPrimaryIndex(classOf[String], classOf[Forum])
  val foraByCopId: SecondaryIndex[String,String,Forum] =
    store.getSecondaryIndex(foraById, classOf[String], "copId")
  val foraByUserId: SecondaryIndex[String,String,Forum] =
    store.getSecondaryIndex(foraById, classOf[String], "userId")
  val foraByName: SecondaryIndex[String,String,Forum] =
    store.getSecondaryIndex(foraById, classOf[String], "name")
  val foraByIsActive: SecondaryIndex[Boolean,String,Forum] =
    store.getSecondaryIndex(foraById, classOf[Boolean], "isActive")
  
  // Posts
  val postsById: PrimaryIndex[String,Post] =
    store.getPrimaryIndex(classOf[String], classOf[Post])
  val postsByCopId: SecondaryIndex[String,String,Post] =
    store.getSecondaryIndex(postsById, classOf[String], "copId")
  val postsByUserId: SecondaryIndex[String,String,Post] =
    store.getSecondaryIndex(postsById, classOf[String], "userId")
  val postsByForumId: SecondaryIndex[String,String,Post] =
    store.getSecondaryIndex(postsById, classOf[String], "forumId")
  val postsByReplyToId: SecondaryIndex[String,String,Post] =
    store.getSecondaryIndex(postsById, classOf[String], "replyToId")
  
  // AgentMessages
  val agentMessagesById: PrimaryIndex[String,AgentMessage] =
    store.getPrimaryIndex(classOf[String], classOf[AgentMessage])
  val agentMessagesByUserId: SecondaryIndex[String,String,AgentMessage] =
    store.getSecondaryIndex(agentMessagesById, classOf[String], "userId")
  val agentMessagesBySentToId: SecondaryIndex[String,String,AgentMessage] =
    store.getSecondaryIndex(agentMessagesById, classOf[String], "sentToId")
  val agentMessagesByReplyToId: SecondaryIndex[String,String,AgentMessage] =
    store.getSecondaryIndex(agentMessagesById, classOf[String], "replyToId")
  
  // AgentCnxns
  val agentCnxnsById: PrimaryIndex[String,AgentCnxn] =
    store.getPrimaryIndex(classOf[String], classOf[AgentCnxn])
  val agentCnxnsByLeftId: SecondaryIndex[String,String,AgentCnxn] =
    store.getSecondaryIndex(agentCnxnsById, classOf[String], "leftId")
  val agentCnxnsByRightId: SecondaryIndex[String,String,AgentCnxn] =
    store.getSecondaryIndex(agentCnxnsById, classOf[String], "rightId")
  val agentCnxnsByTagName: SecondaryIndex[String,String,AgentCnxn] =
    store.getSecondaryIndex(agentCnxnsById, classOf[String], "tagName")
  
  // InternetCnxns
  val internetCnxnsById: PrimaryIndex[String,InternetCnxn] =
    store.getPrimaryIndex(classOf[String], classOf[InternetCnxn])
  val internetCnxnsByUserId: SecondaryIndex[String,String,InternetCnxn] =
    store.getSecondaryIndex(internetCnxnsById, classOf[String], "userId")
  val internetCnxnsBySite: SecondaryIndex[String,String,InternetCnxn] =
    store.getSecondaryIndex(internetCnxnsById, classOf[String], "site")
  val internetCnxnsByUri: SecondaryIndex[String,String,InternetCnxn] =
    store.getSecondaryIndex(internetCnxnsById, classOf[String], "uri")
}