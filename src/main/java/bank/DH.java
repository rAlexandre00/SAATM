package bank;

import messages.DHMessage;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class DH {


    private final byte[] publicParameters;
    private final KeyPair kPair;

    public DH() {
        this.kPair = DHKeyPair.getInstance().getKeyPair();
        this.publicParameters = kPair.getPublic().getEncoded();
    }

    public byte[] getDHParams() {
        return publicParameters;
    }

    public SecretKeySpec generateSecret(DHMessage dhMessageFromATM) throws InvalidKeyException, InvalidKeySpecException, NoSuchAlgorithmException {

        KeyFactory bankKeyFac = KeyFactory.getInstance("DH");

        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(kPair.getPrivate());

        byte[] dhATMParameters = dhMessageFromATM.getDHParams();
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(dhATMParameters);
        Key atmPubKey = bankKeyFac.generatePublic(x509KeySpec);

        keyAgreement.doPhase(atmPubKey, true);
        byte[] bankSharedSecret = keyAgreement.generateSecret();
        return new SecretKeySpec(bankSharedSecret, 0, 16, "AES");
    }

}