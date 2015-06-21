package com.potrwerkz.luastages

import org.luaj.vm2._
import org.luaj.vm2.lib._
import org.luaj.vm2.lib.jse.JsePlatform
import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object Core {
  // constants
  val RECEIVE_FN = "receive"
  
  class LuaActor(actorMetatable: LuaValue, behavior:LuaValue) extends Actor {
    def receive = {
      case msg: LuaValue => processMsg(Core.RECEIVE_FN, msg)
      case _ => // ignore other messages
    }
  
    def processMsg(action: String, msg: LuaValue) {
      // DEBUG
      // println("msg: "+msg)
      
      // pass message to given action method
      val proc = behavior.get(action)
      
      // DEBUG
      // println("proc: "+proc)
      
      // need to convert sender ref to stages.actor LuaValue  
      val luaSender = LuaValue.userdataOf(sender)
      luaSender.setmetatable(actorMetatable)
     
      var res = LuaValue.NONE
      
      if (proc != LuaValue.NIL) {
        res = proc.call(behavior, luaSender, msg) 
      }
      
      sender() ! res
    }
  }
}

class Core(namespace: String) {
  // create actor system for this core module
  val system = ActorSystem(namespace)
  
  def Spawn(amt: LuaValue, bhv: LuaValue, name: String): ActorRef = {
    name match {
      case "" => return system.actorOf(Props(classOf[Core.LuaActor], amt, bhv))
      case _ => return system.actorOf(Props(classOf[Core.LuaActor], amt, bhv), name)
    }
  }
}