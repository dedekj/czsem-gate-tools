package czsem.utils;


public class FirstOfTwoTasksKillsTheSecond<ReturnType>
{
	public static enum HandShakeResult
	{
		HandShakeOK,
		HandShakeKO,
		ProcessTerminated,
		TimeOut
	}

	public static interface Task<ReturnType>
	{
		ReturnType run() throws Exception;
	}

	@SuppressWarnings("unchecked")
	protected Task<ReturnType> [] tasks = new Task[2];
	
	protected Thread threads[] = new Thread[2];
	protected ReturnType ret = null;
	protected Exception exception = null;
	
	public FirstOfTwoTasksKillsTheSecond(Task<ReturnType> task1, Task<ReturnType> task2)
	{
		this.tasks[0] = task1; 
		this.tasks[1] = task2; 
	}
	
	protected void runTask(int num) 
	{
		try {
			ret = tasks[num].run();
			threads[(1+num) % 2].interrupt();
		} catch (InterruptedException e) {
//			e.printStackTrace();
		} catch (Exception e) {
			exception = e;
			threads[(1+num) % 2].interrupt();
		}
	}
	
	public ReturnType execute() throws Exception
	{
		threads[0] = Thread.currentThread();
		threads[1] = new Thread()
		{
			@Override
			public void run()
			{
				runTask(1);
			}			
		};
		
		threads[1].start();
		runTask(0);
		
		if (exception != null) throw exception;
		
		return ret;
	}
	
	 
	/**
	 * @param timeout == 0 means no timeout is used - wait infinitely.
	 */
	public ReturnType executeWithTimeout(final int timeout) throws Exception
	{
		if (timeout == 0) return execute();


		Thread t = new Thread()
		{
			@Override
			public void run()
			{
				try {
					Thread.sleep(timeout);
					threads[1].interrupt();
					threads[0].interrupt();
				} catch (InterruptedException e) {}
			}			
		};
		
		t.start();
		
		ReturnType r = execute();
		t.interrupt();
		
		return r;
	}


	public static void main(String[] args) throws Exception
	{
		Task<Integer> task1 = new Task<Integer>() {
			@Override
			public Integer run() throws InterruptedException {
				Thread.sleep(4000);
				return 111; 
			}
		};
		Task<Integer> task2 = new Task<Integer>() {
			@Override
			public Integer run() throws InterruptedException {
				Thread.sleep(3000);
				return 222; 
			}
		};
		
		FirstOfTwoTasksKillsTheSecond<Integer> tt = new FirstOfTwoTasksKillsTheSecond<Integer>(task1, task2); 
		System.err.println(
				tt.executeWithTimeout(500));

	}

}