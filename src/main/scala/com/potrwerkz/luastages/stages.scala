package com.potrwerkz.luastages

import akka.actor._
import akka.pattern._
import akka.util._
import org.luaj.vm2._
import org.luaj.vm2.lib._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration._
import scala.util._

/** Lua wrapper for Akka actors
 *
**/
object stages extends TwoArgFunction {
  implicit val timeout = Timeout(5.seconds)

  /** optional configuration */
  var config: Option[ActorSystem] = None

  /** look for ActorSystem to be defined in config, else create new ActorSystem */
  lazy val system = config.getOrElse(ActorSystem())
  
  /** LuaJ call override
   * 
   * @param modname name of the module
   * @param env pointer to the calling Lua environment
   */
  override def call(modname: LuaValue, env: LuaValue): LuaValue = {
    // look for 'as' to be defined in the current env; if so, use it
    config = Option(env.get(LuaActor.ACTOR_SYSTEM_KEY).touserdata.asInstanceOf[ActorSystem])

    val library = LuaValue.tableOf
    library.set("actor", actor)
    library.set("spawn", spawn)
    library.set("ask", send(true))
    library.set("tell", send(false))
    library.set("join", join)
    library.set("sleep", sleep)
    
    library
  }

  /** "global" spawn fuction */
  def spawn(amt: LuaValue, bhv: LuaValue, name: String) = {
    name match {
      case "" => system.actorOf(LuaActor.props(amt, bhv))
      case _ => system.actorOf(LuaActor.props(amt, bhv), name)
    }
  }

  def toLuaFuture(f: Future[LuaValue]): LuaUserdata = {
    // define metatable for Akka Future userdata
    val futureMetatable = LuaValue.tableOf
    futureMetatable.set("__index", future(f).call)

    val res = LuaValue.userdataOf(f)
    res.setmetatable(futureMetatable).asInstanceOf[LuaUserdata]
  }

  /* Actor module definition
    *
    */
  object actor extends VarArgFunction {
    /** LuaJ invoke override
      *
      *  @param args LuaJ argument list
      */
    override def invoke(args: Varargs): LuaValue = {
      val mod = LuaValue.tableOf
      mod.set("spawn", spawn)
      mod.set("ask", send(true))
      mod.set("tell", send(false))
      mod
    }
  }
  
  /* Future module definition
   *  
   */
  object future {
    def apply(future: Future[LuaValue]) = new future(future)
  }

  class future(future: Future[LuaValue]) extends ZeroArgFunction {
    /** LuaJ call override
     *
     */
    override def call: LuaValue = {
      val mod = LuaValue.tableOf
      mod.set("result", result)
      mod.set("ready", ready)
      mod
    }
    
    object result extends ZeroArgFunction {
      /** LuaJ call override
       *  
       */
      override def call: LuaValue = {        
        if (future.isCompleted) {
          future.value.get.get
        } else {
          Await.result(future, timeout.duration)
        }
      }
    }
    
    object ready extends VarArgFunction {
      /** LuaJ invoke override
       *  
       */
      override def invoke(args: Varargs): LuaValue = {
        val arg1 = args.arg(1)
        val arg2 = args.arg(2)
        
        if (arg1.isfunction && !arg2.isfunction) {
          future.onSuccess { 
            case value => arg1.call(value)
          }
        } else if (arg1.isfunction && arg2.isfunction) {
          future.onComplete {
            case Success(value) => arg1.call(value)
            case Failure(e) => arg2.call(LuaValue.userdataOf(e))
          }
        }
        
        LuaValue.NIL
      }
    }
  }

