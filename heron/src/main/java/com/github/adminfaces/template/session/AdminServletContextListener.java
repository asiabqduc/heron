package com.github.adminfaces.template.session;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by rmpestano on 28/04/17.
 */
public class AdminServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //only here for backyard compatibility
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
