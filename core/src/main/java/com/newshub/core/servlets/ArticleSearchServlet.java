package com.newshub.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.newshub.core.services.ArticleSearchService;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/newshub/search",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET
        }
)
public class ArticleSearchServlet extends SlingSafeMethodsServlet {

    @Reference
    private ArticleSearchService articleSearchService;

    @Override
    protected void doGet(SlingHttpServletRequest request,
                         SlingHttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("q");

        if(keyword == null || keyword.trim().isEmpty()){

            response.setStatus(400);
            response.getWriter().write("Query parameter 'q' required");
            return;
        }

        response.setContentType("application/json");

        response.getWriter().write(
                articleSearchService.searchArticles(keyword).toString()
        );
    }
}