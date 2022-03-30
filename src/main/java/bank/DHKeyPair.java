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
                    kPair = kPairGen.generateKeyPair();
                    System.out.println("generated new keypair!");
                    Thread.sleep(5000);
                } catch (InterruptedException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        System.out.println("tao");
    }

    public KeyPair getKeyPair() {
        while(kPair == null) {
            try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        }
        return kPair;
    }
}
