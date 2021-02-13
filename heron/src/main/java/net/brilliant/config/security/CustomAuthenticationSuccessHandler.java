/**
 * 
 */
package net.brilliant.config.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * @author ducbq
 *
 */
@Slf4j
@Service
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	/*@Inject 
	private AuthorizationService authorizationService;*/
	
	//private RequestCache requestCache;

	public CustomAuthenticationSuccessHandler() {
		//this.requestCache = new HttpSessionRequestCache();
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
	  log.info("onAuthenticationSuccess");
		//User userDetails = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		/*UserAccountProfile userAccount = null;
		if (SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof String) {
			userAccount = authorizationService.getUserAccount((String)SecurityContextHolder.getContext().getAuthentication().getPrincipal());
		}

		request.getSession().setAttribute("userDetails", userAccount);
		*/
		super.onAuthenticationSuccess(request, response, authentication);
	}
}
