package edu.bbte.kripto.lbim2260.getssl;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class Main {

    static String url = "https://bnr.ro";

    public static void main(String[] args) {
        try {
            SSLContext ctx;
            X509TrustManager trustManager = new TrustManagerImpl();
            ctx = SSLContext.getInstance("TLS");
            ctx.init(null, new TrustManager[] { trustManager }, new SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

            URI uri = new URI(url);
            URL u = uri.toURL();
            HttpsURLConnection conn = (HttpsURLConnection) u.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            }
        } catch (NoSuchAlgorithmException | KeyManagementException | URISyntaxException | IOException e) {
            System.out.println("Error");
            e.printStackTrace();
        }
    }

}