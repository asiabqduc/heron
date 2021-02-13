/**
 * 
 */
package net.brilliant.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import net.brilliant.base.JobSchedulerBase;

/**
 * @author ducbq
 *
 */
public class SyncPOSServicesJob extends JobSchedulerBase {
  protected void executing(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    super.reportScheduleStatus(this.getClass().getSimpleName());
  }
}
