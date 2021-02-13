/**
 * 
 */
package net.brilliant.base;

import java.util.Calendar;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import lombok.extern.slf4j.Slf4j;
import net.brilliant.common.CommonUtility;
import net.brilliant.model.ExecutionStatus;

/**
 * @author ducbq
 *
 */
@Slf4j
public abstract class JobSchedulerBase implements Job {
  protected abstract void executing(JobExecutionContext jobExecutionContext) throws JobExecutionException; 

  @Override
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    executing(jobExecutionContext);
  }

  private ExecutionStatus handleScheduleStatus(String message){
    log.info(message + " fired at: " + Calendar.getInstance().getTime());
    return ExecutionStatus.SUCCESS;
  }

  protected ExecutionStatus reportScheduleStatus(String message){
    return handleScheduleStatus(message);
  }

  protected ExecutionStatus reportScheduleStatus(Throwable throwable){
    String message = CommonUtility.getStackTrace(throwable);
    return handleScheduleStatus(message);
  }
}