  /* Gives life to a new actor
   *
   * @param name name (optional)
   * @param behav actor behavior table (optional)
   * @return actor reference
   *  
   * Lua usage:
   *  = local stages = require "stages"
   *  - a = stages.spawn()
   *  - a = stages.spawn(behavior)
   *  - a = stages.spawn(behavior, "id")
   */
  object spawn extends VarArgFunction {
    /** LuaJ invoke override
     *  
     *  @param args LuaJ argument list
     *  @return LuaValue
     */
    override def invoke(args: Varargs): LuaValue = {
      var name = ""
      var bhv: LuaValue = null
      
      val arg1 = args.arg(1)
      val arg2 = args.arg(2)
      
      if (arg1.isstring) name = arg1.tojstring
      if (arg2.isstring) name = arg2.tojstring      
      
      if (arg1.istable) bhv = arg1
      if (arg2.istable) bhv = arg2
      
      // define metatable for Akka ActorRef userdata
      val luaActor = LuaValue.tableOf
      luaActor.setmetatable(LuaValue.tableOf)
      luaActor.getmetatable.set(LuaValue.INDEX, bhv)
      
      // spawn the Akka actor, and make it an actor prototype
      val actorMetatable = LuaValue.tableOf
      actorMetatable.set("__index", actor.call)
      LuaValue.userdataOf(spawn(actorMetatable, luaActor, name)).setmetatable(actorMetatable) // note: returns this
    }
    
  }
    
  /* Sends a message to the given actor
   *  
   * @param target reference to target actor
   * @param message the massage to send
   *
   * Lua usage:
   *  = local stages = require "stages"
   *  - f = stages.send(actorRef, "msg")
   *  - f = stages.send(actorRef, t1{1,2.1,"tiger",t2{}})
   */
  object send {
    def apply(ask: Boolean) = new send(ask)
  }

  class send(ask: Boolean) extends VarArgFunction {
    /** LuaJ invoke override
     *  
     *  @param args LuaJ argument list  
     */
    override def invoke(args: Varargs): LuaValue = {
      val arg1 = args.arg(1)
      val arg2 = args.arg(2)
     
      val target = arg1.touserdata.asInstanceOf[ActorRef]
      
      if (target == null) {
        return LuaValue.error("stages.send: target actor ref required")
      }
      
      // inspect msg argument for futures
      if (arg2.isuserdata && arg2.touserdata.isInstanceOf[Future[Any]]) {
        // handle continuation
        continue(target, arg2.touserdata.asInstanceOf[Future[LuaValue]])          
      } else {
        // handle normal message
        if (ask) {
          // capture Akka Future as Lua userdata
          toLuaFuture(akka.pattern.ask(target, arg2).mapTo[LuaValue])
        } else {
          target ! arg2
          LuaValue.NONE
        }
      }
    }
    
    def continue(target: ActorRef, f: Future[LuaValue]): LuaValue = {
      if (ask) {
        // use flatMap to map the future to the target ActorRef
        toLuaFuture(f flatMap {
          value => akka.pattern.ask(target, value).mapTo[LuaValue]
        })
      } else {
        // simply pipe the result of the future to the target ActorRef
        f pipeTo target
        LuaValue.NONE
      }
    }
  }
  
  /*
   * 
   */
  object join extends VarArgFunction {
    /** LuaJ invoke override
     *  
     */
    override def invoke(args: Varargs): LuaValue = {
      // build a list of Akka Futures, Futurizing any standard Lua parameters
      val fList = List.tabulate(args.narg)(i => toAkkaFuture(args.arg(i+1)))
      
      // convert the list of Futures to a Future of a list
      val join = Future.sequence(fList)
      
      // map the join list to a LuaTable, and convert to a Lua future
      toLuaFuture(join map {
        value => LuaValue.listOf(value.toArray[LuaValue])
      })
    }
    
    def toAkkaFuture(arg: LuaValue): Future[LuaValue] = {
      if (arg.isuserdata && arg.touserdata.isInstanceOf[Future[Any]]) {
        arg.touserdata.asInstanceOf[Future[LuaValue]]
      } else {
        Future { arg }
      }
    }
  }
  
  /* Platform independent sleep function
   * 
   * @param timeout timeout in milliseconds
   * 
   * Lua usage:
   *  = local stages = require "stages"
   *  - f = stages.sleep(150)
   */
  object sleep extends OneArgFunction {
    /** LuaJ call override
     * 
     * @param arg LuaJ argument
     */
    override def call(arg: LuaValue): LuaValue = {
      Thread sleep arg.tolong
      LuaValue.NONE
    }
  }

}
