package com.newshub.core.services.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Session;

import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.newshub.core.services.ArticleArchiveService;

@Component(service = ArticleArchiveService.class)
public class ArticleArchiveServiceImpl implements ArticleArchiveService {

    private static final String NEWS_ROOT = "/content/newshub/en/news";
    private static final String ARCHIVE_ROOT = "/content/newshub/en/archive";

    private static final String SUBSERVICE_NAME = "content-writer";

    private static final String ORIGINAL_PATH_PROPERTY = "originalPath";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Override
    public boolean archiveArticle(String articlePath) {

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE_NAME);

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            if (!session.nodeExists(articlePath)) {
                throw new Exception("Article does not exist: " + articlePath);
            }

            if (!articlePath.startsWith(NEWS_ROOT)) {
                throw new Exception("Only news articles can be archived");
            }

            String articleName =
                    articlePath.substring(articlePath.lastIndexOf("/") + 1);

            String destinationPath =
                    ARCHIVE_ROOT + "/" + articleName;

            ensureArchiveFolder(session);

            storeOriginalPath(session, articlePath);

            session.move(articlePath, destinationPath);

            session.save();

            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;
        }
    }

    private void storeOriginalPath(Session session, String articlePath) throws Exception {

        Node pageNode = session.getNode(articlePath);

        Node contentNode = pageNode.getNode("jcr:content");

        contentNode.setProperty(ORIGINAL_PATH_PROPERTY, articlePath);
    }

    private void ensureArchiveFolder(Session session) throws Exception {

        if (!session.nodeExists(ARCHIVE_ROOT)) {

            Node parent = session.getNode("/content/newshub/en");

            parent.addNode("archive", "sling:OrderedFolder");

            session.save();
        }
    }
}