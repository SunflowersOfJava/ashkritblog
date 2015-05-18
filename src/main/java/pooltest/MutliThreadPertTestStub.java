package pooltest;

import java.util.LinkedHashMap;
import java.util.concurrent.CountDownLatch;

public abstract class MutliThreadPertTestStub {
	public void executePerf(int threadnum,final int total,String name) {
        final CountDownLatch countDownLatch = new CountDownLatch(threadnum); 
        long t1 = System.currentTimeMillis(); 
        final int execTimes = total/threadnum;
        for(int i=0;i<threadnum;i++)
        {
        	new Thread("$_"+i){
    			public void run(){
    		        for(int i=0;i<execTimes;i++)
    				executeTask();
        			countDownLatch.countDown(); 
    			}
        	}.start();
        }
        try {  
            countDownLatch.await();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
        long t2 = System.currentTimeMillis(); 
        int tps = (int) (total/(t2 - t1));
        System.out.println(String.format("%20s  %2s %20s",name,threadnum,tps));
        LinkedHashMap<String,Integer> type2Tps =  stat.get(threadnum);
        if(type2Tps == null)
        {
        	type2Tps = new  LinkedHashMap<String,Integer>();
        	stat.put(threadnum, type2Tps);
        }
        //多次测试，取较大值
        Integer old = type2Tps.get(name);
        if(old != null)
        {
        	tps = old > tps?old:tps;
        }
    	type2Tps.put(name, tps);
	}
	//threadnum->(type->tps)
	public static LinkedHashMap<Integer,LinkedHashMap<String,Integer>> stat= new LinkedHashMap<Integer,LinkedHashMap<String,Integer>>();
	
	abstract protected void executeTask();
}
