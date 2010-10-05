package com.nonebetwixt.agent.model

import com.nonebetwixt.agent.ui.AgentServices

import com.sleepycat.je.{DatabaseException, Environment, EnvironmentConfig}
import com.sleepycat.persist.{EntityStore, StoreConfig}

import java.io.File

object DbSession {
  var env: Option[Environment] = None
  var store: Option[EntityStore] = None

  def getEntityStore(dbFile: File): Option[EntityStore] = {
    store match {
      case None => init(dbFile)
      case _ => 
    }
    store
  }
  
  def getContentAccessor(): ContentAccessor =
    new ContentAccessor(DbSession.getEntityStore(AgentServices.cmFile).getOrElse(null))
  
  def getEnv(dbFile: File): Option[Environment] = {
    env match {
      case None => init(dbFile)
      case _ =>
    }
    env
  }
  
  def close() {
    store match {
      case Some(s) =>
        try {
          s.close()
        } catch {
          case dbe: DatabaseException => println("\n***** ERROR CLOSING STORE: " + dbe.toString + " *****\n")
        }
      case None =>
    }
    env match {
      case Some(e) =>
        try {
          e.close()
        } catch {
          case dbe: DatabaseException => println("\n***** ERROR CLOSING ENVIRONMENT: " + dbe.toString + " *****\n")
        }
      case None =>
    }
  }
  
  private def init(dbFile: File) {
    try {
      val envConfig = new EnvironmentConfig()
      envConfig.setAllowCreate(true)

      env = Some(new Environment(dbFile, envConfig))

      val storeConfig = new StoreConfig()
      storeConfig.setAllowCreate(true)

      store = Some(new EntityStore(env.get, "ContentManager", storeConfig))

    } catch {
      case e => println("\n***** ERROR: DbSession init(dbFile) FAILED: " + e.toString + " *****\n")
    }
  }
  
}