package net.brilliant.controller.admin;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import lombok.Getter;
import lombok.Setter;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonConstants;
import net.brilliant.common.CommonUtility;
import net.brilliant.domain.model.Filter;
import net.brilliant.esi.common.CommonScheduleUtility;
import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.esi.service.JobScheduleService;

/**
 * @author ducbq
 */
@Named
@ViewScoped
public class JobScheduleBrowse implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = -657321797836602952L;

  @Inject
	private JobScheduleService businessService;

  private List<JobSchedule> selectedObjects;
	private List<JobSchedule> businessObjects;
	private Filter<JobSchedule> bizFilter = new Filter<>(new JobSchedule());
	private List<JobSchedule> filteredObjects;// datatable filteredValue attribute (column filters)

	private String instantSearch;
	Long id;

	Filter<JobSchedule> filter = new Filter<>(new JobSchedule());

	List<JobSchedule> filteredValue;// datatable filteredValue attribute (column filters)

	@Setter @Getter
	private Map<String, ?> searchParameters;

  @Setter @Getter
  private Date searchDate;

	@PostConstruct
	public void initDataModel() {
		try {
		  this.searchParameters = CollectionsUtility.createMap();
			this.businessObjects = businessService.getObjects();
			Locale locale = new Locale(CommonConstants.LOCALE_VIETNAM_LANGUAGE, CommonConstants.LOCALE_VIETNAM_COUNTRY);
			
			this.businessObjects.forEach(jobSchedule->{
			  try {
			    jobSchedule.setCronExpressionReadable(CommonScheduleUtility.parseCronExpressionReadable(jobSchedule.getCronExpression(), locale));
        } catch (Exception e) {
          e.printStackTrace();
        }
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		filter = new Filter<JobSchedule>(new JobSchedule());
	}

	public List<String> completeModel(String query) {
		List<String> result = CollectionsUtility.createDataList();// carService.getModels(query);
		return result;
	}

	public void search(String parameter) {
		System.out.println("Searching parameter: " + parameter);
		/*
		 * if (id == null) { throw new BusinessException("Provide Car ID to load"); }
		 * selectedCars.add(carService.findById(id));
		 */
	}

  public void search() {
    System.out.println("Searching parameters");
    /*
     * if (id == null) { throw new BusinessException("Provide Car ID to load"); }
     * selectedCars.add(carService.findById(id));
     */
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
		//System.out.println("Biz objects: " + businessObjects.size());
		return businessObjects;
	}

	public void setBusinessObjects(List<JobSchedule> businessObjects) {
		this.businessObjects = businessObjects;
	}

	public List<JobSchedule> getSelectedObjects() {
		if (null != selectedObjects) {
			//System.out.println("Sel objects: " + selectedObjects.size());
		}

		return selectedObjects;
	}

	public void setSelectedObjects(List<JobSchedule> selectedObjects) {
		this.selectedObjects = selectedObjects;
	}

	public Filter<JobSchedule> getBizFilter() {
		return bizFilter;
	}

	public void setBizFilter(Filter<JobSchedule> bizFilter) {
		this.bizFilter = bizFilter;
	}

	public List<JobSchedule> getFilteredObjects() {
		return filteredObjects;
	}

	public void setFilteredObjects(List<JobSchedule> filteredObjects) {
		this.filteredObjects = filteredObjects;
	}

	public String getInstantSearch() {
		return instantSearch;
	}

	public void setInstantSearch(String instantSearch) {
		this.instantSearch = instantSearch;
	}

	public void recordsRowSelected(AjaxBehaviorEvent e) {
		//System.out.println("recordsRowSelected");
	}
}
