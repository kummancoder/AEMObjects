package com.newshub.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.api.servlets.HttpConstants;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.newshub.core.schedulers.ArticleArchiveScheduler;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/test-archiver",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class SchedulerTestServlet extends SlingSafeMethodsServlet {

    private static final Logger LOG =
            LoggerFactory.getLogger(SchedulerTestServlet.class);

    @Reference
    private ArticleArchiveScheduler scheduler;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        LOG.info("Manual scheduler execution triggered");

        scheduler.run();

        response.getWriter().write("Scheduler executed successfully");
    }
}