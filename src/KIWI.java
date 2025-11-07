package src;
import errors.*;
import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import utils.*;

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
    
    private static String updateIndex(String content, String filename, String hash) {
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
        String normalizedFile = normalizePath(filename);

        for (String line : content.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(" ", 2);
            if (parts.length < 2) continue;

            String existingFile = normalizePath(parts[0]);

            if (existingFile.equals(normalizedFile)) {
                sb.append(normalizedFile).append(" ").append(hash).append("\n");
                replaced = true;
            } else {
                sb.append(line).append("\n");
            }
        }

        if (!replaced) sb.append(normalizedFile).append(" ").append(hash).append("\n");
        return sb.toString();
    }

    private static String normalizePath(String path) {
        try {
            return new File(path).getCanonicalPath().replace("\\", "/").trim();
        } catch (IOException e) {
            return path.replace("\\", "/").trim();
        }
    }

    private static void addSingleFile(String filename) {
        File file = new File(filename);

        if (!file.exists() || file.isDirectory()) {
            System.err.println(Colors.RED + "[KIWI ERROR] Skipping invalid file: " + filename + Colors.RESET);
            return;
        }

        try {
            String hash = HashUtils.getFileHash(file);

            // Ensure index exists and read current content to find old hash (if any)
            File indexFile = new File(".kiwi/index/stage.index");
            indexFile.createNewFile();

            String existingContent = "";
            if (indexFile.length() > 0)
                existingContent = Files.readString(indexFile.toPath());

            String normalizedFile = normalizePath(filename);
            String oldHash = null;
            for (String line : existingContent.split("\n")) {
                String l = line.trim();
                if (l.isEmpty()) continue;
                String[] parts = l.split(" ", 2);
                if (parts.length < 2) continue;
                String existingFile = normalizePath(parts[0]);
                if (existingFile.equals(normalizedFile)) {
                    oldHash = parts[1].trim();
                    break;
                }
            }

            // If there is an old object for this file and the hash changed, delete the old object
            if (oldHash != null && !oldHash.equals(hash)) {
                File oldObjectFile = new File(".kiwi/objects/" + oldHash);
                if (oldObjectFile.exists()) {
                    try {
                        Files.delete(oldObjectFile.toPath());
                    } catch (IOException ex) {
                        // non-fatal: log but continue to write new object
                        System.err.println(Colors.YELLOW + "[KIWI WARNING] Could not delete old object: " + ex.getMessage() + Colors.RESET);
                    }
                }
            }

            // Write new object (replace if same hash file exists)
            File objectFile = new File(".kiwi/objects/" + hash);
            Files.copy(file.toPath(), objectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Update index with new hash for the file
            String updatedContent = updateIndex(existingContent, filename, hash);
            Files.writeString(indexFile.toPath(), updatedContent);

        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println(Colors.RED + "[KIWI ERROR] Could not stage file: " + e.getMessage() + Colors.RESET);
        }
    }

    private static void addAllFilesRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();

            // Skip .kiwi folder and hidden files
            if (name.equals(".kiwi") || name.startsWith(".")) continue;

            if (file.isDirectory()) {
                addAllFilesRecursively(file); // recursion
            } else {
                addSingleFile(file.getPath());
            }
        }
    }
    
    public static void add(String[] args) {
        if (!new File(".kiwi").exists()) {
            System.err.println(Colors.RED + "[KIWI ERROR] Repository not initialized.\nRun 'kiwi init' first." + Colors.RESET);
            return;
        }
        if (args.length < 2) {
            System.out.println(Colors.YELLOW + "Usage: kiwi add <filename> [more_files] OR kiwi add ." + Colors.RESET);
            return;
        }
        if (args[1].equals(".")) {
            addAllFilesRecursively(new File("."));
            System.out.println(Colors.GREEN+"Staging completed."+Colors.RESET);
            return;
        }
        for (int i = 1; i < args.length; i++) {
            addSingleFile(args[i]);
            System.out.println(Colors.GREEN+"Staging completed."+Colors.RESET);
        }
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
