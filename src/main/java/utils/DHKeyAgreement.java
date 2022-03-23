/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package utils;
import messages.DHMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.*;
import java.util.Arrays;
import javax.crypto.*;
import javax.crypto.spec.*;
import javax.crypto.interfaces.*;

public class DHKeyAgreement {

    private InputStream is;
    private OutputStream os;

    public DHKeyAgreement(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    public SecretKeySpec DHExchangeATM() throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        /*
         * ATM creates her own DH key pair with 2048-bit key size
         */
        KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("DH");
        kPairGen.initialize(2048);
        KeyPair kPair = kPairGen.generateKeyPair();

        // ATM creates and initializes her DH KeyAgreement object
        KeyAgreement atmKeyAgree = KeyAgreement.getInstance("DH");
        atmKeyAgree.init(kPair.getPrivate());

        // ATM encodes her public key, and sends it over to BANK.
        byte[] pubKeyEnc = kPair.getPublic().getEncoded();

        DHMessage atmPubKeyMessage = new DHMessage(pubKeyEnc);
        TransportFactory.sendMessage(atmPubKeyMessage, os);

        /*
         * ATM uses Bank's public key for the first (and only) phase
         * of her version of the DH
         * protocol.
         * Before she can do so, she has to instantiate a DH public key
         * from Bob's encoded key material.
         */
        DHMessage bankPubKeyMessage = (DHMessage) TransportFactory.receiveMessage(is);
        assert bankPubKeyMessage != null;
        byte[] bankPubKeyEnc = bankPubKeyMessage.getKey();
        KeyFactory atmKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bankPubKeyEnc);
        PublicKey bankPubKey = atmKeyFac.generatePublic(x509KeySpec);
        atmKeyAgree.doPhase(bankPubKey, true);

        /*
         * At this stage, both ATM and Bank have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] atmSharedSecret = atmKeyAgree.generateSecret();

        /*
         * Now let's create a SecretKey object using the shared secret
         * and use it for encryption. First, we generate SecretKeys for the
         * "AES" algorithm (based on the raw shared secret data) and
         * Then we use AES in CBC mode, which requires an initialization
         * vector (IV) parameter. Note that you have to use the same IV
         * for encryption and decryption: If you use a different IV for
         * decryption than you used for encryption, decryption will fail.
         *
         * If you do not specify an IV when you initialize the Cipher
         * object for encryption, the underlying implementation will generate
         * a random one, which you have to retrieve using the
         * javax.crypto.Cipher.getParameters() method, which returns an
         * instance of java.security.AlgorithmParameters. You need to transfer
         * the contents of that object (e.g., in encoded format, obtained via
         * the AlgorithmParameters.getEncoded() method) to the party who will
         * do the decryption. When initializing the Cipher for decryption,
         * the (reinstantiated) AlgorithmParameters object must be explicitly
         * passed to the Cipher.init() method.
         */
        SecretKeySpec atmAesKey = new SecretKeySpec(atmSharedSecret, 0, 32, "AES");
        System.out.println(Arrays.toString(atmAesKey.getEncoded()));
        return atmAesKey;

    }

    public SecretKeySpec DHExchangeBank() throws Exception {

        DHMessage atmPubKeyMessage = (DHMessage) TransportFactory.receiveMessage(is);
        assert atmPubKeyMessage != null;
        byte[] atmPubKeyEnc = atmPubKeyMessage.getKey();
        /*
         * Bank has received ATM's public key in encoded format.
         * He instantiates a DH public key from the encoded key material.
         */
        KeyFactory bankKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(atmPubKeyEnc);

        PublicKey atmPubKey = bankKeyFac.generatePublic(x509KeySpec);

        /*
         * Bank gets the DH parameters associated with ATM's public key.
         * He must use the same parameters when he generates his own key
         * pair.
         */
        DHParameterSpec dhParamFromAtmPubKey = ((DHPublicKey)atmPubKey).getParams();

        // Bank creates his own DH key pair
        KeyPairGenerator bankKpairGen = KeyPairGenerator.getInstance("DH");
        bankKpairGen.initialize(dhParamFromAtmPubKey);
        KeyPair bankKpair = bankKpairGen.generateKeyPair();

        // Bank creates and initializes his DH KeyAgreement object
        KeyAgreement bankKeyAgree = KeyAgreement.getInstance("DH");
        bankKeyAgree.init(bankKpair.getPrivate());

        // Bank encodes his public key, and sends it over to ATM.
        byte[] bankPubKeyEnc = bankKpair.getPublic().getEncoded();
        DHMessage bankPubKeyMessage = new DHMessage(bankPubKeyEnc);
        TransportFactory.sendMessage(bankPubKeyMessage, os);

        /*
         * Bank uses ATM's public key for the first (and only) phase
         * of his version of the DH
         * protocol.
         */
        bankKeyAgree.doPhase(atmPubKey, true);

        /*
         * At this stage, both ATM and Bank have completed the DH key
         * agreement protocol.
         * Both generate the (same) shared secret.
         */
        byte[] bankSharedSecret = bankKeyAgree.generateSecret();

        /*
         * Now let's create a SecretKey object using the shared secret
         * and use it for encryption. First, we generate SecretKeys for the
         * "AES" algorithm (based on the raw shared secret data) and
         * Then we use AES in CBC mode, which requires an initialization
         * vector (IV) parameter. Note that you have to use the same IV
         * for encryption and decryption: If you use a different IV for
         * decryption than you used for encryption, decryption will fail.
         *
         * If you do not specify an IV when you initialize the Cipher
         * object for encryption, the underlying implementation will generate
         * a random one, which you have to retrieve using the
         * javax.crypto.Cipher.getParameters() method, which returns an
         * instance of java.security.AlgorithmParameters. You need to transfer
         * the contents of that object (e.g., in encoded format, obtained via
         * the AlgorithmParameters.getEncoded() method) to the party who will
         * do the decryption. When initializing the Cipher for decryption,
         * the (reinstantiated) AlgorithmParameters object must be explicitly
         * passed to the Cipher.init() method.
         */
        SecretKeySpec bankAesKey = new SecretKeySpec(bankSharedSecret, 0, 32, "AES");
        System.out.println(Arrays.toString(bankAesKey.getEncoded()));
        return bankAesKey;
    }

}