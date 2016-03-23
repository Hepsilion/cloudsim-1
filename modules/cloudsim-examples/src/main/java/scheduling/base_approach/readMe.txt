Control methods:
1. Base Methods:
SchedulingConstants.DefautFrequency=5;
SchedulingConstants.ENABLE_DVFS=false;
(1)使用FF时，运行FFExample.java;
(2)使用MBFD时，运行MBFDExample.java

2. Ours Methods:
SchedulingConstants.DefautFrequency=1;
SchedulingConstants.ENABLE_DVFS=true;
(1)使用FF做initial时，SchedulingConstants.our_initial_vmAllocationPolicy_method="FF";
(2)使用MBFD做initial时，SchedulingConstants.our_initial_vmAllocationPolicy_method="MBFD";
然后运行SchedulingMain3.java

