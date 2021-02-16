package net.brilliant.controller.admin;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Faces;
import org.springframework.security.access.AccessDeniedException;

import net.brilliant.auth.entity.UserAccountProfile;
import net.brilliant.auth.service.UserAccountService;
import net.brilliant.common.CommonUtility;
import net.brilliant.utility.FacesService;

/**
 * @author ducbq
 */
@Named/*(value = "userAccountDetail")*/
@ViewScoped
public class UserAccountDetail implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1654144183269646576L;

	@Inject
	private UserAccountService businessService;

	@Inject
	private FacesService facesUtilities;

	private Long id;
	private UserAccountProfile businessObject;

	public void init() {
		if (Faces.isAjaxRequest()) {
			return;
		}
		if (CommonUtility.isNotEmpty(id)) {
			businessObject = businessService.getObject(id);
		} else {
			businessObject = new UserAccountProfile();
		}
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UserAccountProfile getBusinessObject() {
		return businessObject;
	}

	public void setBusinessObject(UserAccountProfile businessObject) {
		this.businessObject = businessObject;
	}

	public void remove() throws IOException {
		if (!facesUtilities.isUserInRole("ROLE_ADMIN")) {
			throw new AccessDeniedException("User not authorized! Only role <b>admin</b> can remove cars.");
		}
		if (CommonUtility.isNotEmpty(businessObject) && CommonUtility.isNotEmpty(businessObject.getId())) {
			businessService.remove(businessObject);
			facesUtilities.addDetailMessage("Business object " + businessObject.getDisplayName() + " removed successfully");
			Faces.getFlash().setKeepMessages(true);
			Faces.redirect("user/car-list.jsf");
		}
	}

	public void save() {
		String msg;
		if (businessObject.getId() == null) {
			businessService.saveOrUpdate(businessObject);
			msg = "Business object " + businessObject.getDisplayName() + " created successfully";
		} else {
			businessService.saveOrUpdate(businessObject);
			msg = "Business object " + businessObject.getDisplayName() + " updated successfully";
		}
		facesUtilities.addDetailMessage(msg);
	}

	public void clear() {
		businessObject = new UserAccountProfile();
		id = null;
	}

	public boolean isNew() {
		return businessObject == null || businessObject.getId() == null;
	}
}
