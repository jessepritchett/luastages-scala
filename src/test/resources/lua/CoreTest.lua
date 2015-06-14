local stages = require 'stages'
local testing = require 'testing'

-- define test data
local count = 0

-- define test functions
local askTestFunc = function(res)
  count = count + 1
  testing.assertEquals('done', res)
end

-- define test tables
local TestActor = {
  receive = function(self, sender, msg)
    if type(msg) == 'number' then
     	testing.assertEquals(123, msg)
    else
      return string.gsub(msg, '?', '')
    end
  end
}

local DelayedActor = {
  receive = function(self, sender, msg)
  	if type(msg) == 'number' then
  		stages.sleep(msg)
  	end
  	
    return 'done?'
  end
}

-- test init
testDRef = stages.spawn(DelayedActor)


--- begin tests ---

-- test actor creation
testRef = stages.spawn(TestActor)
testing.assertNotNil(testRef)


-- test actor send (a.k.a. tell)
testRef:tell(123)


-- test actor send/future (a.k.a. ask)
testRes = testRef:ask('banana?')
testing.assertEquals('banana', testRes.result())


-- test actor future callback
testRes = testRef:ask('orange?')
testRes.ready(
	function(res)
		testing.assertEquals('orange', res)
	end
)





-- test future continuation
testRef:ask(testDRef:ask(1000)).ready(askTestFunc)

stages.sleep(100)
testing.assertEquals(0, count)

stages.sleep(1000)
testing.assertEquals(1, count)


-- test branching continuation
testRefA = stages.spawn(TestActor)
testRefB = stages.spawn(TestActor)
testRefC = stages.spawn(TestActor)

count = 0
testRes = testDRef:ask(1000)
testRefA:ask(testRes).ready(askTestFunc)
testRefB:ask(testRes).ready(askTestFunc)
testRefC:ask(testRes).ready(askTestFunc)

stages.sleep(100)
testing.assertEquals(0, count)

stages.sleep(1000)
testing.assertEquals(3, count)


-- test join continuation
count = 0
testRes = stages.join(testDRef:ask(200), testDRef:tell(500), testRef:ask('grapes?'), 'lemons')
testRes.ready(
  function(res)
    count = count + 1
    testing.assertEquals(res[1], 'done?')
    testing.assertEquals(res[2], nil)
    testing.assertEquals(res[3], 'grapes')
    testing.assertEquals(res[4], 'lemons')
  end
)

stages.sleep(100)
testing.assertEquals(0, count)

stages.sleep(300)
testing.assertEquals(1, count)