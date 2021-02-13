/**
 * 
 */
package net.brilliant.listener;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import net.brilliant.auth.helper.AuthDataDispatchRepositoryHelper;
import net.brilliant.framework.component.ComponentBase;
import net.brilliant.global.GlobalSchedulerRepository;

/**
 * @author ducbq
 *
 */
@Component
public class SystemEventManager extends ComponentBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7564662407839189753L;

	@Inject
	private AuthDataDispatchRepositoryHelper dataServiceDispatchHelper;
	//private DataServiceDispatchHelper dataServiceDispatchHelper;

  @Inject
  private GlobalSchedulerRepository globalSchedulerRepository;

  @EventListener(ApplicationReadyEvent.class)
	public void onApplicationReadyEventListener() {
		try {
			dataServiceDispatchHelper.dispatch();
			initSchedulers();
		} catch (Exception e) {
			log.error(e);
		}
		log.info("Leave onApplicationReadyEventListener");
	}

	private void initSchedulers() throws SchedulerException, ClassNotFoundException{
    log.info("Enter initSchedulers");
    globalSchedulerRepository.start();
    log.info("Leave initSchedulers");
	}
}
