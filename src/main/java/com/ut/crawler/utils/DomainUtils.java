package com.ut.crawler.utils;

import java.net.URI;
import java.net.URISyntaxException;

public class DomainUtils {
    public static String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            return uri.getHost();
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
