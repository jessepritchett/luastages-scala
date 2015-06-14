package com.potrwerkz.luastages

import java.io.File
import org.junit.{Test, BeforeClass, AfterClass}
import org.luaj.vm2._
import org.luaj.vm2.lib.jse._

import com.potrwerkz.luastages.luaext.Util
import com.potrwerkz.luastages.luaext.stages
import com.potrwerkz.luastages.luaext.testing


object AllLuaTests {
  var globals: Globals = null
  var testing: LuaValue = null
  var stages: LuaValue = null
  
  @BeforeClass def standUp() {
    globals = JsePlatform.standardGlobals
    
    Util.loadLib(globals, new testing)
    Util.loadLib(globals, new stages("test"))
  }

  @AfterClass def tearDown() {
  }
}

class AllLuaTests {
  @Test def allLuaTests {
    for (file <- new File("src/test/resources/lua").listFiles.filter(_.getName.endsWith(".lua"))) {
      println("running: "+file.toString)
      val chunk = AllLuaTests.globals.loadfile(file.toString)
      chunk.call()
    }
  }

}
