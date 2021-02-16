package net.brilliant.controller.admin;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import net.brilliant.auth.entity.UserAccountProfile;
import net.brilliant.auth.service.UserAccountService;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.domain.model.Filter;

/**
 * @author ducbq
 */
@Named/*(value = "userAccountBrowser")*/
@ViewScoped
public class UserAccountBrowser implements Serializable {
	/**
   * 
   */
  private static final long serialVersionUID = -3681380031071678146L;
  @Inject
	private UserAccountService businessService;
	private List<UserAccountProfile> selectedObjects;
	private List<UserAccountProfile> businessObjects;
	private Filter<UserAccountProfile> bizFilter = new Filter<>(new UserAccountProfile());
	private List<UserAccountProfile> filteredObjects;// datatable filteredValue attribute (column filters)

	private String instantSearch;
	Long id;

	Filter<UserAccountProfile> filter = new Filter<>(new UserAccountProfile());

	List<UserAccountProfile> filteredValue;// datatable filteredValue attribute (column filters)

	@PostConstruct
	public void initDataModel() {
		try {
			this.businessObjects = businessService.getObjects();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void clear() {
		filter = new Filter<UserAccountProfile>(new UserAccountProfile());
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

	public void delete() {
		if (CommonUtility.isNotEmpty(this.selectedObjects)) {
			for (UserAccountProfile removalItem : this.selectedObjects) {
				System.out.println("#" + removalItem.getDisplayName());
				this.businessObjects.remove(removalItem);
			}
			this.selectedObjects.clear();
		}
	}

	public List<UserAccountProfile> getFilteredValue() {
		return filteredValue;
	}

	public void setFilteredValue(List<UserAccountProfile> filteredValue) {
		this.filteredValue = filteredValue;
	}

	public Filter<UserAccountProfile> getFilter() {
		return filter;
	}

	public void setFilter(Filter<UserAccountProfile> filter) {
		this.filter = filter;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public List<UserAccountProfile> getBusinessObjects() {
		//System.out.println("Biz objects: " + businessObjects.size());
		return businessObjects;
	}

	public void setBusinessObjects(List<UserAccountProfile> businessObjects) {
		this.businessObjects = businessObjects;
	}

	public List<UserAccountProfile> getSelectedObjects() {
		if (null != selectedObjects) {
			//System.out.println("Sel objects: " + selectedObjects.size());
		}

		return selectedObjects;
	}

	public void setSelectedObjects(List<UserAccountProfile> selectedObjects) {
		this.selectedObjects = selectedObjects;
	}

	public Filter<UserAccountProfile> getBizFilter() {
		return bizFilter;
	}

	public void setBizFilter(Filter<UserAccountProfile> bizFilter) {
		this.bizFilter = bizFilter;
	}

	public List<UserAccountProfile> getFilteredObjects() {
		return filteredObjects;
	}

	public void setFilteredObjects(List<UserAccountProfile> filteredObjects) {
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
