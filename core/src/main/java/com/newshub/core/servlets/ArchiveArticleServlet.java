package com.newshub.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.apache.sling.api.servlets.ServletResolverConstants;

import com.newshub.core.services.ArticleArchiveService;

@Component(
        service = Servlet.class,
        property = {
                ServletResolverConstants.SLING_SERVLET_PATHS + "=/bin/newshub/archive",
                ServletResolverConstants.SLING_SERVLET_METHODS + "=" + HttpConstants.METHOD_POST
        }
)
public class ArchiveArticleServlet extends SlingAllMethodsServlet {

    private static final long serialVersionUID = 1L;

    @Reference
    private ArticleArchiveService archiveService;

    @Override
    protected void doPost(SlingHttpServletRequest request,
                          SlingHttpServletResponse response)
            throws ServletException, IOException {

        String articlePath = request.getParameter("articlePath");

        if (articlePath == null || articlePath.trim().isEmpty()) {

            response.setStatus(400);
            response.getWriter().write("Parameter 'articlePath' is required");

            return;
        }

        boolean archived =
                archiveService.archiveArticle(articlePath);

        if (archived) {

            response.getWriter().write("Article archived successfully");

        } else {

            response.setStatus(500);
            response.getWriter().write("Failed to archive article");
        }
    }
}