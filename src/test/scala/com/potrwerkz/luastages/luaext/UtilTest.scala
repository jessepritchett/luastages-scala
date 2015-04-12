package com.potrwerkz.luastages.luaext

import org.junit.Assert._
import org.junit.{Test, BeforeClass, AfterClass}
import org.luaj.vm2._
import org.luaj.vm2.lib._
import org.luaj.vm2.lib.jse._

object UtilTest {
  var globals: Globals = null
  val testLib = LuaValue.tableOf

  @BeforeClass def standUp {
    globals = JsePlatform.standardGlobals
  }

  @AfterClass def tearDown {
  }
}

class UtilTest {
  val globals = UtilTest.globals

  class TestLib extends TwoArgFunction {
    override def call(modname: LuaValue, env: LuaValue): LuaValue = {
      return UtilTest.testLib
    }
  }

  @Test def testLoadLib {
    Util.loadLib(globals, new TestLib)
    assertEquals(globals.get("require").call("TestLib"), UtilTest.testLib)
  }
}
