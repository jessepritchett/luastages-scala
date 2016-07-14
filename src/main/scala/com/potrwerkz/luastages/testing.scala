package com.potrwerkz.luastages

import org.junit.Assert
import org.luaj.vm2._
import org.luaj.vm2.lib._

/** Set of assertion functions and other unit test functionality for Lua
 *
 */
object testing extends TwoArgFunction {

  /** LuaJ call override
   *
   * @param modname name of the module
   * @param env pointer to the calling Lua environment
   */
  override def call(modname: LuaValue, env: LuaValue): LuaValue = {
    val library = LuaValue.tableOf
    library.set("assertEquals", assertEquals)
    library.set("assertNotEquals", assertNotEquals)
    library.set("assertFalse", assertFalse)
    library.set("assertNotNil", assertNotNil)
    library.set("assertNil", assertNil)
    library.set("assertTrue", assertTrue)
    library.set("fail", fail)
    library
  }

  /* Asserts equality between two Lua values
   *
   * Note: in Lua, object references are only equal to one 
   * another when they refer to the same object.
   *  
   * Objects include: 
   *  - tables
   *  - functions
   *  - threads
   *  - full userdata
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertEquals("one", "two")
   *  - testing.assertEquals(2.1, 1.9, 0.02)
   *  - testing.assertEquals("message", true, "false")
   *  - testing.assertEquals("message", 1.11, 11, 0.01)
   *  - t1 = {}; t2 = {}; testing.assertEquals(t1, t2)
   *  - t1 = {}; t2 = {}; testing.assertEquals("message", t1, t2)
   */
  object assertEquals extends VarArgFunction {
    /** LuaJ invoke override 
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      val arg1 = args.arg(1)
      val arg2 = args.arg(2)
      val arg3 = args.arg(3)
      val arg4 = args.arg(4)

      if (args.narg == 0) {
        // assertEquals() = fail()
        return fail.call
      
      } else if (args.narg == 1) {
        // assertEquals(singleArg) = assertNotNil(singleArg)
        return assertNotNil.call(arg1)
      
      } else if (args.narg == 2) {
        // standard two-value use-case
        assertEquals(null, arg1, arg2)

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
          assertEquals(arg1.tojstring, arg2, arg3)
        }
      }
      
      LuaValue.NONE
    }
    
    def assertEquals(msg: String, arg1: LuaValue, arg2: LuaValue) {
      if (arg1.`type` != arg2.`type`) {
        Assert.fail("argument type mismatch: "+arg1.typename+" and "+arg2.typename)
      }
      
      arg1.typename match {
        case "nil" => // do nothing, as nil always equals nil
        case "boolean" => Assert.assertEquals(msg, arg1.toboolean, arg2.toboolean) 
        case "number" => Assert.assertEquals(msg, arg1.todouble, arg2.todouble, 0)
        case "string" => Assert.assertEquals(msg, arg1.tojstring, arg2.tojstring)
        case "table" => Assert.assertEquals(msg, arg1, arg2)
        case "function" => Assert.assertEquals(msg, arg1, arg2)
        case "userdata" => Assert.assertEquals(msg, arg1, arg2)
        case "thread" => Assert.assertEquals(msg, arg1, arg2)
      }
    }
  }
    
  
  /* Asserts inequality between two Lua values
   *
   * Note: in Lua, object references are only equal to one 
   * another when they refer to the same object.
   *  
   * Objects include: 
   *  - tables
   *  - functions
   *  - threads
   *  - full userdata
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertNotEquals("one", "one")
   *  - testing.assertNotEquals(2.01, 1.99, 0.2)
   *  - testing.assertNotEquals("message", true, true)
   *  - testing.assertNotEquals("message", 1.11, 1.11, 0.1)
   *  - t1 = {}; t2 = {}; testing.assertEquals(t1, t2)
   *  - t1 = {}; t2 = {}; testing.assertEquals("message", t1, t2)
   */
  object assertNotEquals extends VarArgFunction {
    /** LuaJ invoke override 
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      val arg1 = args.arg(1)
      val arg2 = args.arg(2)
      val arg3 = args.arg(3)
      val arg4 = args.arg(4)

      if (args.narg == 0) {
        // assertEquals() = fail()
        return fail.call
      
      } else if (args.narg == 1) {
        // assertNotEquals(singleArg) = assertNil(singleArg)
        return assertNil.call(arg1)
      
      } else if (args.narg == 2) {
        // standard two-value use-case
        assertNotEquals(null, arg1, arg2)

      } else {

        if (arg1.isnumber && arg2.isnumber && arg3.isnumber) {
          // assume three numbers is delta archetype
          Assert.assertNotEquals(arg1.todouble, arg2.todouble, arg3.todouble)
        } else if (arg2.isnumber && arg3.isnumber && arg4.isnumber) {
          // handle (msg, num1, num2, delta)
          Assert.assertNotEquals(arg1.tojstring, arg2.todouble, arg3.todouble,
            arg4.todouble)
        } else {
          // otherwise, assume (msg, arg1, arg2)
          assertNotEquals(arg1.tojstring, arg2, arg3)
        }
      }
      
      LuaValue.NONE
    }
    
    def assertNotEquals(msg: String, arg1: LuaValue, arg2: LuaValue) {
      if (arg1.`type` == arg2.`type`) {
        arg1.typename match {
          case "nil" => Assert.assertNotEquals(msg, LuaValue.NIL, LuaValue.NIL)
          case "boolean" => Assert.assertNotEquals(msg, arg1.toboolean, arg2.toboolean) 
          case "number" => Assert.assertNotEquals(msg, arg1.todouble, arg2.todouble, 0)
          case "string" => Assert.assertNotEquals(msg, arg1.tojstring, arg2.tojstring)
          case "table" => Assert.assertNotEquals(msg, arg1, arg2)
          case "function" => Assert.assertNotEquals(msg, arg1, arg2)
          case "userdata" => Assert.assertNotEquals(msg, arg1, arg2)
          case "thread" => Assert.assertNotEquals(msg, arg1, arg2)
        }
      }
    }
  }

  
  /* Asserts that a given value is false
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertFalse(true)
   *  - testing.assertFalse("message", true)
   */
  object assertFalse extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      // check for message
      if (args.narg > 1) {
        Assert.assertFalse(args.arg1.tojstring, args.arg(2).toboolean)
      } else {
        Assert.assertFalse(args.arg1.toboolean)
      }

