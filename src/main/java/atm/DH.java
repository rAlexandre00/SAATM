package atm;

import messages.DHMessage;
import utils.KeyAndIV;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class DH {

    private byte[] publicParameters;
    private KeyPair kPair;

    public DH() {

        try {
            KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("DH");
            kPairGen.initialize(2048);
            kPair = kPairGen.generateKeyPair();
            publicParameters = kPair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException ignored) {}

    }

    public byte[] getPublicParameters() {
        return publicParameters;
    }

    public KeyAndIV getEncryptionParams(DHMessage dhMessageFromBank) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {

        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(kPair.getPrivate());

        byte[] bankPubKeyEnc = dhMessageFromBank.getDHParams();
        IvParameterSpec iv = dhMessageFromBank.getIV();
        KeyFactory atmKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bankPubKeyEnc);
        PublicKey bankPubKey = atmKeyFac.generatePublic(x509KeySpec);
        keyAgreement.doPhase(bankPubKey, true);

        byte[] atmSharedSecret = keyAgreement.generateSecret();

        SecretKeySpec atmAesKey = new SecretKeySpec(atmSharedSecret, 0, 16, "AES");
        return new KeyAndIV(atmAesKey, iv);

    }

}