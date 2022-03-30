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
package bank;

import messages.DHMessage;

import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

public class DH {

    private final InputStream is;
    private final OutputStream os;
    private byte[] publicParameters;
    private Key atmPubKey;
    private KeyPair kPair;


    public DH(InputStream is, OutputStream os) throws InvalidKeySpecException, InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {
        this.is = is;
        this.os = os;

        kPair = DHKeyPair.getInstance().getKeyPair();
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
        this.atmPubKey = bankKeyFac.generatePublic(x509KeySpec);

        keyAgreement.doPhase(atmPubKey, true);
        byte[] bankSharedSecret = keyAgreement.generateSecret();
        return new SecretKeySpec(bankSharedSecret, 0, 16, "AES");
    }

}