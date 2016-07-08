/****************************************************************************************/   
                                DVFS Methods
/****************************************************************************************/
(1) SchedulingConstants.ENABLE_DVFS=true;
    SchedulingConstants.DefautFrequency=1;
(2) 
Method 1: DVFS_FF 
          run DVFS_FF_Example.java
Method 2: DVFS_MBFD
          run DVFS_MBFD_Example.java
Method 3: MU
          run DVFS_MU_Example.java
Method 4: SWPMM
          run DVFS_SWPMM_Example.java
Method 5: Ours
    1) initialization algorithm: FF
          SchedulingConstants.our_initial_vmAllocationPolicy_method="FF";
          run SchedulingMain3.java
    2) initialization algorithm: MBFD
          SchedulingConstants.our_initial_vmAllocationPolicy_method="MBFD";
          run SchedulingMain3.java
/****************************************************************************************/   
                                Non DVFS Methods
/****************************************************************************************/
(1) SchedulingConstants.ENABLE_DVFS=false;
    SchedulingConstants.DefautFrequency=5;
(2)
Method 1: FF
          run FFExample.java 
Method 2: MBFD
          run MBFDExample.java