/**
 * 
 */
package net.brilliant.domain;

import org.quartz.Job;

import lombok.Getter;
import net.brilliant.global.SchedulingConstants;

/**
 * @author ducbq
 *
 */
@Getter
public enum JobSpec {
  ReconcileAuthServices (SchedulingConstants.defaultGroup, "ReconcileAuthServices", "5 0/2 * * * ?"),
  SyncGeneralServices (SchedulingConstants.defaultGroup, "SyncGeneralServices", "10 0/3 * * * ?"),
  AquariumReminders (SchedulingConstants.defaultGroup, "AquariumReminders", "2 0/1 * * * ?"),
  TransportationGenerators (SchedulingConstants.defaultGroup, "TransportationGenerators", "0 0/4 * * * ?"),
  SyncPOSServices (SchedulingConstants.defaultGroup, "SyncPOSServices", "3 0/5 * * * ?"),
  ;

  private String group;
  private String name;
  private String cronExpression;
  private Class<? extends Job> jobClass;
  
  private JobSpec(String group, String name, String cronExpression){
    this.group = group;
    this.name = name;
    this.cronExpression = cronExpression;
  }
}
