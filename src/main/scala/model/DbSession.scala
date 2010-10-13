package com.nonebetwixt.agent.model

import com.nonebetwixt.agent.ui.AgentServices

import com.sleepycat.je.{DatabaseException, Environment, EnvironmentConfig}
import com.sleepycat.persist.{EntityStore, StoreConfig}

import java.io.File

object DbSession {
  val dbFile: File = AgentServices.cmFile
  val envConfig = new EnvironmentConfig()
  val storeConfig = new StoreConfig()
  var env: Environment = null
  var store: EntityStore = null
  var contentAccessor: ContentAccessor = null
  
  try {
    envConfig.setAllowCreate(true)
    storeConfig.setAllowCreate(true)
    env = new Environment(dbFile, envConfig)
    storeConfig.setAllowCreate(true)
    store = new EntityStore(env, "ContentManager", storeConfig)
    contentAccessor = new ContentAccessor(store)
  } catch {
    case e => println("\n***** ERROR: DbSession init(dbFile) FAILED: " + e.toString + " *****\n")
  }

  def getEntityStore(dbFile: File): Option[EntityStore] = {
    store match {
      case null => None
      case s => Some(s)
    }
  }
  
  def getEnv(dbFile: File): Option[Environment] = {
    env match {
      case null => None
      case e => Some(e)
    }
  }
  
  def close() {
    store match {
      case null =>
      case s =>
        try {
          s.close()
        } catch {
          case dbe: DatabaseException => println("\n***** ERROR CLOSING STORE: " + dbe.toString + " *****\n")
        }
    }
    env match {
      case null =>
      case e =>
        try {
          e.close()
        } catch {
          case dbe: DatabaseException => println("\n***** ERROR CLOSING ENVIRONMENT: " + dbe.toString + " *****\n")
        }
    }
  }
}