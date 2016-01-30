1. Base approach
修改SchedulingConstants
DefautFrequency = 5;    
ENABLE_DVFS = false; 
2. Our approach
修改SchedulingConstants
DefautFrequency = 5;    
ENABLE_DVFS = false;


3. Example in paper
修改
1. SchedulingHelper.createVmList中int[] MIPSs
2. SchedulingHelper.createSchedulingCloudlet中int[] startTime和int[] execution_time