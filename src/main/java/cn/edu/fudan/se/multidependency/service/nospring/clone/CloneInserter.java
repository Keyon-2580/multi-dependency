package cn.edu.fudan.se.multidependency.service.nospring.clone;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import cn.edu.fudan.se.multidependency.service.nospring.ExtractorForNodesAndRelationsImpl;

public abstract class CloneInserter extends ExtractorForNodesAndRelationsImpl {
	
	protected static long cloneGroupNumber = 0;
	
	private CountDownLatch latch;

	public CloneInserter() {
		super();
		this.latch = new CountDownLatch(2);
	}
	
	private static final Executor executor = Executors.newCachedThreadPool();
	
	protected abstract void readMeasureIndex() throws Exception;
	
	protected abstract void readResult() throws Exception;
	
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
		latch.await();
	}
}
