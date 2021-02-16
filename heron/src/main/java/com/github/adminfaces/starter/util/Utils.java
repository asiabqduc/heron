package com.github.adminfaces.starter.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.faces.application.FacesMessage;
import javax.inject.Named;

import org.omnifaces.util.Messages;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.adminfaces.starter.model.Car;

import net.brilliant.ccs.GlobalSharedConstants;

/**
 * Created by rmpestano on 07/02/17.
 */
@Named
@ApplicationScoped
public class Utils implements Serializable {

  /**
  * 
  */
  private static final long serialVersionUID = -2618428552814595899L;
  private List<Car> cars;

  @PostConstruct
  public void init() {
    cars = new ArrayList<>();
    IntStream.rangeClosed(1, 50).forEach(i -> cars.add(create(i)));
  }

  private static Car create(int i) {
    return new Car(i).model("model " + i).name(GlobalSharedConstants.PROP_NAME + i).price(Double.valueOf(i));
  }

  public static void addDetailMessage(String message) {
    addDetailMessage(message, null);
  }

  public static void addDetailMessage(String message, FacesMessage.Severity severity) {

    FacesMessage facesMessage = Messages.create("").detail(message).get();
    if (severity != null && severity != FacesMessage.SEVERITY_INFO) {
      facesMessage.setSeverity(severity);
    }
    Messages.add(null, facesMessage);
  }

  @Produces
  public List<Car> getCars() {
    return cars;
  }

  public static boolean isUserInRole(String role) {
    // get security context from thread local
    SecurityContext context = SecurityContextHolder.getContext();
    if (context == null)
      return false;

    Authentication authentication = context.getAuthentication();
    if (authentication == null)
      return false;

    for (GrantedAuthority auth : authentication.getAuthorities()) {
      if (role.equals(auth.getAuthority()))
        return true;
    }

    return false;
  }

}
