/**
 * 
 */
package net.brilliant.esi.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

import javax.validation.constraints.NotBlank;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import net.brilliant.common.CommonUtility;
import net.brilliant.domain.CoreObject;

/**
 * @author ducbq
 *
 */
@Setter
@Getter
@Builder
public class TriggerDescriptor extends CoreObject {
  /**
   * 
   */
  private static final long serialVersionUID = -1731694110319900296L;
  @NotBlank
  private String name;
  private String group;
  private LocalDateTime fireTime;
  private String cronExpression;
  private String status;
  private JobDetail jobDetail;

  public TriggerDescriptor setName(final String name) {
    this.name = name;
    return this;
  }

  public TriggerDescriptor setGroup(final String group) {
    this.group = group;
    return this;
  }

  public TriggerDescriptor setFireTime(final LocalDateTime fireTime) {
    this.fireTime = fireTime;
    return this;
  }

  public TriggerDescriptor setCron(final String cron) {
    this.cronExpression = cron;
    return this;
  }

  private String buildName() {
    return CommonUtility.isEmpty(name) ? java.util.UUID.randomUUID().toString() : name;
  }
  
  /**
   * Convenience method for building a Trigger
   * 
   * @return the Trigger associated with this descriptor
   */
  private Trigger doBuildTrigger(String name, String group, LocalDateTime fireTime, String cron) {
    // @formatter:off
    if (!CommonUtility.isEmpty(cron)) {
      if (!CronExpression.isValidExpression(cron))
        throw new IllegalArgumentException("Provided expression " + cron + " is not a valid cron expression");

      return TriggerBuilder.newTrigger()
          .withIdentity(buildName(), group)
          .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())))
          .usingJobData("cron", cron)
          .build();
    } else if (!CommonUtility.isEmpty(fireTime)) {
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("fireTime", fireTime);
      return TriggerBuilder.newTrigger()
          .withIdentity(buildName(), group)
          .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNextWithExistingCount())
          .startAt(Date.from(fireTime.atZone(ZoneId.systemDefault()).toInstant()))
          .usingJobData(jobDataMap)
          .build();
    }
    // @formatter:on
    throw new IllegalStateException("unsupported trigger descriptor " + this);
  }

  /**
   * Convenience method for building a Trigger
   * 
   * @return the Trigger associated with this descriptor
   */
  public Trigger buildTrigger() {
    return this.doBuildTrigger(this.name, this.group, this.fireTime, this.cronExpression);
    /*
    // @formatter:off
    if (!CommonUtility.isEmpty(cron)) {
      if (!CronExpression.isValidExpression(cron))
        throw new IllegalArgumentException("Provided expression " + cron + " is not a valid cron expression");

      return TriggerBuilder.newTrigger()
          .withIdentity(buildName(), group)
          .withSchedule(CronScheduleBuilder.cronSchedule(cron).withMisfireHandlingInstructionFireAndProceed().inTimeZone(TimeZone.getTimeZone(ZoneId.systemDefault())))
          .usingJobData("cron", cron)
          .build();
    } else if (!CommonUtility.isEmpty(fireTime)) {
      JobDataMap jobDataMap = new JobDataMap();
      jobDataMap.put("fireTime", fireTime);
      return TriggerBuilder.newTrigger()
          .withIdentity(buildName(), group)
          .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionNextWithExistingCount())
          .startAt(Date.from(fireTime.atZone(ZoneId.systemDefault()).toInstant()))
          .usingJobData(jobDataMap)
          .build();
    }
    // @formatter:on
    throw new IllegalStateException("unsupported trigger descriptor " + this);
  */
  }

  /**
   * Convenience method for building a Trigger
   * 
   * @return the Trigger associated with this descriptor
   */
  public Trigger buildTrigger(String name, String group, LocalDateTime fireTime, String cron) {
    return this.doBuildTrigger(name, group, fireTime, cron);
  }

  /**
   * 
   * @param trigger
   *          the Trigger used to build this descriptor
   * @return the TriggerDescriptor
   */
  public static TriggerDescriptor buildDescriptor(Trigger trigger) {
    return TriggerDescriptor.builder()
    .name(trigger.getKey().getName())
    .group(trigger.getKey().getGroup())
    .fireTime((LocalDateTime) trigger.getJobDataMap().get("fireTime"))
    .cronExpression(trigger.getJobDataMap().getString("cron"))
    .build();
  }

  /**
   * 
   * @param trigger
   *          the Trigger used to build this descriptor
   * @return the TriggerDescriptor
   */
  public static TriggerDescriptor buildDescriptor(
      String name, 
      String group, 
      LocalDateTime fireTime, 
      String cronExpression, 
      JobDetail jobDetail) {
    return TriggerDescriptor.builder()
    .name(name)
    .group(group)
    .fireTime(fireTime)
    .cronExpression(cronExpression)
    .jobDetail(jobDetail)
    .build();
  }
}
