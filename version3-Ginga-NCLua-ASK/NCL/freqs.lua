local APP
local INPUT = { 1, 17, 33, 49, 65, 81}
local number = {}
local size = 16
local flagSync = false

local END = false
local value_f1, value_f2, value_f3, value_f4, value_f5, value_f6, value_f7, value_f8

function bin (v)
  for size=size, 1,-1 do
	number[size] = bit32.band(bit32.rshift (v,size-1), 1)
	end
end

function resume()
	assert(coroutine.resume(APP))
end

function silence(evt)
	fq1_0(event)
	fq2_0(event)
	fq3_0(event)
	fq4_0(event)
	fq5_0(event)
	fq6_0(event)
	fq7_0(event)
	fq8_0(event)
end

-- for i=1, #INPUT,1 do
-- 	bin(INPUT[i])
-- 	for t=1,size,1 do
-- 		print("num:"..INPUT[i].." "..number[t])
-- 	end
-- end

function hdlr_enter (evt)
    if END then return end
	if evt.class ~= 'key'   then return end
	if evt.type  ~= 'press' then return end
	if evt.key == 'ENTER' then
		print("Enter pressed")
		assert(coroutine.resume(APP))
	end
end
function hdlr_green (evt)
    if END then return end
	if evt.class ~= 'key'   then return end
	if evt.type  ~= 'press' then return end
	if evt.key == 'GREEN' then
		print("GREEN pressed")
		assert(coroutine.resume(APP))
	end
end

function sleep(n)
    os.execute("sleep " .. tonumber(n))
end

function fq1_1 (evt)
	value_f1 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f1',
    	action   = 'start',
    	value    = value_f1,
	}
	print("Sent f1_1")
	print(os.date("Time %X"))
end
function fq1_0 (evt)
	value_f1 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f1',
    	action   = 'start',
    	value    = value_f1,
	}
	print("Sent f1_0")
end

function fq2_1 (evt)
	value_f2 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f2',
    	action   = 'start',
    	value    = value_f2,
	}
	print("Sent f2_1")
end
function fq2_0 (evt)
	value_f2 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f2',
    	action   = 'start',
    	value    = value_f2,
	}
	print("Sent f2_0")
end

function fq3_1 (evt)
	value_f3 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f3',
    	action   = 'start',
    	value    = value_f3,
	}
	print("Sent f3_1")
end
function fq3_0 (evt)
	value_f3 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f3',
    	action   = 'start',
    	value    = value_f3,
	}
	print("Sent f3_0")
end

function fq4_1 (evt)
	value_f4 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f4',
    	action   = 'start',
    	value    = value_f4,
	}
	print("Sent f4_1")
end
function fq4_0 (evt)
	value_f4 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f4',
    	action   = 'start',
    	value    = value_f4,
	}
	print("Sent f4_0")
end

function fq5_1 (evt)
	value_f5 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f5',
    	action   = 'start',
    	value    = value_f5,
	}
	print("Sent f5_1")
end
function fq5_0 (evt)
	value_f5 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f5',
    	action   = 'start',
    	value    = value_f5,
	}
	print("Sent f5_0")
end

function fq6_1 (evt)
	value_f6 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f6',
    	action   = 'start',
    	value    = value_f6,
	}
	print("Sent f6_1")
end
function fq6_0 (evt)
	value_f6 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f6',
    	action   = 'start',
    	value    = value_f6,
	}
	print("Sent f6_0")
end

function fq7_1 (evt)
	value_f7 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f7',
    	action   = 'start',
    	value    = value_f7,
	}
	print("Sent f7_1")
end
function fq7_0 (evt)
	value_f7 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f7',
    	action   = 'start',
    	value    = value_f7,
	}
	print("Sent f7_0")
end

function fq8_1 (evt)
	value_f8 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f8',
    	action   = 'start',
    	value    = value_f8,
	}
	print("Sent f8_1")
end
function fq8_0 (evt)
	value_f8 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f8',
    	action   = 'start',
    	value    = value_f8,
	}
	print("Sent f8_0")
end

function fq13_1 (evt)
	value_f13 = 1
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f13',
    	action   = 'start',
    	value    = value_f13,
	}
	print("Sent f13_1")
end
function fq13_0 (evt)
	value_f13 = 0
	event.post{
		class    = 'ncl',
    	type     = 'attribution',
    	name     = 'f13',
    	action   = 'start',
    	value    = value_f13,
	}
	print("Sent f13_0")
end

