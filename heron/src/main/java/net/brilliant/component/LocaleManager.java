/**
 * 
 */
package net.brilliant.component;

import java.util.Locale;

import javax.annotation.ManagedBean;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Faces;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import net.brilliant.ccs.GlobalSharedConstants;
import net.brilliant.common.CommonUtility;
import net.brilliant.i18n.GlobalDataRepository;

/**
 * @author ducbq
 *
 */
@ManagedBean
@SessionScoped
public class LocaleManager {
	private Locale locale;

  @Inject
  private HttpSession session;

  @PostConstruct
  public void init() {
      locale = CommonUtility.LOCALE_VIETNAMESE; //FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
  }

  public Locale getLocale() {
      return locale;
  }

  public String getLanguage() {
      return locale.getLanguage();
  }

  public void setLanguage(String language) {
  	if ("en".equalsIgnoreCase(language)) {
    	locale = Locale.US;
  	} else {
  		locale = CommonUtility.LOCALE_VIETNAMESE;
  	}

  	String currentRequestPath = ServletUriComponentsBuilder.fromCurrentRequest().build().getPath();
  	this.session.setAttribute(GlobalSharedConstants.WORKING_LOCALE, locale);
  	GlobalDataRepository.builder().build().switchLocale(locale);
    FacesContext.getCurrentInstance().getViewRoot().setLocale(locale);
    try {
			Faces.redirect(currentRequestPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
  }
}
