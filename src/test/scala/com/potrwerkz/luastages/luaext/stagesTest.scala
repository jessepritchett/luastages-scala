package com.potrwerkz.luastages.luaext

import java.io.File

import com.potrwerkz.luastages.{Util, stages, testing}
import org.junit.{AfterClass, BeforeClass, Test}
import org.luaj.vm2._
import org.luaj.vm2.lib.jse._

object stagesTest {
  var globals: Globals = null

  @BeforeClass def standUp {
    globals = JsePlatform.standardGlobals

    Util.loadLib(globals, testing)
    Util.loadLib(globals, stages)
  }

  @AfterClass def tearDown {
  }
}

class stagesTest {
  import stagesTest.globals

  @Test def allLuaTests {
    for (file <- new File("src/test/resources/lua").listFiles.filter(_.getName.endsWith(".lua"))) {
      println("running: "+file.toString)
      globals.loadfile(file.toString).call
    }
  }
}
