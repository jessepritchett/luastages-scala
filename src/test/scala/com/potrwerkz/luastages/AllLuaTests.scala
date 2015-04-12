package com.potrwerkz.luastages

import java.io.File
import org.junit.{Test, BeforeClass, AfterClass}
import org.junit.Assert
import org.junit.Assert._
import org.luaj.vm2._
import org.luaj.vm2.lib.jse._

object AllLuaTests {
  var globals: Globals = null
  @BeforeClass def standUp() {
    globals = JsePlatform.standardGlobals
  }

  @AfterClass def tearDown() {
  }
}

class AllLuaTests {
  @Test def allLuaTests {
    for (file <- new File("src/test/resources/lua").listFiles.
         filter(_.getName.endsWith(".lua"))) {
      println("running: "+file.toString)
      val chunk = AllLuaTests.globals.loadfile(file.toString)
      chunk.call()
    }
  }

}
