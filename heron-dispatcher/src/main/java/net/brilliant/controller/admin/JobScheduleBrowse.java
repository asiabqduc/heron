package net.brilliant.controller.admin;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.brilliant.ccs.GlobalSharedConstants;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonConstants;
import net.brilliant.common.CommonUtility;
import net.brilliant.domain.model.Filter;
import net.brilliant.esi.common.CommonScheduleUtility;
import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.esi.service.JobScheduleService;
import net.brilliant.framework.model.SearchParameter;

/**
 * @author ducbq
 */
@Slf4j
@Named
@ViewScoped
public class JobScheduleBrowse implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -657321797836602952L;

  private final String cachedBusinessObjects = "cachedJobSchedules";
  @Inject
  private JobScheduleService businessService;

  private List<JobSchedule> selectedObjects;
  private List<JobSchedule> businessObjects;
  private List<JobSchedule> filteredObjects;// datatable filteredValue attribute (column filters)

  Long id;

  Filter<JobSchedule> filter = new Filter<>(new JobSchedule());

  List<JobSchedule> filteredValue;// datatable filteredValue attribute (column filters)

  @Setter
  @Getter
  private Map<String, ? extends Object> searchParameters;

  @Setter
  @Getter
  private Date searchDate;

  @Setter
  @Getter
  private String toggleTitle;

  @Inject
  private HttpSession session;

  @PostConstruct
  public void initDataModel() {
    try {
      loadBusinessData();
      this.toggleTitle = "lbl.toggleExpand";
      this.searchParameters = CollectionsUtility.createMap();
    } catch (Exception e) {
      log.error(e.getMessage());
    }
  }

  public void onToggle(ToggleEvent event) {
    if (Visibility.HIDDEN.equals(event.getVisibility())) {
      this.toggleTitle = "lbl.toggleExpand";
    } else if (Visibility.VISIBLE.equals(event.getVisibility())) {
      this.toggleTitle = "lbl.toggleCollapse";
    }
  }

  public void clear() {
    filter = new Filter<JobSchedule>(new JobSchedule());
  }

  public List<String> completeModel(String query) {
    List<String> result = CollectionsUtility.createDataList();// carService.getModels(query);
    return result;
  }

  public void search() {
    PageRequest pageRequest = PageRequest.of(0, 500, Sort.Direction.ASC, "id");
    SearchParameter searchParameter = SearchParameter.getInstance().setPageable(pageRequest);
    searchParameter.put("code", this.searchParameters.get("searchCode"));
    Page<JobSchedule> jobSchedules = businessService.getObjects(searchParameter);
    this.businessObjects.clear();
    if (!jobSchedules.isEmpty()) {
      this.businessObjects.addAll(jobSchedules.getContent());
      this.session.setAttribute(cachedBusinessObjects, this.businessObjects);
    }
  }

  public void delete() {
    if (CommonUtility.isNotEmpty(this.selectedObjects)) {
      for (JobSchedule removalItem : this.selectedObjects) {
        System.out.println("#" + removalItem.getDisplayName());
        this.businessObjects.remove(removalItem);
      }
      this.selectedObjects.clear();
    }
  }

  public List<JobSchedule> getFilteredValue() {
    return filteredValue;
  }

  public void setFilteredValue(List<JobSchedule> filteredValue) {
    this.filteredValue = filteredValue;
  }

  public Filter<JobSchedule> getFilter() {
    return filter;
  }

  public void setFilter(Filter<JobSchedule> filter) {
    this.filter = filter;
  }

  public List<JobSchedule> getBusinessObjects() {
    // System.out.println("Biz objects: " + businessObjects.size());
    return businessObjects;
  }

  public void setBusinessObjects(List<JobSchedule> businessObjects) {
    this.businessObjects = businessObjects;
  }

  public List<JobSchedule> getSelectedObjects() {
    if (null != selectedObjects) {
      // System.out.println("Sel objects: " + selectedObjects.size());
    }

    return selectedObjects;
  }

  public void setSelectedObjects(List<JobSchedule> selectedObjects) {
    this.selectedObjects = selectedObjects;
  }

  public List<JobSchedule> getFilteredObjects() {
    return filteredObjects;
  }

  public void setFilteredObjects(List<JobSchedule> filteredObjects) {
    this.filteredObjects = filteredObjects;
  }

  public void recordsRowSelected(AjaxBehaviorEvent e) {
    // System.out.println("recordsRowSelected");
  }

  private void loadBusinessData() {
    // Check and load cached data first
    if (null != this.session.getAttribute(cachedBusinessObjects)) {
      this.businessObjects = (List<JobSchedule>) this.session.getAttribute(cachedBusinessObjects);
    } else {
      this.businessObjects = businessService.getObjects();
      this.businessObjects.forEach(jobSchedule -> {
        try {
          Locale locale = (Locale) this.session.getAttribute(GlobalSharedConstants.WORKING_LOCALE);
          jobSchedule.setCronExpressionReadable(CommonScheduleUtility.parseCronExpressionReadable(jobSchedule.getCronExpression(), locale));
        } catch (Exception e) {
          log.error(e.getMessage(), e);
        }
      });

    }
  }
}
