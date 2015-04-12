package com.potrwerkz.luastages.luaext

import org.junit.Assert._
import org.junit.{Test, BeforeClass, AfterClass}
import org.luaj.vm2._
import org.luaj.vm2.lib.jse._

object stagesTest {
  var globals: Globals = null
  var stages: LuaValue = null

  @BeforeClass def standUp {
    globals = JsePlatform.standardGlobals
    Util.loadLib(globals, new stages)
    stages = globals.get("require").call("stages")
  }

  @AfterClass def tearDown {
  }
}

class stagesTest {
  val globals = stagesTest.globals
  val stages = stagesTest.stages

  @Test def tbd {
    fail("TBD")
  }
}
