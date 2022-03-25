package bank;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class Parser {

    final ArgumentParser ap = ArgumentParsers.newFor("Bank").build().defaultHelp(true);

    public Parser() {
        ap.addArgument("-p").setDefault("3000");
        ap.addArgument("-s").setDefault("bank.auth");
    }

    public Namespace parseArguments(String[] args) throws ArgumentParserException {
        return ap.parseArgs(args);
    }

}
