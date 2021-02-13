package com.github.adminfaces.starter.bean;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.springframework.security.core.context.SecurityContextHolder;

@Named
@ViewScoped
public class SessionMB implements Serializable {

	/**
   * 
   */
  private static final long serialVersionUID = 9192378456310373153L;
  private String currentUser;

	@PostConstruct
	public void init() {
		currentUser = SecurityContextHolder.getContext().getAuthentication().getName();
	}

	public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}
}
