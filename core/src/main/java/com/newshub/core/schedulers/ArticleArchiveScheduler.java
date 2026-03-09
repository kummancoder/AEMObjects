package com.newshub.core.schedulers;

import java.util.*;

import javax.jcr.Session;

import org.apache.sling.api.resource.*;

import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.Designate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.search.*;
import com.day.cq.search.result.*;

import com.newshub.core.config.ArticleArchiverConfig;

@Component(service = {Runnable.class, ArticleArchiveScheduler.class}, immediate = true)
@Designate(ocd = ArticleArchiverConfig.class)
public class ArticleArchiveScheduler implements Runnable {

    private static final Logger LOG =
            LoggerFactory.getLogger(ArticleArchiveScheduler.class);

    private static final String SUBSERVICE = "content-writer";

    @Reference
    private ResourceResolverFactory resolverFactory;

    @Reference
    private QueryBuilder queryBuilder;

    private ArticleArchiverConfig config;

    @Activate
    @Modified
    protected void activate(ArticleArchiverConfig config) {

        this.config = config;

        LOG.info("Newshub Article Archiver Scheduler activated with cron: {}",
                config.scheduler_expression());
    }

    @Override
    public void run() {

        LOG.info("Starting Newshub Automatic Article Archiver...");

        int successCount = 0;
        int failureCount = 0;

        Map<String, Object> serviceMap = new HashMap<>();
        serviceMap.put(ResourceResolverFactory.SUBSERVICE, SUBSERVICE);

        try (ResourceResolver resolver =
                     resolverFactory.getServiceResourceResolver(serviceMap)) {

            Session session = resolver.adaptTo(Session.class);

            List<String> oldArticles = findOldArticles(session);

            LOG.info("Total articles eligible for archiving: {}", oldArticles.size());

            int batchSize = config.batch_size();

            for (int i = 0; i < oldArticles.size(); i += batchSize) {

                int endIndex = Math.min(i + batchSize, oldArticles.size());

                List<String> batch = oldArticles.subList(i, endIndex);

                try {

                    for (String articlePath : batch) {

                        String articleName =
                                articlePath.substring(articlePath.lastIndexOf("/") + 1);

                        String destination =
                                config.archive_root() + "/" + articleName;

                        LOG.info("Archiving article {}", articlePath);

                        session.move(articlePath, destination);
                    }

                    session.save();

                    successCount += batch.size();

                } catch (Exception e) {

                    LOG.error("Batch failed. Reverting batch starting with {}",
                            batch.get(0), e);

                    session.refresh(false);

                    failureCount += batch.size();
                }
            }

        } catch (Exception e) {

            LOG.error("Error executing Article Archiver Scheduler", e);
        }

        LOG.info("Article Archiver Summary -> Success: {}, Failed: {}",
                successCount, failureCount);
    }

    private List<String> findOldArticles(Session session) {

        List<String> results = new ArrayList<>();

        Calendar limitDate = Calendar.getInstance();
        limitDate.add(Calendar.DAY_OF_YEAR, -config.days_limit());

        Map<String, String> map = new HashMap<>();

        map.put("path", config.news_root());
        map.put("type", "cq:Page");

        map.put("daterange.property", "jcr:content/publishDate");
        map.put("daterange.upperBound", limitDate.toInstant().toString());
        map.put("daterange.upperOperation", "<");

        map.put("p.limit", "-1");

        Query query =
                queryBuilder.createQuery(PredicateGroup.create(map), session);

        SearchResult result = query.getResult();

        try {

            for (Hit hit : result.getHits()) {

                results.add(hit.getPath());
            }

        } catch (Exception e) {

            LOG.error("Error retrieving old articles", e);
        }

        return results;
    }
}