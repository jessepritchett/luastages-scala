package com.potrwerkz.luastages.luaext

import org.luaj.vm2._
import org.luaj.vm2.lib._
import org.luaj.vm2.lib.jse.JsePlatform

import akka.util._
import akka.actor._
import akka.pattern._
import scala.util._
import scala.concurrent._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

import com.potrwerkz.luastages.Core

/** Lua wrapper for Akka actors
 *
**/
class stages(name: String) extends TwoArgFunction {
  var core = new Core(name)
  implicit val timeout = Timeout(5.seconds)
  
  /** LuaJ call override
   * 
   * @param modname name of the module
   * @param env pointer to the calling Lua environment
   */
  override def call(modname: LuaValue, env: LuaValue): LuaValue = {
    val library = LuaValue.tableOf
    library.set("actor", new actor)
    library.set("spawn", new spawn)
    library.set("ask", new send(true))
    library.set("tell", new send(false))
    library.set("join", new join)
    library.set("sleep", new sleep)
    
    return library
  }
  
    def toLuaFuture(f: Future[LuaValue]): LuaUserdata = {
    // define metatable for Akka Future userdata
    val futureMetatable = LuaValue.tableOf
    futureMetatable.set("__index", new future(f).call)
    
    val res = LuaValue.userdataOf(f)
    res.setmetatable(futureMetatable)
    
    res
  }
  
  /** Actor class definition
   *  
   */
  class actor extends VarArgFunction {
    /** LuaJ invoke override
     *  
     *  @param args LuaJ argument list
     */
    override def invoke(args: Varargs): LuaValue = {
      val actorClass = LuaValue.tableOf
      actorClass.set("spawn", new spawn)
      actorClass.set("ask", new send(true))
      actorClass.set("tell", new send(false))
      return actorClass
    }
  }
  
  /** Future class definition
   *  
   */
  class future(future: Future[LuaValue]) extends ZeroArgFunction {
    /** LuaJ call override
     *
     */
    override def call: LuaValue = {
      val futureClass = LuaValue.tableOf
      futureClass.set("result", new result)
      futureClass.set("ready", new ready)
      return futureClass
    }
    
    class result extends ZeroArgFunction {
      /** LuaJ call override
       *  
       */
      override def call: LuaValue = {        
        if (future.isCompleted) {
          return future.value.get.get
        } else {
          return Await.result(future, timeout.duration).asInstanceOf[LuaValue]
        }
      }
    }
    
    class ready extends VarArgFunction {
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
        
        return LuaValue.NIL
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
  class spawn extends VarArgFunction {
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
      actorMetatable.set("__index", new actor().call)
      val res = LuaValue.userdataOf(core.Spawn(actorMetatable, luaActor, name))
      res.setmetatable(actorMetatable)
      
      return res
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
          return toLuaFuture(akka.pattern.ask(target, arg2).mapTo[LuaValue])
        } else {
          target ! arg2
          return LuaValue.NONE
        }
      }
    }
    
    def continue(target: ActorRef, f: Future[LuaValue]): LuaValue = {
      if (ask) {
        // use flatMap to map the future to the target ActorRef
        return toLuaFuture(f flatMap {
          value => akka.pattern.ask(target, value).mapTo[LuaValue]
        })
      } else {
        // simply pipe the result of the future to the target ActorRef
        f pipeTo target
        return LuaValue.NONE
      }
      
      LuaValue.NONE
    }
  }
  
  /*
   * 
   */
  class join extends VarArgFunction {
    /** LuaJ invoke override
     *  
     */
    override def invoke(args: Varargs): LuaValue = {
      // build a list of Akka Futures, Futurizing any standard Lua parameters
      val fList = List.tabulate(args.narg)(i => toAkkaFuture(args.arg(i+1)))
      
      // convert the list of Futures to a Future of a list
      val join = Future.sequence(fList)
      
      // map the join list to a LuaTable, and convert to a Lua future
      return toLuaFuture(join map { 
        value => LuaValue.listOf(value.toArray[LuaValue])
      })
    }
    
    def toAkkaFuture(arg: LuaValue): Future[LuaValue] = {
      if (arg.isuserdata && arg.touserdata.isInstanceOf[Future[Any]]) {
        return arg.touserdata.asInstanceOf[Future[LuaValue]]
      } else {
        return Future { arg }        
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
  class sleep extends OneArgFunction {
    /** LuaJ call override
     * 
     * @param arg LuaJ argument
     */
    override def call(arg: LuaValue): LuaValue = {
      Thread sleep arg.tolong
      return LuaValue.NONE
    }
  }

}
