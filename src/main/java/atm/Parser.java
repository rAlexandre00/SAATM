package atm;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Parser {

    ArgumentParser ap = ArgumentParsers.newFor("Bank").build().defaultHelp(true);

    public Parser() {
        ap.addArgument("-a").required(true);
        ap.addArgument("-p").setDefault("3000");
        ap.addArgument("-s").setDefault("bank.auth");
        ap.addArgument("-i").setDefault("127.0.0.1");
        ap.addArgument("-c");
        ap.addArgument("-n");
        ap.addArgument("-d");
        ap.addArgument("-w");
        ap.addArgument("-g");
    }

    public Namespace parseArguments(String[] args) throws ArgumentParserException {
        Namespace ns = ap.parseArgs(args);
        if(ns.getString("c") == null) {
            ap.setDefault("c", ns.getString("a") + ".card");
        }
        return ap.parseArgs(args);
    }

}
