// -*- mode: Scala;-*- 
// Filename:    Schema.scala 
// Authors:     lgm                                                    
// Creation:    Mon May 17 10:14:25 2010 
// Copyright:   Not supplied 
// Description: 
// ------------------------------------------------------------------------

package com.biosimilarity.validation

import _root_.scala.collection.mutable.HashMap

import org.squeryl._
//import org.squeryl.PrimitiveTypeMode._
import org.squeryl.customtypes.CustomTypesMode._
import org.squeryl.customtypes._
import org.squeryl.adapters.H2Adapter
import java.sql.{SQLException, Connection => SQLConnection}
import org.squeryl.dsl._

import java.net.URI
import _root_.java.util.UUID

import java.sql.DatabaseMetaData
import java.sql.ResultSet

trait SafeTableCreation {
  def checkTableExists(
    conn : SQLConnection,
    tblName : String
  ) : Boolean = {
    val dbm : DatabaseMetaData = conn.getMetaData()
    val rs : ResultSet = dbm.getTables(null, null, tblName, null)
    rs.next()
  }
}

trait UUIDOps {
  def getUUID(): UUID = UUID.randomUUID() 
  def getUUID(uuid: String) = UUID.fromString(uuid)
}

object AgentSchema extends Schema with UUIDOps {    
  val registrants        = table[Registrant](      "registrants"       )
  val users              = table[User](            "users"             )
  val aliases            = table[Alias](           "aliases"           )
  val agents             = table[Agent](           "agents"            )
  val coplinks           = table[Coplink](         "coplinks"          )

  val justifiedRequests  = table[Msg](             "requests"          )
  val justifiedResponses = table[Msg](             "responses"         )

  val connections        = table[Link](            "connections"       )
  val connectionStates   = table[LinkState](       "connnectionStates" )

  // val userAliases =
  //   oneToManyRelation(
  //     users,
  //     aliases
  //   ).via((u, a) => u.id === a.userId)
  // 
  // val userAgents =
  //   oneToManyRelation(
  //     users,
  //     agents
  //   ).via((u, a) => u.id === a.userId)
  // 
  // val userCoplinks =
  //   oneToManyRelation(
  //     users,
  //     coplinks
  //   ).via((u, c) => u.id === c.userId)
  // 
  // val agentRequests =
  //   oneToManyRelation(
  //     agents,
  //     justifiedRequests
  //   ).via((a, m) => a.id === m.recipientId)
  // 
  // val agentResponses =
  //   oneToManyRelation(
  //     agents,
  //     justifiedResponses
  //   ).via((a, m) => a.id === m.recipientId)
  // 
  // val agentLeftNameSpace =
  //   oneToManyRelation(
  //     agents,
  //     connections
  //   ).via((a, c) => (a.id === c.leftId))
  // 
  // val agentRightNameSpace =
  //   oneToManyRelation(
  //     agents,
  //     connections
  //   ).via((a, c) => (a.id === c.rightId))

  // drop (schema) is normaly protected... for safety,
  // here we live dangerously !
  override def drop = super.drop  
}

trait Domain[A] {
  self: Product1[Any] =>

  def label             : String
  def baseURI           : URI
  def asURI             : URI
  def validate( a : A ) : Unit
  def value             : A

  validate( value )
}

trait AgentDomain[A] extends Domain[A] {
  self: Product1[Any] =>
    var _baseURI        : Option[URI] = None 
    var _uri            : Option[URI] = None

    def baseURI = {
      _baseURI match {
        case Some( uri ) => uri
        case None => {
          val uri = new URI( "agent", "domain", "" )
          _baseURI = Some( uri )
          uri
        }
      }
    }
    def asURI = {
      _uri match {
        case Some( uri ) => uri
        case None => {
          val uri =
            baseURI.relativize(
              new URI(
                "agent",
                "www.biosimilarity.com",
                "domain",
                label + "=" + value,
                ""
              )
            )
          _uri = Some( uri )
          uri
        }
      }      
    }  
}

/* -------------------------------------------------------------------------- */
/* Persistent properties                                                      */
/* -------------------------------------------------------------------------- */

class Name( val name : String )
extends StringField( name )
with AgentDomain[String] {
  def excludedChars : List[String] = List( " " )
  def label = "Name"
  def validate( a : String ) = {
    for( c <- excludedChars ) {
      if ( a.contains( c ) ) {
        throw new Exception( "names are not allowed to contain: " + c )
      }
    }
  }
}

