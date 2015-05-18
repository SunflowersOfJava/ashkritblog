package sharinglocalpool;



@SuppressWarnings("rawtypes")
public class ThreadLocalPool<T> extends ThreadLocal<LocalQueue> {
	private final int size;

	public ThreadLocalPool(int size) {
		this.size = size;
	}

	protected synchronized LocalQueue<T> initialValue() {
		LocalQueue<T> bufferQueue = new LocalQueue<T>(size);
		SharingLocalPool.threadLocalQueueMap.put(Thread.currentThread().getName(), bufferQueue);
		return bufferQueue;
	}
}
