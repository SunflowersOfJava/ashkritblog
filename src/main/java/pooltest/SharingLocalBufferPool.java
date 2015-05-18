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
package pooltest;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import sharinglocalpool.SharingLocalPool;

/**
 * @author mycat
 */
public final class SharingLocalBufferPool {
//	private static final Logger LOGGER = Logger.getLogger(BufferPool.class);
	private final int chunkSize;
	private final SharingLocalPool<ByteBuffer> sharingAndLocalPool;
	public SharingLocalPool<ByteBuffer> getSharingAndLocalPool() {
		return sharingAndLocalPool;
	}



	public SharingLocalBufferPool(int bufferSize, int chunkSize, int threadLocalPercent) {
		this.chunkSize = chunkSize;
		int size = bufferSize / chunkSize;
		size = (bufferSize % chunkSize == 0) ? size : size + 1;
		int threadLocalMaxNumber = threadLocalPercent * size / 100;
		sharingAndLocalPool = new SharingLocalPool<ByteBuffer>(threadLocalMaxNumber);
		
		for (int i = 0; i < size; i++) {
			recycle(createDirectBuffer(chunkSize));
		}
//		LOGGER.info("total buffer:"+bufferSize+",every chunk bytes:"+this.chunkSize+",chunk number:"+size
//				+ ",every threadLocalMaxNumber:" + threadLocalMaxNumber);
	}

	public int getChunkSize() {
		return chunkSize;
	}

	/**
	 * 先从pool中取，取不到再创建
	 * @return
	 */
	public ByteBuffer allocate() {
		ByteBuffer buffer = sharingAndLocalPool.allocate();
		if (buffer == null) {
			buffer = this.createDirectBuffer(chunkSize);
		}
		return buffer;
	}
	
	private boolean checkValidBuffer(ByteBuffer buffer) {
		// 拒绝回收null和容量大于chunkSize的缓存
		if (buffer == null || !buffer.isDirect()) {
			return false;
		} else if (buffer.capacity() > chunkSize) {
//			LOGGER.warn("cant' recycle  a buffer large than my pool chunksize "
//					+ buffer.capacity());
			return false;
		}
		buffer.clear();
		return true;
	}

	/**
	 * 先检查
	 * @param buffer
	 */
	public void recycle(ByteBuffer buffer) {
		if (!checkValidBuffer(buffer)) {
			return;
		}
		sharingAndLocalPool.recycle(buffer);
	}

	private ByteBuffer createTempBuffer(int size) {
		return ByteBuffer.allocate(size);
	}

	private ByteBuffer createDirectBuffer(int size) {
		return ByteBuffer.allocateDirect(size);
	}

	public ByteBuffer allocate(int size) {
		if (size <= this.chunkSize) {
			return allocate();
		} else {
//			LOGGER.warn("allocate buffer size large than default chunksize:"
//					+ this.chunkSize + " he want " + size);
			return createTempBuffer(size);
		}
	}

	public static void main(String[] args) {
		SharingLocalBufferPool pool = new SharingLocalBufferPool(1024 * 5, 1024, 2);
		int i = 10;
		ArrayList<ByteBuffer> all = new ArrayList<ByteBuffer>();
		for (int j = 0; j <= i; j++) {
			all.add(pool.allocate());
		}
		for (ByteBuffer buf : all) {
			pool.recycle(buf);
		}
		System.out.println(pool.sharingAndLocalPool.size());
	}
}
