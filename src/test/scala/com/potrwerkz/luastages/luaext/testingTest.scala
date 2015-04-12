package com.potrwerkz.luastages.luaext

import org.junit.Assert._
import org.junit.{Test, BeforeClass, AfterClass}
import org.luaj.vm2._
import org.luaj.vm2.lib.jse._
import java.lang.AssertionError

object testingTest {
  var globals: Globals = null
  var testing: LuaValue = null

  @BeforeClass def standUp {
    globals = JsePlatform.standardGlobals
   
    Util.loadLib(globals, new testing)
    testing = globals.get("require").call("testing")
  }

  @AfterClass def tearDown {
  }
}

class testingTest {
  val globals = testingTest.globals
  val testing = testingTest.testing

  class TestFunc extends LuaFunction {}

  def testUserdata: LuaUserdata = {
    return LuaValue.userdataOf(new Object)
  }

  def testThread: LuaThread = {
    return new LuaThread(globals, new TestFunc)
  }

  def expectFail(f: () => Any, msg: String = null) {
    var failed = false

    try {
      f()
    } catch {
      case ae: AssertionError => {
        if (msg != null) {
          assertNotNull("message null", ae.getMessage())
          assertTrue("message not found", ae.getMessage().contains(msg))
        }
        failed = true
      }
    }

    assertTrue("failure expected", failed)
  }

  @Test def testAssertEquals {
    val luaFunc = testing.get("assertEquals")

    // these should pass
    luaFunc.call(LuaValue.NIL, LuaValue.NIL)
    luaFunc.call(LuaValue.FALSE, LuaValue.FALSE)
    luaFunc.call(LuaValue.valueOf(2.1), LuaValue.valueOf(2.1))
    luaFunc.call(LuaValue.valueOf(1.01), 
                 LuaValue.valueOf(0.99),
                 LuaValue.valueOf(0.2))
    luaFunc.call(LuaValue.valueOf("test"), LuaValue.valueOf("test"))

    val tf = new TestFunc
    luaFunc.call(tf, tf)
    
    val tud = testUserdata
    luaFunc.call(tud, tud)

    val tth = testThread
    luaFunc.call(tth, tth)

    val tt = LuaValue.tableOf
    luaFunc.call(tt, tt)
    
    // these should fail
    var f = () => luaFunc.call
    expectFail(f)
      
    f = () => luaFunc.call(LuaValue.NIL, LuaValue.FALSE)
    expectFail(f)
    
    f = () => luaFunc.call(LuaValue.FALSE, LuaValue.TRUE)
    expectFail(f)
    
    f = () => luaFunc.call(LuaValue.valueOf(2.1), LuaValue.valueOf(2.0))
    expectFail(f)
    
    f = () => luaFunc.call(LuaValue.valueOf(1.01), 
      LuaValue.valueOf(0.99),
      LuaValue.valueOf(0.001))
    expectFail(f)
    
    f = () => luaFunc.call(LuaValue.valueOf("test1"), 
      LuaValue.valueOf("test2"))
    expectFail(f)

    f = () => luaFunc.call(new TestFunc, new TestFunc)
    expectFail(f)

    f = () => luaFunc.call(testUserdata, testUserdata)
    expectFail(f)
   
    f = () => luaFunc.call(testThread, testThread)
    expectFail(f)

    f = () => luaFunc.call(LuaValue.tableOf, LuaValue.tableOf)
    expectFail(f)

    // test message
    f = () => luaFunc.call(LuaValue.valueOf("testMsg"), 
      LuaValue.valueOf("test1"), LuaValue.valueOf(12.3))
    expectFail(f, "testMsg")
  }

  @Test def testAssertFalse {
    val luaFunc = testing.get("assertFalse")

    // these should pass
    luaFunc.call
    luaFunc.call(LuaValue.NIL)
    luaFunc.call(LuaValue.FALSE)

    // these should fail
    expectFail(() => luaFunc.call(LuaValue.valueOf(2.1)))
    expectFail(() => luaFunc.call(LuaValue.valueOf("test")))
    expectFail(() => luaFunc.call(new TestFunc))
    expectFail(() => luaFunc.call(testUserdata))
    expectFail(() => luaFunc.call(testThread))
    expectFail(() => luaFunc.call(LuaValue.tableOf))
    
    // test message
    expectFail(() => luaFunc.call(LuaValue.valueOf("testMsg"), 
      LuaValue.TRUE), "testMsg")
  }

  @Test def testAssertNotNil {
    val luaFunc = testing.get("assertNotNil")
    
    // these should pass
    luaFunc.call(LuaValue.FALSE)
    luaFunc.call(LuaValue.ZERO)
    luaFunc.call(LuaValue.EMPTYSTRING)
    luaFunc.call(new TestFunc)
    luaFunc.call(testUserdata)
    luaFunc.call(testThread)
    luaFunc.call(LuaValue.tableOf)

    // these should fail
    expectFail(() => luaFunc.call)
    expectFail(() => luaFunc.call(LuaValue.NIL))

    // test message
    expectFail(() => luaFunc.call(LuaValue.valueOf("testMsg"), 
      LuaValue.NIL), "testMsg")
  }
  
  @Test def testAssertNil {
    val luaFunc = testing.get("assertNil")

    // these should pass
    luaFunc.call
    luaFunc.call(LuaValue.NIL)

    // these should fail
    expectFail(() => luaFunc.call(LuaValue.FALSE))
    expectFail(() => luaFunc.call(LuaValue.ZERO))
    expectFail(() => luaFunc.call(LuaValue.EMPTYSTRING))
    expectFail(() => luaFunc.call(new TestFunc))
    expectFail(() => luaFunc.call(testUserdata))
    expectFail(() => luaFunc.call(testThread))
    expectFail(() => luaFunc.call(LuaValue.tableOf))

    // test message
    expectFail(() => luaFunc.call(LuaValue.valueOf("testMsg"),
      LuaValue.FALSE), "testMsg")
  }
  
  @Test def testAssertTrue {
    val luaFunc = testing.get("assertTrue")

    // these should pass
    luaFunc.call(LuaValue.TRUE)
    luaFunc.call(LuaValue.ZERO)
    luaFunc.call(LuaValue.EMPTYSTRING)
    luaFunc.call(new TestFunc)
    luaFunc.call(testUserdata)
    luaFunc.call(testThread)
    luaFunc.call(LuaValue.tableOf)

    // these should fail
    expectFail(() => luaFunc.call)
    expectFail(() => luaFunc.call(LuaValue.NIL))
    expectFail(() => luaFunc.call(LuaValue.FALSE))

    // test message
    expectFail(() => luaFunc.call(LuaValue.valueOf("testMsg"),
      LuaValue.NIL), "testMsg")
  }

  @Test def testFail {
    val luaFunc = testing.get("fail")

    // these should fail
    expectFail(() => luaFunc.call)
    expectFail(() => luaFunc.call("testMsg"), "testMsg")
  }
}
