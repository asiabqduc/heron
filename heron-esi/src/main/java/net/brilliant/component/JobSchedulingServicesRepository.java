/**
 * 
 */
package net.brilliant.component;

import java.util.List;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.springframework.stereotype.Component;

import net.brilliant.common.BeanUtility;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.domain.JobDescriptor;
import net.brilliant.domain.JobSpec;
import net.brilliant.domain.TriggerDescriptor;
import net.brilliant.global.SchedulingConstants;

/**
 * @author ducbq
 *
 */
@Component
public class JobSchedulingServicesRepository {
  Map<String, String> triggerSpecs = CollectionsUtility.createHashMapData(      
      "ReconcileAuthServices",    "5 0/2 * * * ?",
      "SyncGeneralServices",      "10 0/3 * * * ?",
      "AquariumReminders",        "2 0/1 * * * ?",
      "TransportationGenerators", "0 0/4 * * * ?",
      "SyncPOSServices",          "3 0/5 * * * ?"
  );

  @SuppressWarnings("unchecked")
  public List<TriggerDescriptor> getDefaultTriggerDescriptors(String group) throws ClassNotFoundException{    
    List<TriggerDescriptor> triggerDescriptors = CollectionsUtility.createDataList();
    JobDetail currJobDetail = null;
    String jobPackageNameQualified = "net.brilliant.scheduler.";
    for (JobSpec jobSpec :JobSpec.values()){
      currJobDetail = JobDescriptor.buildJobDetail(
          jobSpec.getGroup(), 
          jobSpec.getName() + SchedulingConstants.specJob, 
          (Class<? extends Job>)BeanUtility.getClass(jobPackageNameQualified + jobSpec.getName() + SchedulingConstants.specJob));

      triggerDescriptors.add(TriggerDescriptor.buildDescriptor(
          jobSpec.getName() + SchedulingConstants.specTrigger, 
          group, 
          null, 
          jobSpec.getCronExpression(), 
          currJobDetail)
       );
    }
    return triggerDescriptors;
  }

  
}