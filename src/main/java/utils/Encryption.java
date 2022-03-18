package utils;

import messages.EncryptedMessage;
import messages.ResponseMessage;
import sun.security.x509.*;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.cert.*;
import java.security.*;
import java.math.BigInteger;
import java.util.Arrays;
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

    public static byte[] getRandomNonce(int size) {
        byte[] nonce = new byte[size];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    public static void sendEncryptedResponse(String response, OutputStream os, Key symmKey, byte[] iv) throws IOException {
        byte[] encryptedResponse;
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, symmKey, new GCMParameterSpec(128, iv));
            encryptedResponse = cipher.doFinal(response.getBytes());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return;
        } catch (InvalidKeyException e) {
            System.err.println("Invalid Key");
            return;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("Invalid encrypted message");
            return;
        }
        os.write(encryptedResponse.length);
        os.write(encryptedResponse);
        os.close();
    }

    public static void sendEncryptedResponse(EncryptedMessage msg, OutputStream os, Key symmKey, byte[] iv) throws IOException {
        byte[] encryptedResponse;

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(msg);
        oos.flush();

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, symmKey, new GCMParameterSpec(128, iv));
            encryptedResponse = cipher.doFinal(bos.toByteArray());
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return;
        } catch (InvalidKeyException e) {
            System.err.println("Invalid Key");
            return;
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("Invalid encrypted message");
            return;
        }
        os.write(encryptedResponse.length);
        os.write(encryptedResponse);
        os.close();
    }

    public static String receiveEncryptedResponse(InputStream in, Key symmKey, byte[] iv) throws IOException {
        int messageSize = in.read();
        byte[] encryptedResponseArray = new byte[messageSize];
        for (int i = 0; i < messageSize; i++) {
            try {
                int byteRead = in.read();
                if (byteRead == -1) break;
                encryptedResponseArray[i] = (byte)byteRead;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] decryptedResponse = new byte[0];
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, symmKey, new GCMParameterSpec(128, iv));
            decryptedResponse = cipher.doFinal(encryptedResponseArray);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            System.exit(255);
        } catch (InvalidKeyException e) {
            System.err.println("Invalid Key");
            System.exit(255);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            System.err.println("Invalid encrypted message");
            System.exit(255);
        }

        return new String(decryptedResponse, StandardCharsets.UTF_8);
    }
}
