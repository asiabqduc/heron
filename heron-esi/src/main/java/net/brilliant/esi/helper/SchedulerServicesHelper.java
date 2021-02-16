/**
 * 
 */
package net.brilliant.esi.helper;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;

import javax.inject.Inject;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.brilliant.ccs.GlobalSharedConstants;
import net.brilliant.common.BeanUtility;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.esi.common.SchedulingConstants;
import net.brilliant.esi.domain.JobDescriptor;
import net.brilliant.esi.domain.TriggerDescriptor;
import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.esi.domain.entity.SchedulePlan;
import net.brilliant.esi.service.JobScheduleService;
import net.brilliant.esi.service.SchedulePlanService;
import net.brilliant.model.Context;

/**
 * @author ducbq
 *
 */
@Slf4j
@Component
public class SchedulerServicesHelper {
  @Inject
  private JobScheduleService jobScheduleService;

  @Inject
  private SchedulePlanService schedulePlanService;

  private StdSchedulerFactory schedulerFactory;
  List<TriggerDescriptor> triggerDescriptors;

  @Setter @Getter
  private List<JobDescriptor> jobDescriptors;

  @Async
  private void startScheduleEngine(Context context) throws SchedulerException, ClassNotFoundException {
    log.info("Enter startScheduleEngine()");

    List<JobSchedule> jobSchedules = loadMasterSchedules(context);
    if (CommonUtility.isEmpty(jobSchedules)){
      log.info("There is no job schedule to be run. ");
      return;
    }

    this.schedulerFactory = new StdSchedulerFactory();
    Scheduler scheduler = this.schedulerFactory.getScheduler();

    JobDetail currentJobDetail = null;
    Class<? extends Job> jobScheduleClass = null;
    Trigger currentTrigger = null;
    for (JobSchedule jobSchedule :jobSchedules) {
      if (CommonUtility.isEmpty(jobSchedule.getJobClass()))
        continue;

      try {
        jobScheduleClass = (Class<? extends Job>)BeanUtility.getClass(jobSchedule.getJobClass());
      } catch (Exception e) {
        jobScheduleClass = null;
        log.error("An error occurred while get associated class from: " + jobSchedule.getJobClass());
      }

      if (null==jobScheduleClass)
        continue;

      currentTrigger = buildTrigger(jobSchedule.getName() + SchedulingConstants.specTrigger, jobSchedule.getCategory(), null, jobSchedule.getCronExpression());
      
      currentJobDetail = JobBuilder
          .newJob(jobScheduleClass)
          .withIdentity(jobSchedule.getName(), jobSchedule.getCategory())
          .build();

      scheduler.scheduleJob(currentJobDetail, currentTrigger);
    }

    scheduler.start();
    log.info("Leave startScheduleEngine()");
  }

  public void start(Context context) throws SchedulerException, ClassNotFoundException {
    this.startScheduleEngine(context);
  }

  public List<TriggerDescriptor> getTriggerDescriptors(String status) throws SchedulerException{
    if (CommonUtility.isNotEmpty(this.triggerDescriptors))
      return this.triggerDescriptors;

    if (null==this.triggerDescriptors){
      this.triggerDescriptors = CollectionsUtility.createDataList();
    }

    Scheduler scheduler = this.schedulerFactory.getScheduler();
    for (String groupName : scheduler.getJobGroupNames()) {
      for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
        String jobName = jobKey.getName();
        String jobGroup = jobKey.getGroup();
         //get job's trigger
        List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
        Date nextFireTime = triggers.get(0).getNextFireTime(); 
        System.out.println("[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime);
        for (Trigger trigger :triggers){
          triggerDescriptors.add(TriggerDescriptor.buildDescriptor(trigger));
        }
      }
     }
    return triggerDescriptors;
  }

  private List<JobSchedule> loadMasterSchedules(Context context){
    List<JobSchedule> loadedJobSchedules = CollectionsUtility.createDataList();
    List<SchedulePlan> loadedSchedulePlans = schedulePlanService.getObjects();

    JobSchedule jobSchedule = null;
    SchedulePlan schedulePlan = null;
    if (!loadedSchedulePlans.isEmpty()){
      schedulePlan = loadedSchedulePlans.get(0);
    } else if (context.containsKey(SchedulingConstants.CTX_SCHEDULE_PLANS)){
      schedulePlan = (SchedulePlan)context.get(SchedulingConstants.CTX_SCHEDULE_PLANS);

      if (null != schedulePlan && !schedulePlanService.exists(GlobalSharedConstants.PROP_CODE, schedulePlan.getCode())){
        schedulePlanService.save(schedulePlan);
      }
    }

    Optional<JobSchedule> optJobSchedule = null;
    List<String[]> csvContextData = (List<String[]>)context.get(SchedulingConstants.CTX_JOB_SCHEDULE_ELEMENTS);
    for (String[] parts :csvContextData){
      if (jobScheduleService.exists(GlobalSharedConstants.PROP_CODE, parts[2])){
        optJobSchedule = jobScheduleService.getByCode(parts[2]);
        jobSchedule = optJobSchedule.get();
      } else {
        jobSchedule = parseJobSchedule(parts);
        jobSchedule.setSchedulePlan(schedulePlan);

        jobScheduleService.save(jobSchedule);
      }

      if (null != jobSchedule){
        loadedJobSchedules.add(jobSchedule);
      }
    }
    return loadedJobSchedules;
  }

  private JobSchedule parseJobSchedule(String[] rawData){
    return JobSchedule.builder()
    .cronExpression(rawData[0])
    .jobClass(rawData[1])
    .code(rawData[2])
    .name(rawData[3])
    .displayName(rawData[4])
    .category(rawData[5])
    .build();
  }

  private Trigger buildTrigger(String name, String group, LocalDateTime fireTime, String cron) {
    name = CommonUtility.isEmpty(name) ? java.util.UUID.randomUUID().toString() : name;
    if (!CommonUtility.isEmpty(cron)) {
      if (!CronExpression.isValidExpression(cron))
        throw new IllegalArgumentException("Provided expression " + cron + " is not a valid cron expression");

      return TriggerBuilder.newTrigger()
          .withIdentity(name, group)
          .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())))
          .usingJobData("cron", cron)
          .build();
    } else if (!CommonUtility.isEmpty(fireTime)) {
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("fireTime", fireTime);
      return TriggerBuilder.newTrigger()
          .withIdentity(name, group)
          .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNextWithExistingCount())
          .startAt(Date.from(fireTime.atZone(ZoneId.systemDefault()).toInstant()))
          .usingJobData(jobDataMap)
          .build();
    }
    throw new IllegalStateException("unsupported trigger descriptor " + this);
  }
}