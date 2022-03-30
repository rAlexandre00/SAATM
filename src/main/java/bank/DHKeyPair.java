package bank;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class DHKeyPair {

    private KeyPair kPair;
    private KeyPairGenerator kPairGen;

    private static DHKeyPair instance = new DHKeyPair();

    public static DHKeyPair getInstance() {
        return instance;
    }

    private DHKeyPair() {

        new Thread(() -> {
            while(true) {
                try {
                    kPairGen = KeyPairGenerator.getInstance("DH");
                    kPairGen.initialize(2048);
                    kPair = kPairGen.generateKeyPair();
                    Thread.sleep(5000);
                } catch (InterruptedException | NoSuchAlgorithmException ignored) {}
            }
        }).start();

    }

    public KeyPair getKeyPair() {
        while(kPair == null) {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return kPair;
    }
}
