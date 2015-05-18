package pooltest;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import util.ParaUtil;

public class PoolTestMain {
	public static void main(String[] args) {
		try {
			String threadNumArray = "1-10,20,30,40,50,60,80,100,150,200,300,500";
			String strTotal = "50M";
			if (args.length >= 1) {
				threadNumArray = args[0];
				strTotal = args[1];
			}
			int total = ParaUtil.numWithKM(strTotal);
			List<Integer> list = ParaUtil.regStringNum(threadNumArray);
			for (Integer i : list) {
				testMutliThreadTask(i, total);//warm up first
				testMutliThreadTask(i, total);
			}
			printAsExcel(MutliThreadPertTestStub.stat);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("input format: [threadNumArray] [executeTimes]");
			System.out.println("such as: 1,2,3,4,5,6  100M");
			return;
		}
	}



	private static void printAsExcel(
			LinkedHashMap<Integer, LinkedHashMap<String, Integer>> s) {
		boolean isPrintHead = false;
		for (Entry<Integer, LinkedHashMap<String, Integer>> entry1 : s
				.entrySet()) {
			if (!isPrintHead) {
				StringBuilder head = new StringBuilder();
				head.append("threadnum").append('\t');
				for (String name : entry1.getValue().keySet()) {
					head.append(name).append('\t');
				}
				System.out.println(head.toString());
				isPrintHead = true;
			}
			StringBuilder row = new StringBuilder();
			row.append(entry1.getKey()).append('\t');
			for (Integer i : entry1.getValue().values()) {
				row.append(i).append('\t');
			}
			System.out.println(row.toString());
		}

	}

	private static void testMutliThreadTask(int threadnum, int total) {
		new MutliThreadPertTestStub() {
			final LockBufferPool pool = new LockBufferPool(1024 * 1024*2, 1024);

			@Override
			final protected void executeTask() {
				ByteBuffer buffer = pool.allocate();
				pool.recycle(buffer);
			}
		}.executePerf(threadnum, total, "SharingPoolByLock");

		new MutliThreadPertTestStub() {
			final SharingLocalBufferPool pool = new SharingLocalBufferPool(
					1024 * 1024*2, 1024, 10);

			@Override
			final protected void executeTask() {
				ByteBuffer buffer = pool.allocate();
				pool.recycle(buffer);
			}
		}.executePerf(threadnum, total, "Local+SharingPool");
		new MutliThreadPertTestStub() {
			final ConcurrentLinkedQueue<Integer> list = new ConcurrentLinkedQueue<Integer>();
			final Integer i = new Integer(0);

			@Override
			final protected void executeTask() {
				list.add(i);
				list.poll();
			}
		}.executePerf(threadnum, total, "ConcurrentLinkedQueue");
		new MutliThreadPertTestStub() {
			final ConcurrentLinkedDeque<Integer> list = new ConcurrentLinkedDeque<Integer>();
			final Integer i = new Integer(0);

			@Override
			final protected void executeTask() {
				list.add(i);
				list.poll();
			}
		}.executePerf(threadnum, total, "ConcurrentLinkedDeque");
		new MutliThreadPertTestStub() {
			final LinkedBlockingQueue<Integer> list = new LinkedBlockingQueue<Integer>();
			final Integer i = new Integer(0);

			@Override
			final protected void executeTask() {
				list.offer(i);
				try {
					list.take();
				} catch (InterruptedException e) {
				}
			}
		}.executePerf(threadnum, total, "LinkedBlockingQueue");
	}

}
