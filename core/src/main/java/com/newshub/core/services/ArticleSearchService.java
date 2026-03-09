package com.newshub.core.services;

import org.json.JSONArray;

public interface ArticleSearchService {

    JSONArray searchArticles(String keyword);

}