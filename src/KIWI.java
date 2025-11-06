package src;
import java.io.*;
import java.util.*;
import errors.*;
import src.Colors;

class VCSHANDLER {
    public static void initRepository() {
        File kiwiDir = new File(".kiwi");
        try {
            if (kiwiDir.exists()) {
                throw new RepoAlreadyExistsException(Colors.BLUE+"Repository already initialized in this directory!"+Colors.RESET);
            }
            if (kiwiDir.mkdir()) {
                new File(".kiwi/objects").mkdirs();
                new File(".kiwi/commits").mkdirs();
                new File(".kiwi/index").mkdirs();

                // Create HEAD file
                File headFile = new File(".kiwi/HEAD");
                headFile.createNewFile();

                System.out.println(Colors.GREEN+"Initialized KIWI repository"+Colors.RESET);
            } else {
                System.err.println(Colors.RED+"Failed to create .kiwi directory!"+Colors.RESET);
            }

        } catch (RepoAlreadyExistsException e) {
            System.err.println(Colors.YELLOW+"[KIWI ERROR] " + e.getMessage()+Colors.RESET);
        } catch (IOException e) {
            System.err.println(Colors.YELLOW+"[KIWI ERROR] Could not initialize repository: " + e.getMessage()+Colors.RESET);
        }
    }


    public static void add(String[] args) {
        System.out.println("Added files to staging area");
    }

    public static void commit(String[] args) {
        System.out.println("Committed changes to repository");
    }

    public static void status() {
        System.out.println("Displaying files status");
    }
    public static void log() {
        System.out.println("Displaying commit logs");
    }
}

public class KIWI {
    public static void main(String[] args) {
        VCSHANDLER vcs = new VCSHANDLER();

        if (args.length == 0) {
            System.out.println(Colors.RED+"No command provided...\n Try 'kiwi init', 'kiwi add', or 'kiwi commit'\n UwU"+Colors.RESET);
            return;
        }

        String command = args[0];

        switch (command) {
            case "init":
                vcs.initRepository();
                break;
            case "status":
                vcs.status();
                break;
            case "add":
                vcs.add(args);
                break;
            case "commit":
                vcs.commit(args);
                break;
            case "log":
                vcs.log();
                break;
            default:
                System.out.println(Colors.YELLOW+"Unknown command: " + command+Colors.RESET);
        }
    }
}
