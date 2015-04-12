package com.potrwerkz.luastages.luaext

import org.junit.Assert
import org.luaj.vm2._
import org.luaj.vm2.lib._

object testing {
  def load(globals: Globals) {
  }
}

/** Set of assertion functions and other unit test functionality for Lua
 *
 * @constructor used by LuaJ to import the module
 * @param modname name of the module
 * @param env pointer to the calling Lua environment
**/
class testing extends TwoArgFunction {
  var env: LuaValue = null

  override def call(modname: LuaValue, env: LuaValue): LuaValue = {
    this.env = env

    val library = LuaValue.tableOf
    library.set("assertEquals", new assertEquals)
    library.set("assertFalse", new assertFalse)
    library.set("assertNotNil", new assertNotNil)
    library.set("assertNil", new assertNil)
    library.set("assertTrue", new assertTrue)
    library.set("fail", new fail)
    return library
  }

  /** Asserts equality between two Lua values
   *
   * Note: in Lua, objects are only equal to one another
   * when they refer to the same object. Objects include: 
   *  - tables
   *  - functions
   *  - threads
   *  - full userdata
   *
   * Lua usage:
   *  - assertEquals("one", "two")
   *  - assertEquals(2.01, 1.99, 0.2)
   *  - assertEquals("message", true, "false")
   *  - assertEquals("message", 1.11, 11, 0.01)
   *  - t1 = {}; t2 = {}; assertEquals(t1, t2)
   *  - t1 = {}; t2 = {}; assertEquals("message", t1, t2)
  **/
  class assertEquals extends VarArgFunction {
    /** LuaJ invoke override 
     *
     *  @param args Lua parameter list
     *
    **/
    override def invoke(args: Varargs): LuaValue = {
      val arg1 = args.arg(1)
      val arg2 = args.arg(2)
      val arg3 = args.arg(3)
      val arg4 = args.arg(4)

      if (args.narg == 0) {
        // assertEquals() = fail()
        return new fail().call
      
      } else if (args.narg == 1) {
        // assertEquals(singleArg) = assertNotNil(singleArg)
        return new assertNotNil().call(arg1)
      
      } else if (args.narg == 2) {
        // standard two-value use-case
        Assert.assertTrue(arg1.eq_b(arg2))

      } else {

        if (arg1.isnumber && arg2.isnumber && arg3.isnumber) {
          // assume three numbers is delta archetype
          Assert.assertEquals(arg1.todouble, arg2.todouble, arg3.todouble)
        } else if (arg2.isnumber && arg3.isnumber && arg4.isnumber) {
          // handle (msg, num1, num2, delta)
          Assert.assertEquals(arg1.tojstring, arg2.todouble, arg3.todouble,
            arg4.todouble)
        } else {
          // otherwise, assume (msg, arg1, arg2)
          Assert.assertTrue(arg1.tojstring, arg2.eq_b(arg3))
        }
      }
      
      return LuaValue.NONE
    }
  }

  /** Asserts that a given value is false
   *
   * Lua usage:
   *  - assertFalse(true)
   *  - assertFalse("message", true)
  **/
  class assertFalse extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
    **/
    override def invoke(args: Varargs): LuaValue = {
      // check for message
      if (args.narg > 1) {
        Assert.assertFalse(args.arg1.tojstring, args.arg(2).toboolean)
      } else {
        Assert.assertFalse(args.arg1.toboolean)
      }

      return LuaValue.NONE
    }
  }

  /** Asserts that a given value is not nil
   *
   * Lua usage:
   *  - assertNotNil()
   *  - assertNotNil(nil)
   *  - assertNotNil("message", nil)
  **/
  class assertNotNil extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
    **/
    override def invoke(args: Varargs): LuaValue = {
      if (args.narg > 1) {
        Assert.assertFalse(args.arg1.tojstring, args.arg(2).isnil)
      } else {
        Assert.assertFalse(args.arg1.isnil)
      }

      return LuaValue.NONE
    }
  }

  /** Asserts that a given value is nil
   *
   * Lua usage:
   *  - assertNil(2.1)
   *  - assertNil(true)
   *  - assertNil("test")
   *  - assertNil("message", "test")
  **/
  class assertNil extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
    **/
    override def invoke(args: Varargs): LuaValue = {
      if (args.narg > 1) {
        Assert.assertTrue(args.arg1.tojstring, args.arg(2).isnil)
      } else {
        Assert.assertTrue(args.arg1.isnil)
      }

      return LuaValue.NONE
    }
  }

  /** Asserts that a given value is true
   *
   * Lua usage:
   *  - assertTrue()
   *  - assertTrue(false)
   *  - assertTrue("message", false)
  **/
  class assertTrue extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
    **/
    override def invoke(args: Varargs): LuaValue = {
      // check for message
      if (args.narg > 1) {
        Assert.assertTrue(args.arg1.tojstring, args.arg(2).toboolean)
      } else {
        Assert.assertTrue(args.arg1.toboolean)
      }

      return LuaValue.NONE
    }
  }

  /** Fail a test case, as in "assertTrue(false)"
   *
   * Lua usage:
   *  - fail()
   *  - fail('message')
  **/
  class fail extends OneArgFunction {
    /** LuaJ call override
     *
     *  @param arg Lua argument
    **/
    override def call(arg: LuaValue): LuaValue = {
      Assert.fail(arg.tojstring)

      // this is unreachable, but necessory to compile
      return LuaValue.NONE
    }
  }
}
