package com.potrwerkz.luastages.luaext

import org.luaj.vm2._
import org.luaj.vm2.lib._

object Util {
  def loadLib(globals: Globals, lib: LibFunction) {
    val name = lib.getClass.getSimpleName
    val loaded = globals.get("package").get("loaded")
    loaded.set(name, lib.call(LuaValue.valueOf(name), globals))
  }
}
