package edu.bbte.kripto.lbim2260.getssl;

import javax.net.ssl.X509TrustManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrustManagerImpl implements X509TrustManager {

    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    boolean done;

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        if (done) {
            return;
        }
        done = true;

        for (X509Certificate oneCertificate : x509Certificates) {
            System.out.println();
            System.out.println("Certificate: ");
            System.out.println(" - Version: " + oneCertificate.getVersion());
            System.out.println(" - Serial Number: " + oneCertificate.getSerialNumber());
            System.out.println(" - Issuer: " + oneCertificate.getIssuerX500Principal());
            System.out.println(" - Date issued: " + oneCertificate.getNotBefore());
            System.out.println(" - Expiration: " + oneCertificate.getNotAfter());
            System.out.println(" - Subject: " + oneCertificate.getSubjectX500Principal());

            PublicKey publicKey = oneCertificate.getPublicKey();
            String algorithm = publicKey.getAlgorithm();
            byte[] encodedKey = publicKey.getEncoded();
            System.out.println(" - Algorithm: " + algorithm);
            System.out.println(" - Public Key: " + Base64.getEncoder().encodeToString(encodedKey));
            try {
                String cn = getCN(oneCertificate.getSubjectX500Principal().toString());
                String filename = cn.replaceAll("\\*", "_ANY_");
                writeCertificatePem(oneCertificate, new File("output/" + filename + ".pem"));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }

    private static String getCN(String s) {
        try {
            Pattern p = Pattern.compile(".*\\bCN=([^,]*),.*");
            Matcher m = p.matcher(s);
            m.find();
            return m.group(1);
        } catch (Exception e) {
            return "Unknown CN of " + s;
        }
    }

    private static void writeCertificatePem(X509Certificate cert, File file)
            throws IOException, CertificateEncodingException {
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            Base64.Encoder encoder = Base64.getMimeEncoder(64, new byte[] { 0x0a });
            out.write(BEGIN_CERT.getBytes(StandardCharsets.US_ASCII));
            out.write(0x0a);
            out.write(encoder.encode(cert.getEncoded()));
            out.write(0x0a);
            out.write(END_CERT.getBytes(StandardCharsets.US_ASCII));
            out.write(0x0a);
            System.out.println("Successfully saved to " + file);
        }
    }
}