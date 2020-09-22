package cn.edu.fudan.se.multidependency.service.insert.clone;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.edu.fudan.se.multidependency.service.insert.ExtractorForNodesAndRelationsImpl;

public abstract class CloneInserter extends ExtractorForNodesAndRelationsImpl {
	
	protected static long cloneGroupNumber = 0;
	
	private CountDownLatch latch;

	public CloneInserter() {
		super();
		this.latch = new CountDownLatch(3);
	}
	
	private static final Executor executor = Executors.newCachedThreadPool();
	
	protected abstract void readMeasureIndex() throws Exception;
	
	protected abstract void readResult() throws Exception;
	
//	protected abstract void readGroup() throws Exception;
	
	protected abstract void extractNodesAndRelations() throws Exception;
	
	@Override
	public void addNodesAndRelations() throws Exception {
		processFile();
		extractNodesAndRelations();
	}

	private void processFile() throws Exception {
		executor.execute(() -> {
			try {
				readMeasureIndex();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
		executor.execute(() -> {
			try {
				readResult();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				latch.countDown();
			}
		});
//		executor.execute(() -> {
//			try {
//				readGroup();
//			} catch (Exception e) {
//				e.printStackTrace();
//			} finally {
//				latch.countDown();
//			}
//		});
		latch.await();
	}
}
