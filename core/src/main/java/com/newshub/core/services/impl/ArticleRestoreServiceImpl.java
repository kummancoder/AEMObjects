package com.newshub.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.newshub.core.services.ArticleRestoreService;

@Component(service = ArticleRestoreService.class)
public class ArticleRestoreServiceImpl implements ArticleRestoreService {

    private static final String NEWS_ROOT = "/content/newshub/en/news";
    private static final String ARCHIVE_ROOT = "/content/newshub/en/archive";

    private static final String ORIGINAL_PATH_PROPERTY = "originalPath";

    private static final String SUBSERVICE = "content-writer";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public boolean restoreArticle(String originalPath) {

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            if (originalPath == null || originalPath.isEmpty()) {
                throw new Exception("Original path is empty");
            }

            if (!originalPath.startsWith(NEWS_ROOT)) {
                throw new Exception("Invalid news article path");
            }

            String articleName =
                    originalPath.substring(originalPath.lastIndexOf("/") + 1);

            String archivePath =
                    ARCHIVE_ROOT + "/" + articleName;

            if (!session.nodeExists(archivePath)) {
                throw new Exception("Archived article not found");
            }

            if (session.nodeExists(originalPath)) {
                throw new Exception("Article already exists at original location");
            }

            session.move(archivePath, originalPath);

            Node restoredNode = session.getNode(originalPath);

            Node contentNode = restoredNode.getNode("jcr:content");

            if (contentNode.hasProperty(ORIGINAL_PATH_PROPERTY)) {
                contentNode.getProperty(ORIGINAL_PATH_PROPERTY).remove();
            }

            session.save();

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }
}