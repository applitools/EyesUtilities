package com.applitools.commands;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.CommaParameterSplitter;

import javax.net.ssl.*;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

public abstract class CommandBase implements Command {
    @Parameter(names = {"-dv"}, description = "Disable SSL certificate validation. !!!Unsecured!!!")
    private boolean disableCertificateValidation = false;
    @Parameter(names = {"-pu", "-proxyUrl"}, description = "The URL for intermediary proxy if necessary")
    private String proxyUrl;
    @Parameter(names = {"-pp", "-proxyPort"}, description = "Port to use when sending proxy requests (when Url is an IP address)")
    private String proxyPort;
    @Parameter(
            names = {"-pa", "-proxyAuth"},
            description = "Username and password for auth communication. Pass arguments as \"username,password\")",
            splitter = CommaParameterSplitter.class
    )
    List<String> proxyCredentials = new ArrayList<>();

    public void Execute() throws Exception {
        if (disableCertificateValidation)
            disableCertValidation();
        if (proxyUrl != null) {
            setProxy();
        }
        run();
    }

    private static void disableCertValidation() throws KeyManagementException, NoSuchAlgorithmException {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    private void setProxy() {
        StringBuilder sb = new StringBuilder();
        sb.append("Executing calls through proxy URL ").append(proxyUrl).append("\n");
        System.setProperty("https.proxyHost", proxyUrl);

        if (proxyPort != null) {
            sb.append("Setting proxy port to ").append(proxyPort).append("\n");
            System.setProperty("https.proxyPort", proxyPort);
        }

        if (proxyCredentials.size() > 0) {
            sb.append("Setting proxy user for username ").append(proxyCredentials.get(0)).append("\n");
            System.setProperty("https.proxyUser", proxyCredentials.get(0));
            System.setProperty("https.ProxyPassword", proxyCredentials.get(1));
        }
        System.out.print(sb);
    }
}