abstract class Reference( val key : Long )
extends LongField( key )
with AgentDomain[Long] {
  def labelQualifier : Option[String]
  def label = {
    (
      (
        labelQualifier match {
          case Some( lblQlfr ) => lblQlfr
          case None => ""
        }
      )
      + "Id"
    )
  }
  def validate( a : Long ) = {
    if ( a < 0 ) {
      throw new Exception( "keys must be positive: " + key )
    }
  }
}

class Key( val id : Long ) extends Reference( id ) {
  var _labelQualifierMemo : Option[String] = None
  def labelQualifier = _labelQualifierMemo
  def labelQualifier( lblQlfr : String ) = {
    _labelQualifierMemo = Some( lblQlfr )
  }
}

//class Identity( val uuid : getUUID() )
//extends StringField( uuid.toString )
class Identity( override val value : String )
extends StringField( value )
with AgentDomain[String] 
with UUIDOps {
  def asUUID : UUID = {
    getUUID( value )
  }
  def asNumeric : BigInt = {
    (( value.toString.split( "-" ) :\ ( BigInt( "0", 16 ), 0 ) )(
      { ( s : String, acc : ( BigInt, Int ) ) => {
        val sBI = BigInt( s, 16 )
        acc match {
          case ( total, shift ) =>
            ((( sBI * BigInt( 16 ).pow( shift ) ) + total), (s.length + shift))
        }
      }
     }
    ))._1
  }
  def asId : Long = {
    (asNumeric % java.lang.Integer.MAX_VALUE).toLong
  }
  def label = "Identity"
  def validate( a : String ) = {
    try {
      if ( a != null ) {
      	getUUID( a )
      }
      else {
      	println( "Warning : null UUID String." )
      }
    }
    catch {
      case e => println( "Warning : " + a + " is not a valid UUID String." )
    }
  }
}

class PURI( val uriStr : String )
extends StringField( uriStr )
with AgentDomain[String] {  
  def uri : URI = {
    new URI( uriStr )
  }
  def label = "URI"
  def validate( a : String ) = {
    uri.toString.equals( a )
  }
  def email = uriStr.substring(6)
}

/* -------------------------------------------------------------------------- */
/* Persistent entities                                                        */
/* -------------------------------------------------------------------------- */

class Alias(
  val id        : Key,
  val identity  : Identity,
  val dba       : Name, // D_oing B_usiness A_s
  var userId    : Key
) extends KeyedEntity[LongField] {
  // lazy val user : ManyToOne[User] =
  //   AgentSchema.userAliases.right( this )
}

class Registrant(
  val id        : Key,
  val identity  : Identity,
  val email     : PURI,
  var code      : Name,
  var expiry    : Long
) extends KeyedEntity[LongField] {}

class User(
  val id        : Key,
  val identity  : Identity,
  var firstName : Name,
  var lastName  : Name,
  var email     : PURI,
  var pwd       : Name,
  val salt      : Name
) extends KeyedEntity[LongField] {
  // lazy val aliases : OneToMany[Alias] =
  //   AgentSchema.userAliases.left( this )
  // lazy val agents : OneToMany[Agent] =
  //   AgentSchema.userAgents.left( this )
    
  def authenticate(password: String): Boolean = {
    pwd.name.toLowerCase == password.toLowerCase
  }
  
  def getName(): String = {
    this.firstName.value + " " + this.lastName.value
  }
}

class Coplink(
  val id        : Key,
  val identity  : Identity,
  val cop       : Identity,
  val memberId  : Identity,
  val copPw     : String,
  var userId    : Key
) extends KeyedEntity[LongField] {
  // lazy val user : ManyToOne[User] =
  //   AgentSchema.userCoplinks.right( this )
}

class Agent(
  val id        : Key,
  val identity  : Identity,
  val userId    : Key,
  var token     : Name
) extends KeyedEntity[LongField] {
  // lazy val user : ManyToOne[User] =
  //   AgentSchema.userAgents.right( this )
  // lazy val requests : OneToMany[Msg] =
  //   AgentSchema.agentRequests.left( this )
  // lazy val responses : OneToMany[Msg] =
  //   AgentSchema.agentResponses.left( this )
  // lazy val leftNames : OneToMany[Link] =
  //   AgentSchema.agentLeftNameSpace.left( this )
  // lazy val rightNames : OneToMany[Link] =
  //   AgentSchema.agentRightNameSpace.left( this )
  def getToken = token.value
  def setToken(tkn: String) = {
    token = new Name(tkn)
  }
}

