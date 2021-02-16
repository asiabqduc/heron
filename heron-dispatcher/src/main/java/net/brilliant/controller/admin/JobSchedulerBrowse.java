package net.brilliant.controller.admin;

import java.util.List;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.esi.service.JobScheduleService;
import net.brilliant.framework.controller.BrowserHome;
import net.brilliant.framework.model.NameFilter;

/**
 * @author ducbq
 */
@Named
@ViewScoped
public class JobSchedulerBrowse extends BrowserHome<JobSchedule, NameFilter> {
  /**
   * 
   */
  private static final long serialVersionUID = 1754644079582592185L;
  private static final String cachedDataProp = "cachedJobDescriptors";

  @Inject
  private JobScheduleService businessService;

  @Override
  public NameFilter createFilterModel() {
    return new NameFilter();
  }

  @Override
  protected List<JobSchedule> requestBusinessObjects() {
    List<JobSchedule> requestedBusinessObjects = (List<JobSchedule>)this.fetchCachedData(cachedDataProp);
    if (null == requestedBusinessObjects) {
      requestedBusinessObjects = businessService.getObjects();
      this.cachePut(cachedDataProp, requestedBusinessObjects);
    }
    return requestedBusinessObjects;
  }
}
