package atm;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Parser {

    ArgumentParser ap = ArgumentParsers.newFor("Bank").build().defaultHelp(true);

    public Parser() {
        ap.addArgument("-a").required(true).help("The customer's account name.\n");
        ap.addArgument("-p").setDefault("3000").help("Port which Bank is listening to.\n");
        ap.addArgument("-s").setDefault("bank.auth").help("Bank's Auth File\n");
        ap.addArgument("-i").setDefault("127.0.0.1").help("IP of Bank\n");
        ap.addArgument("-c").help("The customer's ATM Card file.\n");
        ap.addArgument("-n").help("Creates a new account with the given balance:\n\t -a <accountName> -n <balance>\n");
        ap.addArgument("-d").help("Deposit the amount of money specified:\n\t -a <accountName> -d <amount>\n");
        ap.addArgument("-w").help("Withdraw the amount of money specified:\n\t -a <accountName> -w <amount>\n");
        ap.addArgument("-g").nargs("?").setConst("x").help("Get the current balance of the account:\n\t -a <accountName> -g\n");
    }

    public Namespace parseArguments(String[] args) throws ArgumentParserException {
        Namespace ns = ap.parseArgs(args);
        if(ns.getString("c") == null) {
            ap.setDefault("c", ns.getString("a") + ".card");
        }
        return ap.parseArgs(args);
    }

}
