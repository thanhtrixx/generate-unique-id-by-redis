local keyPrefix = KEYS[1]
-- if(keyPrefix == nil)
-- then
--     return nil;
-- end
local increaseNumber = KEYS[2]
local timeSlot = 86400
local safeTimeSlot = 3600
local increaseByTimeZone = 28800 -- GMT+8
-- if(increaseNumber == nil)
-- then
--     return nil;
-- end
local time = tonumber(redis.call('TIME')[1]) + increaseByTimeZone
local dateFrom1970 = math.floor(time / timeSlot)
local expireSeconds = 86400 - (time % timeSlot) + safeTimeSlot
local key = keyPrefix .. ':' .. dateFrom1970
local currentCounter = redis.call('INCRBY', key, increaseNumber)
redis.call('EXPIRE', key, expireSeconds)
return {time, currentCounter}