class Link(
  val id        : Key,
  val leftId    : Key,
  val rightId   : Key,
  val stateId   : Key
) extends KeyedEntity[LongField] {
}

class LinkState(
  val id        : Key,
  val cnxnId    : Key
) extends KeyedEntity[LongField] {
}

class Msg(
  val id          : Key,
  val recipientId : Key,
  val toId        : PURI,
  val fromId      : PURI,
  val flowId      : Key
) extends KeyedEntity[LongField] {
  // lazy val messenger : ManyToOne[Agent] =
  //   AgentSchema.agentRequests.right( this )
}

/* -------------------------------------------------------------------------- */
/* Session management                                                         */
/* -------------------------------------------------------------------------- */

abstract class StoreSession() {
  var _H2Session : Option[Session] = None
  def acquireH2Session() = {
    _H2Session match {
      case Some( session ) => session
      case None => {
      	Class.forName( "org.h2.Driver" );
      	val session = Session.create(
      	  java.sql.DriverManager.getConnection( "jdbc:h2:~/webapps/WEB-INF/db/test", "sa", "" ),
      	  //Currently there are adapters for Oracle, Postgres, MySql and H2 :
      	  new H2Adapter
      	)
      	_H2Session = Some( session )
      	session
      }
    }
  }
}

object StoreSession
extends StoreSession() {
}

/* -------------------------------------------------------------------------- */
/* Bootstrap                                                                  */
/* -------------------------------------------------------------------------- */

object Bootstrap {
  val fredId: UUID = UUID.fromString("00000000-1111-2222-3333-444444444444")
  val fredIdent: Identity = new Identity(fredId.toString)
  val fredPw: String = "betwixt"
  val demoCopId: UUID = UUID.fromString("88888888-4444-4444-4444-cccccccccccc")
  val demoCopIdent: Identity = new Identity(demoCopId.toString)
  val copName: String = "EACOP"
  val copDesc: String = "Early Adopters Community of Practice"
}

/* -------------------------------------------------------------------------- */
/* Validation suite                                                           */
/* -------------------------------------------------------------------------- */

