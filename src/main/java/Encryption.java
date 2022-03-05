import sun.security.x509.*;

import java.io.*;
import java.security.cert.*;
import java.security.*;
import java.math.BigInteger;
import java.util.Date;

@SuppressWarnings("sunapi")
public class Encryption {

    /**
     * Create a self-signed X.509 Certificate
     *
     * @param dn        the X.509 Distinguished Name, eg "CN=Test, L=London, C=GB"
     * @param pair      the KeyPair
     * @param days      how many days from now the Certificate is valid for
     * @param algorithm the signing algorithm, eg "SHA1withRSA"
     */
    public static X509CertImpl generateCertificate(String dn, KeyPair pair, int days, String algorithm)
            throws GeneralSecurityException, IOException {
        PrivateKey privkey = pair.getPrivate();
        X509CertInfo info = new X509CertInfo();
        Date from = new Date();
        Date to = new Date(from.getTime() + days * 86400000L);
        CertificateValidity interval = new CertificateValidity(from, to);
        BigInteger sn = new BigInteger(64, new SecureRandom());
        X500Name owner = new X500Name(dn);

        info.set(X509CertInfo.VALIDITY, interval);
        info.set(X509CertInfo.SERIAL_NUMBER, new CertificateSerialNumber(sn));
        info.set(X509CertInfo.SUBJECT, owner);
        info.set(X509CertInfo.ISSUER, owner);
        info.set(X509CertInfo.KEY, new CertificateX509Key(pair.getPublic()));
        info.set(X509CertInfo.VERSION, new CertificateVersion(CertificateVersion.V3));
        AlgorithmId algo = new AlgorithmId(AlgorithmId.md5WithRSAEncryption_oid);
        info.set(X509CertInfo.ALGORITHM_ID, new CertificateAlgorithmId(algo));

        // Sign the cert to identify the algorithm that's used.
        X509CertImpl cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);

        // Update the algorithm, and resign.
        algo = (AlgorithmId) cert.get(X509CertImpl.SIG_ALG);
        info.set(CertificateAlgorithmId.NAME + "." + CertificateAlgorithmId.ALGORITHM, algo);
        cert = new X509CertImpl(info);
        cert.sign(privkey, algorithm);
        return cert;
    }

    public static void certificateToFile(X509CertImpl cert, String fileName) throws CertificateEncodingException {
        try {
            File file = new File(fileName);
            if (!file.createNewFile()) {
                System.err.println("File " + fileName + " already exists.");
                System.exit(255);
            }
            FileOutputStream fos = new FileOutputStream(file);
            cert.encode(fos);
            fos.close();
        } catch (IOException | CertificateEncodingException e) {
            throw new CertificateEncodingException(e);
        }
    }

    public static KeyPair generateKeyPair() {
        try {
            return KeyPairGenerator.getInstance("RSA").generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(255);
            return null;
        }
    }
}
