package com.potrwerkz.luastages

import org.luaj.vm2._
import org.luaj.vm2.lib._

/** Lua utility functions 
 *  
 */
object Util {
  /** Loads a library into the given global environment table
   *  the simple name of the class is used for the table key
   *  (thus avoiding importing libraries by full java classpath)
   * 
   * @param globals Luaj global environment
   * @param lib Luaj LibFunction library instance to load
   */
  def loadLib(globals: Globals, lib: LibFunction): LuaValue = {
    val name = lib.getClass.getSimpleName.replace("$", "")
    val loaded = globals.get("package").get("loaded")
    loaded.set(name, lib.call(LuaValue.valueOf(name), globals))
    loaded.get(name)
  }
}
