package com.yanirta.obj.Factories;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;

public class CloseableHttpClientFactory {

    public CloseableHttpClient getCloseableHttpClient() {
        String proxyString = System.getenv("APPLITOOLS_PROXY");

        if (proxyString != null) {
            String[] proxySettings = proxyString.split(",");
            HttpHost proxy = new HttpHost(proxySettings[0], 8080);
            DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxy);

            if (proxySettings.length == 1) {
                return HttpClientBuilder.create().setRoutePlanner(routePlanner).build();
            } else if (proxySettings.length == 3) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(
                        new AuthScope(proxy),
                        new UsernamePasswordCredentials(
                                proxySettings[1],
                                proxySettings[2]
                        )
                );

                AuthCache authCache = new BasicAuthCache();
                BasicScheme basicAuth = new BasicScheme();
                authCache.put(proxy, basicAuth);
                HttpClientContext context = HttpClientContext.create();
                context.setCredentialsProvider(credentialsProvider);
                context.setAuthCache(authCache);
                return HttpClientBuilder.create().setRoutePlanner(routePlanner).setDefaultCredentialsProvider(credentialsProvider).build();
            }

        }
        return HttpClientBuilder.create().build();
    }
}