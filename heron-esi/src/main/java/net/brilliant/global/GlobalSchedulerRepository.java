/**
 * 
 */
package net.brilliant.global;

import java.util.List;

import javax.inject.Inject;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import net.brilliant.component.JobSchedulingServicesRepository;
import net.brilliant.domain.TriggerDescriptor;

/**
 * @author ducbq
 *
 */
@Slf4j
@Component
public class GlobalSchedulerRepository {
  @Inject
  private JobSchedulingServicesRepository jobSchedulingServicesRepository;

  private void startScheduleEngine() throws SchedulerException, ClassNotFoundException {
    log.info("Enter startScheduleEngine()");
    SchedulerFactory schedulerFactory = new StdSchedulerFactory();
    Scheduler scheduler = schedulerFactory.getScheduler();

    List<TriggerDescriptor> triggerDescriptors = jobSchedulingServicesRepository.getDefaultTriggerDescriptors(SchedulingConstants.defaultGroup);
    for (TriggerDescriptor triggerDescriptor :triggerDescriptors){
      scheduler.scheduleJob(triggerDescriptor.getJobDetail(), triggerDescriptor.buildTrigger());
    }

    scheduler.start();
    log.info("Leave startScheduleEngine()");
  }

  public void start() throws SchedulerException, ClassNotFoundException {
    this.startScheduleEngine();
  }
}