object ValidatePersistence
extends StoreSession() {
  import AgentSchema._
  
  def initSchema = {
    AgentSchema.drop
    AgentSchema.create    
  }
  
  def testWithH2 = {
    test( acquireH2Session() )
  }
  
  def test( session : Session ) = using( session ) {
    session.setLogger(s=>println(s))
    try {
      initSchema 
      populateStore    
      probeStore      
    }
    catch {
      case e : RuntimeException => {
      	println(e.getCause)
      	e.printStackTrace
      }
    }    
  }
  
  def populateStore : Unit = {
    // tbd
    val rids = for( i <- 1 to 2 ) yield { new Identity( getUUID().toString ) }
    val uids = for( i <- 1 to 4 ) yield { new Identity( getUUID().toString ) }
    val aids = for( i <- 1 to 4 ) yield { new Identity( getUUID().toString ) }

    val identStrs =
      for( i <- 1 to 4 ) yield { getUUID().toString }

    for( i <- 0 to 3 ) {
      println( "identStr( " + i + " ) = " + identStrs( i ) )
    }

    registrants.insert(
      new Registrant(
        new Key( rids( 0 ).asId ), rids( 0 ),
        new PURI( "email:Bob.Dole@nonebetwixt.com" ),
        new Name("code"),
        0L
      )
    )
    registrants.insert(
      new Registrant(
        new Key( rids( 1 ).asId ), rids( 1 ),
        new PURI( "email:Bill.Clinton@nonebetwixt.com" ),
        new Name("code"),
        0L
      )
    )

    for(
      regs <-
      from( registrants )(
      	r => where(
      	  r.code === new Name( "code" )
      	) select( r )
      )
    ) {
      println( "registrant email: " + regs.email.value )
    }
        
    users.insert(
      new User(
        new Key( uids( 0 ).asId ), uids( 0 ),
      	new Name( "John" ), new Name( "Smith" ),
        new PURI( "email:John.Smith@nonebetwixt.com" ),
        new Name( "changeMe" ),
        new Name( "NaCL" )
      )
    )

    users.insert(
      new User(
        new Key( uids( 1 ).asId ), uids( 1 ),
      	new Name( "Kyle" ), new Name( "Jones" ),
        new PURI( "email:Kyle.Jones@nonebetwixt.com" ),
        new Name( "changeMe" ),
        new Name( "NaCL" )
      )
    )

    users.insert(
      new User(
        new Key( uids( 2 ).asId ), uids( 2 ),
      	new Name( "Franklin" ), new Name( "Ordcutt" ),
        new PURI( "email:Franklin.Ordcutt@nonebetwixt.com" ),
        new Name( "changeMe" ),
        new Name( "NaCL" )
      )
    )

    users.insert(
      new User(
        new Key( uids( 3 ).asId ), uids( 3 ),
      	new Name( "Willard" ), new Name( "Quine" ),
        new PURI( "email:Willard.Quine@nonebetwixt.com" ),
        new Name( "changeMe" ),
        new Name( "NaCL" )
      )
    )

    aliases.insert( new Alias( new Key( aids( 0 ).asId ), aids( 0 ), new Name( "Smitty" ), new Key( uids( 0 ).asId ) ) )
    aliases.insert( new Alias( new Key( aids( 1 ).asId ), aids( 1 ), new Name( "Jonesy" ), new Key( uids( 1 ).asId ) ) )
    aliases.insert( new Alias( new Key( aids( 2 ).asId ), aids( 2 ), new Name( "Spy" ), new Key( uids( 2 ).asId ) ) )
    aliases.insert( new Alias( new Key( aids( 3 ).asId ), aids( 3 ), new Name( "Wiseguy" ), new Key( uids( 3 ).asId ) ) )
  
  }

  def probeStore : Unit = {
    val usrs = from( users )( u => where( u.id > 0 ) select( u ) )
      for( u <- usrs ){ println( u ) }

    println( "Listing users, now..." )
    for( u <- usrs ){
      println( "User: " + u.firstName.name + " " + u.lastName.name )
      println( "has identity " + u.identity.value )
      println( "generating id " + u.identity.asId )
    }
    println( "Users listed." )

    val nonebetwixters =
      from( users )(
      	u => where(
      	  u.email ===  "email:Willard.Quine@nonebetwixt.com"
      	) select( u )
      )

    println( "Listing nonebetwixters..." )
    for( u <- nonebetwixters ){
      println( "User: " + u.firstName.name + " " + u.lastName.name )
      println( "has identity " + u.identity.value )
      println( "generating id " + u.identity.asId )
    }
    println( "nonebetwixters listed." )

    println( "about to delete the nonebetwixters" )
    for( nU <- nonebetwixters ){      
      users.deleteWhere( u => u.id === nU.id )
    }    
    println( "nonebetwixters deleted" )

    val newUsrs = from( users )( u => where( u.id > 0 ) select( u ) )
    for( u <- newUsrs ){ println( u ) }

    println( "Listing users, again..." )
    for( u <- newUsrs ){
      println( "User: " + u.firstName.name + " " + u.lastName.name )
      println( "has identity " + u.identity.value )
      println( "generating id " + u.identity.asId )
    }
    println( "Users listed, again." )
    
    println( "Making agents." )
    val agency =
      for(
	u <- newUsrs;
	agentId = new Identity( getUUID().toString )
      ) yield {
    	new Agent(
    	  new Key( agentId.asId ),
    	  agentId,
    	  u.id,
    	  new Name("")
      )      
    }
    println( "Agents made." )
    
    println( "Inserting agents." )
    for( agent <- agency ) {
      agents.insert( agent )
    }
    println( "Agents inserted." )

    println( "Verifying agent insertions." )
    val newAgents =
      from( agents )( a => where( a.id > 0 ) select( a ) )
    
    for( agent <- newAgents ) {
      println( "has identity " + agent.identity.value )
      println( "generating id " + agent.identity.asId )
      println( "associated to user id " + agent.userId )
      for(
	usr <-
	from( users )(
      	  u => where(
      	    u.id === agent.userId
      	  ) select( u )
	)
      ) {
	println( "named " + usr.firstName.name + " " + usr.lastName.name )
      }
    }
    println( "Agent insertions verified." )
  }
}
