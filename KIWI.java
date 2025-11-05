// version control system in java

import java.util.*;

class VCSHANDLER {
    public static void initRepository() {
        System.out.println("Initialized empty KIWI repository");
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
            default:
                System.out.println("Unknown command: " + command);
        }
    }
}
