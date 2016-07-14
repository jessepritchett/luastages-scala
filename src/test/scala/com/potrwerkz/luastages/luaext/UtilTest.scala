package com.potrwerkz.luastages.luaext

import com.potrwerkz.luastages.Util
import org.junit.Assert._
import org.junit.{AfterClass, BeforeClass, Test}
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

  object TestLib extends TwoArgFunction {
    override def call(modname: LuaValue, env: LuaValue): LuaValue = {
      UtilTest.testLib
    }
  }

  @Test def testLoadLib {
    Util.loadLib(globals, TestLib)
    assertEquals(globals.get("require").call("TestLib"), UtilTest.testLib)
  }
}
