// version control system in java

import java.io.*;
import java.util.*;
import errors.*;

class VCSHANDLER {
    public static void initRepository() {
        File kiwiDir = new File(".kiwi");
        try {
            if (kiwiDir.exists()) {
                throw new RepoAlreadyExistsException("Repository already initialized in this directory!");
            }
            if (kiwiDir.mkdir()) {
                new File(".kiwi/objects").mkdirs();
                new File(".kiwi/commits").mkdirs();
                new File(".kiwi/index").mkdirs();

                // Create HEAD file
                File headFile = new File(".kiwi/HEAD");
                headFile.createNewFile();

                System.out.println("Initialized empty KIWI repository in " + kiwiDir.getAbsolutePath());
            } else {
                System.err.println("Failed to create .kiwi directory!");
            }

        } catch (RepoAlreadyExistsException e) {
            System.err.println("[KIWI ERROR] " + e.getMessage());
        } catch (IOException e) {
            System.err.println("[KIWI ERROR] Could not initialize repository: " + e.getMessage());
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
            System.out.println("No command provided...\n Try 'kiwi init', 'kiwi add', or 'kiwi commit.. ^3^'");
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
                System.out.println("Unknown command: " + command);
        }
    }
}
