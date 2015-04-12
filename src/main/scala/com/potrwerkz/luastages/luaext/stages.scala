package com.potrwerkz.luastages.luaext

import org.luaj.vm2._
import org.luaj.vm2.lib._

/** Lua wrapper for Akka actors
 *
 * @constructor used by LuaJ to import the module
 * @param modname name of the module
 * @param env pointer to the calling Lua environment
**/
class stages extends TwoArgFunction {
  var env: LuaValue = null

  override def call(modname: LuaValue, env: LuaValue): LuaValue = {
    this.env = env

    val library = LuaValue.tableOf
    return library
  }
}
