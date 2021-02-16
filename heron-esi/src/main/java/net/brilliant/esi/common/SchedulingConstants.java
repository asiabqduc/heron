/**
 * 
 */
package net.brilliant.esi.common;

import net.brilliant.ccs.GlobalSharedConstants;

/**
 * @author ducbq
 *
 */
public interface SchedulingConstants {
  static char CSV_ELEMENT_SEPARATOR = '|';
  static String CTX_JOB_SCHEDULE_ELEMENTS = "ctxJobScheduleElements";
  static String CTX_SCHEDULE_PLANS = "ctxSchedulePlanElements";

  static String CTX_SEPARATOR = "; ";

  static String CTX_NAME = GlobalSharedConstants.PROP_NAME;
  static String CTX_GROUP = "group";

  static String CTX_CLASS = "class";

  static String defaultGroup = "Aquarium";
  static String specJob = "Job";
  static String specTrigger = "Trigger";

}
