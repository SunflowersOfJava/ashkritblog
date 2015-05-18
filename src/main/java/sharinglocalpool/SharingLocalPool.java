/*
 * Copyright (c) 2013, OpenCloudDB/MyCAT and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software;Designed and Developed mainly by many Chinese 
 * opensource volunteers. you can redistribute it and/or modify it under the 
 * terms of the GNU General Public License version 2 only, as published by the
 * Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Any questions about this component can be directed to it's project Web address 
 * https://code.google.com/p/opencloudb/.
 *
 */
package sharinglocalpool;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author lcz
 */
public class SharingLocalPool<T> {
	public static final String LOCAL_BUF_THREAD_PREX = "$";
	private final ConcurrentLinkedQueue<T> items = new ConcurrentLinkedQueue<T>();
	private final int threadLocalMaxNum;
	
	private static ThreadLocalPool<?> localBufferPool;
	
	private volatile int allocateOpts;
	private volatile int recycleOpts;
	
	public int getAllocateOpts() {
		return allocateOpts;
	}

	public int getRecycleOpts() {
		return recycleOpts;
	}
	
	@SuppressWarnings("rawtypes")
	public static  ConcurrentHashMap<String,LocalQueue> threadLocalQueueMap = new ConcurrentHashMap<String,LocalQueue>();

	public SharingLocalPool(int threadLocalMaxNumber) {
		localBufferPool = new ThreadLocalPool<T>(threadLocalMaxNumber);
		this.threadLocalMaxNum = threadLocalMaxNumber;
	}

	private boolean isLocalCacheThread() {
		final String thname = Thread.currentThread().getName();
		return (thname.length() < LOCAL_BUF_THREAD_PREX.length()) ? false
				: (thname.charAt(0) == '$');

	}

	public int size() {
		return this.items.size();
	}

	public T allocate() {
		T buffer = null;
		if (isLocalCacheThread()) {
			@SuppressWarnings("unchecked")
			LocalQueue<T> localQueue = (LocalQueue<T>) localBufferPool.get();
			buffer = localQueue.poll();
			if (buffer != null) {
				return buffer;
			}
		}
		buffer = items.poll();
		allocateOpts++;
		return buffer;
	}


	public void recycle(T buffer) {
		if (isLocalCacheThread()) {
			@SuppressWarnings("unchecked")
			LocalQueue<T> localQueue = (LocalQueue<T>) localBufferPool.get();
			if (localQueue.size() < threadLocalMaxNum) {
				localQueue.offer(buffer);
			} else {
				// recyle 3/4 thread local buffer
				items.addAll(localQueue.removeItems(threadLocalMaxNum * 3 / 4));
				items.offer(buffer);
			}
		} else {
			recycleOpts++;
			items.offer(buffer);
		}
	}
}
