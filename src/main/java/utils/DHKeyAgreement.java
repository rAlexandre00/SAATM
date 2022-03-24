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
import messages.DHIVMessage;
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

    public KeyAndIV DHExchangeATM(Key pubKeyBank) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

        KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("DH");
        kPairGen.initialize(2048);
        KeyPair kPair = kPairGen.generateKeyPair();

        KeyAgreement atmKeyAgree = KeyAgreement.getInstance("DH");
        atmKeyAgree.init(kPair.getPrivate());

        byte[] pubKeyEnc = kPair.getPublic().getEncoded();

        DHMessage atmPubKeyMessage = new DHMessage(pubKeyEnc);
        TransportFactory.sendMessage(atmPubKeyMessage, os);

        DHIVMessage bankPubKeyMessage = (DHIVMessage) TransportFactory.receiveMessage(is);

        assert bankPubKeyMessage != null;
        if(!bankPubKeyMessage.verifyChecksum(pubKeyBank)) {
            System.err.println("DHParameters checksum is not valid");
            System.exit(63);
        }

        byte[] bankPubKeyEnc = bankPubKeyMessage.getKey();
        IvParameterSpec iv = bankPubKeyMessage.getIV();
        KeyFactory atmKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bankPubKeyEnc);
        PublicKey bankPubKey = atmKeyFac.generatePublic(x509KeySpec);
        atmKeyAgree.doPhase(bankPubKey, true);

        byte[] atmSharedSecret = atmKeyAgree.generateSecret();

        SecretKeySpec atmAesKey = new SecretKeySpec(atmSharedSecret, 0, 32, "AES");
        System.out.println(Arrays.toString(atmAesKey.getEncoded()));
        return new KeyAndIV(atmAesKey, iv);

    }

    public SecretKeySpec DHExchangeBank(byte[] iv, Key privKey) throws Exception {

        DHMessage atmPubKeyMessage = (DHMessage) TransportFactory.receiveMessage(is);
        assert atmPubKeyMessage != null;
        byte[] atmPubKeyEnc = atmPubKeyMessage.getDHParams();

        KeyFactory bankKeyFac = KeyFactory.getInstance("DH");
        X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(atmPubKeyEnc);

        PublicKey atmPubKey = bankKeyFac.generatePublic(x509KeySpec);

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
        DHIVMessage bankPubKeyMessage = new DHIVMessage(bankPubKeyEnc, iv, privKey);
        TransportFactory.sendMessage(bankPubKeyMessage, os);

        bankKeyAgree.doPhase(atmPubKey, true);

        byte[] bankSharedSecret = bankKeyAgree.generateSecret();

        SecretKeySpec bankAesKey = new SecretKeySpec(bankSharedSecret, 0, 32, "AES");
        System.out.println(Arrays.toString(bankAesKey.getEncoded()));
        return bankAesKey;
    }

}