function generateHamming(msg) -- Generates a Hamming code according to the number of data bits
	r = 0
	m = string.len(msg)
	-- calculate number of parity bits needed using m+r+1<=2^r
	while true do
		if (m + r <= math.pow (2, r)) then
          break
		end
	  r = r + 1
	end

	transLength = string.len(msg) + r
	temp = 0
	temp2 = 0
	j = 1

	transMsg = {}
	transMsg[transLength + 1] = 0

	for i = 1, transLength, 1 do
      temp2 = math.pow (2, temp);
	  if (i % temp2 ~= 0) then
		-- print("i "..i.." temp2 "..temp2.." i % temp2 "..(i % temp2))
		transMsg[i] = tonumber (string.sub(msg,j,j))
		j = j + 1
      else
		temp = temp + 1
	  end
	end

	for k=1, transLength+1, 1 do 
		if transMsg[k] == nil then
			transMsg[k] = 0
		end
	end
	
	for i = 0, r-1, 1 do
      smallStep = math.pow (2, i)
      bigStep = smallStep * 2
	  start = smallStep
	  checkPos = start

	  while (true) do -- Not sure if this section of code is correct
        	for k = start, start + smallStep-1, 1 do
            	checkPos = k
            	if (k > transLength) then
            		break
				end
        		transMsg[smallStep] = bit32.band((transMsg[smallStep]), (transMsg[checkPos]))
			end
			
        	if (checkPos > transLength) then
          		break
        	else
				start = start + bigStep;
			end
		  
        end
	end
	
	txt = ""
	for i = 1,transLength, 1 do
		txt = txt .. tonumber(transMsg[i])
	end
	print ("Hamming Encoded Message : " .. txt)

	return transMsg
end

function hammingBinToI (arr)
	sum = 0
	for i=1, 16,1 do
		sum = sum + math.pow(2,i) * arr[i] 
	end
	return sum
end

APP = coroutine.create(
function()   
	event.register(hdlr_green)
	coroutine.yield()      -- espera o ENTER

	while true do
		event.timer(1800,resume)
		coroutine.yield()
		
		for i=0, 2, 1 do
			if(i ~= 1) then
				fq13_1(event)
			else
				fq13_0(event)
			end
			event.timer(150,resume)
			coroutine.yield()	
		end

		for i=1, #INPUT,1 do
			bin(INPUT[i])

			strTransmission = ""
			for k=1, size, 1 do
				if (not(number[k] == 0 and string.len(strTransmission) == 0)) then
					strTransmission = strTransmission..tostring(number[k])
				end
			end

			transmission = generateHamming(string.sub(strTransmission,1,12))  -- Not sure if this function gives the correct result
			hammingVal = hammingBinToI(transmission)
			bin(hammingVal)
			print("Hamming value: " .. hammingVal.."\n")
			-- for k, v in pairs(number) do print(k, v) end


			for t=1,size,1 do
				if t == 1 then
					if number[t] == 1 then
						fq1_1(event)
					else
						fq1_0(event)
					end
				end
				if t == 2 then
					if number[t] == 1 then
						fq2_1(event)
					else
						fq2_0(event)
					end
				end
				if t == 3 then
					if number[t] == 1 then
						fq3_1(event)
					else
						fq3_0(event)
					end
				end
				if t == 4 then
					if number[t] == 1 then
						fq4_1(event)
					else
						fq4_0(event)
					end
				end
				if t == 5 then
					if number[t] == 1 then
						fq5_1(event)
					else
						fq5_0(event)
					end
				end
				if t == 6 then
					if number[t] == 1 then
						fq6_1(event)
					else
						fq6_0(event)
					end
				end
				if t == 7 then
					if number[t] == 1 then
						fq7_1(event)
					else
						fq7_0(event)
					end
				end
				if t == 8 then
					if number[t] == 1 then
						fq8_1(event)
					else
						fq8_0(event)
					end
				end
			end
			event.timer(150,resume)
			coroutine.yield()
			silence(event)
			event.timer(300,resume)
			coroutine.yield()		
			print("Done number: "..i)
		end
		print("Loop")
		event.timer(1800,resume)
		coroutine.yield()
	end

    -- coroutine.yield()      -- espera o ENTER
    -- event.post('out',
    --     { class='ncl', type='presentation', action='stop' })

    END = true
end)

event.register(function(evt)
	if evt.class ~= 'ncl' then return end
	if (evt.type == 'presentation') and (evt.action == 'start') then
		assert(coroutine.resume(APP))
	end
end)