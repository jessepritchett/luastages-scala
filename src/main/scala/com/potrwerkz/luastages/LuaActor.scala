package com.potrwerkz.luastages

import akka.actor.{Actor, ActorRef, Props}
import org.luaj.vm2.LuaValue

/**
  * Created by vyera on 7/13/2016.
  */
object LuaActor {
  // constants
  val ACTOR_SYSTEM_KEY = "as"
  val RECEIVE_FN = "receive"

  def props(actorMetatable: LuaValue, behavior: LuaValue) = Props(new LuaActor(actorMetatable, behavior))
}

class LuaActor(actorMetatable: LuaValue, behavior: LuaValue) extends Actor {
  override def receive = {
    case msg: LuaValue => processMsg(LuaActor.RECEIVE_FN, msg)
    case _ => // ignore other messages
  }

  def processMsg(action: String, msg: LuaValue) {
    // DEBUG
    // println("msg: "+msg)

    // pass message to given action method
    val proc = behavior.get(action)

    // DEBUG
    // println("proc: "+proc)

    if (!proc.isnil) {
      // need to convert sender ref to stages.actor LuaValue
      val luaSender = LuaValue.userdataOf(sender).setmetatable(actorMetatable)

      // execute the action
      val res = proc.call(behavior, luaSender, msg)

      // FIXME always send result?
      if (res != LuaValue.NONE) sender() ! res
    }
  }

  def spawn(amt: LuaValue, bhv: LuaValue, name: String) = {
    name match {
      case "" => context.actorOf(LuaActor.props(amt, bhv))
      case _ => context.actorOf(LuaActor.props(amt, bhv), name)
    }
  }
}

