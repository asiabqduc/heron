/**
 * 
 */
package net.brilliant.framework.async;

import java.util.GregorianCalendar;

import javax.inject.Inject;

import net.brilliant.framework.logging.LogService;
import net.brilliant.model.Context;

/**
 * @author bqduc_2
 *
 */
public abstract class Asynchronous /*implements Runnable*/ extends Thread {
	private boolean running = true;

	@Inject 
	protected LogService log;

	private Context executionContext;

	public Asynchronous() {
		this.executionContext = Context.builder().build();
	}

	@Override
	public void run() {
		log.info("Execute started at: " + GregorianCalendar.getInstance().getTime());
		executeAsync(Context.builder().build());
		log.info("Execute finishaed at: " + GregorianCalendar.getInstance().getTime());
	}

	protected abstract void executeAsync(Context executionContext);

	public Context getExecutionContext() {
		return executionContext;
	}

	public void setExecutionContext(Context executionContext) {
		this.executionContext = executionContext;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