      LuaValue.NONE
    }
  }

  /* Asserts that a given value is not nil
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertNotNil()
   *  - testing.assertNotNil(nil)
   *  - testing.assertNotNil("message", nil)
   */
  object assertNotNil extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      if (args.narg > 1) {
        Assert.assertFalse(args.arg1.tojstring, args.arg(2).isnil)
      } else {
        Assert.assertFalse(args.arg1.isnil)
      }

      LuaValue.NONE
    }
  }

  /* Asserts that a given value is nil
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertNil(2.1)
   *  - testing.assertNil(true)
   *  - testing.assertNil("test")
   *  - testing.assertNil("message", "test")
   */
  object assertNil extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      if (args.narg > 1) {
        Assert.assertTrue(args.arg1.tojstring, args.arg(2).isnil)
      } else {
        Assert.assertTrue(args.arg1.isnil)
      }

      LuaValue.NONE
    }
  }

  /* Asserts that a given value is true
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.assertTrue()
   *  - testing.assertTrue(false)
   *  - testing.assertTrue("message", false)
   */
  object assertTrue extends VarArgFunction {
    /** LuaJ invoke override
     *
     *  @param args Lua parameter list
     */
    override def invoke(args: Varargs): LuaValue = {
      // check for message
      if (args.narg > 1) {
        Assert.assertTrue(args.arg1.tojstring, args.arg(2).toboolean)
      } else {
        Assert.assertTrue(args.arg1.toboolean)
      }

      LuaValue.NONE
    }
  }

  /* Fail a test case, as in "assertTrue(false)"
   *
   * Lua usage:
   *  = local testing = require "testing"
   *  - testing.fail()
   *  - testing.fail('message')
   */
  object fail extends OneArgFunction {
    /** LuaJ call override
     *
     *  @param arg Lua argument
     */
    override def call(arg: LuaValue): LuaValue = {
      Assert.fail(arg.tojstring)

      // this is unreachable, but necessary to compile
      LuaValue.NONE
    }
  }
}
