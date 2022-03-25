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
package atm;
import messages.DHMessage;
import utils.KeyAndIV;


import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;
import java.security.spec.*;
import javax.crypto.*;
import javax.crypto.spec.*;

public class DH {

    private final InputStream is;
    private final OutputStream os;
    private byte[] publicParameters;
    private KeyAgreement keyAgreement;

    public DH(InputStream is, OutputStream os) throws InvalidKeyException {
        this.is = is;
        this.os = os;

        try {
            KeyPairGenerator kPairGen = KeyPairGenerator.getInstance("DH");
            kPairGen.initialize(2048);
            KeyPair kPair = kPairGen.generateKeyPair();
            keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(kPair.getPrivate());
            publicParameters = kPair.getPublic().getEncoded();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    public byte[] getPublicParameters() {
        return publicParameters;
    }

    public KeyAndIV getEncryptionParams(DHMessage dhMessageFromBank) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {

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