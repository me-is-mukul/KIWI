package src;
import errors.*;
import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import utils.*;

class Helper{
    protected static String updateIndex(String content, String filename, String hash) {
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

    protected static String normalizePath(String path) {
        try {
            return new File(path).getCanonicalPath().replace("\\", "/").trim();
        } catch (IOException e) {
            return path.replace("\\", "/").trim();
        }
    }

    protected static void removeDeletedFilesFromIndex() {
        File indexFile = new File(".kiwi/index/stage.index");
        if (!indexFile.exists()) return;

        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());
            StringBuilder updatedContent = new StringBuilder();

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(" ", 2);
                if (parts.length < 2) continue;

                String filename = normalizePath(parts[0]);
                String hash = parts[1].trim();
                File f = new File(filename);

                if (!f.exists()) {
                    File objectFile = new File(".kiwi/objects/" + hash);
                    if (objectFile.exists()) {
                        try {
                            Files.delete(objectFile.toPath());
                        } catch (IOException ex) {
                            System.err.println(Colors.YELLOW + "[KIWI WARNING] Could not delete old object: " + ex.getMessage() + Colors.RESET);
                        }
                    }
                } else {
                    updatedContent.append(filename).append(" ").append(hash).append("\n");
                }
            }
            Files.writeString(indexFile.toPath(), updatedContent.toString());

        } catch (IOException e) {
            System.err.println(Colors.RED + "[KIWI ERROR] Failed to clean deleted files from index: " + e.getMessage() + Colors.RESET);
        }
    }

    protected static void addSingleFile(String filename) {
        File file = new File(filename);

        if (!file.exists() || file.isDirectory()) {
            System.err.println(Colors.RED + "[KIWI ERROR] Skipping invalid file: " + filename + Colors.RESET);
            return;
        }

        try {
            String hash = HashUtils.getFileHash(file);
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
            if (oldHash != null && !oldHash.equals(hash)) {
                File oldObjectFile = new File(".kiwi/objects/" + oldHash);
                if (oldObjectFile.exists()) {
                    try {
                        Files.delete(oldObjectFile.toPath());
                    } catch (IOException ex) {
                        System.err.println(Colors.YELLOW + "[KIWI WARNING] Could not delete old object: " + ex.getMessage() + Colors.RESET);
                    }
                }
            }
            File objectFile = new File(".kiwi/objects/" + hash);
          
            Files.copy(file.toPath(), objectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
          
            String updatedContent = updateIndex(existingContent, filename, hash);
          
            Files.writeString(indexFile.toPath(), updatedContent);

        } catch (IOException | NoSuchAlgorithmException e) {
            System.err.println(Colors.RED + "[KIWI ERROR] Could not stage file: " + e.getMessage() + Colors.RESET);
        }
    }

    protected static void addAllFilesRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String name = file.getName();
            if (name.equals(".kiwi") || name.startsWith(".")) continue;

            if (file.isDirectory()) {
                addAllFilesRecursively(file);
            } else {
                addSingleFile(file.getPath());
            }
        }
    }
    
}

class VCSHANDLER extends Helper {
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
        if (!new File(".kiwi").exists()) {
            System.err.println(Colors.RED + "[KIWI ERROR] Repository not initialized.\nRun 'kiwi init' first." + Colors.RESET);
            return;
        }
        removeDeletedFilesFromIndex();
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

    public static void status(File dir, Map<String, String> indexMap, ArrayList<String> deletedfiles, ArrayList<String> modified, ArrayList<String> untracked) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for(File file : files){
            String name = file.getName();
            if(name.equals(".kiwi") || name.startsWith(".")) continue;
            if(file.isDirectory()){
                status(file, indexMap,deletedfiles, modified, untracked);
            } else {
                try {
                String normalizedPath = normalizePath(file.getPath());
                String hash = HashUtils.getFileHash(file);

                if (indexMap.containsKey(normalizedPath)) {
                    String storedHash = indexMap.get(normalizedPath);
                    if (!storedHash.equals(hash)) {
                        modified.add(file.getName());
                    }
                } else {
                    untracked.add(file.getName());
                }

            } catch (IOException | NoSuchAlgorithmException e) {
                System.err.println("[KIWI ERROR] Could not hash file: " + file.getName());
            }
            }
        }
    }

    public static void status() {
        if (!new File(".kiwi").exists()) {
            System.err.println(Colors.RED + "[KIWI ERROR] Repository not initialized.\nRun 'kiwi init' first." + Colors.RESET);
            return;
        }
        File indexFile = new File(".kiwi/index/stage.index");

        if (!indexFile.exists()) {
            System.out.println(Colors.RED + "No files have been staged yet!" + Colors.RESET);
            return;
        }
        Map<String, String> indexMap = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] parts = line.split(" ", 2);
                if (parts.length < 2) continue;
                String filename = normalizePath(parts[0]);
                String hash = parts[1].trim();
                indexMap.put(filename, hash);
            }
        } catch (IOException e) {
            System.err.println("[KIWI ERROR] Could not read index file.");
            return;
        }
        ArrayList<String> modified = new ArrayList<>();
        ArrayList<String> untracked = new ArrayList<>();
        ArrayList<String> deletedfiles = new ArrayList<>();
        for (String filename : indexMap.keySet()) {
            File f = new File(filename);
            if (!f.exists()) {
                deletedfiles.add(f.getName());
            }
        }
        status(new File("."), indexMap,deletedfiles ,modified, untracked);
        System.out.println();
        System.out.println(Colors.CYAN + "======================================" + Colors.RESET);
        if (modified.isEmpty() && untracked.isEmpty()) {
            System.out.println(Colors.GREEN + "ALL GOOD" + Colors.RESET);
        }
        if (!modified.isEmpty()) {
                System.out.println(Colors.YELLOW + "\nModified files:" + Colors.RESET);
                for (String file : modified)
                    System.out.println(Colors.YELLOW+"   " + file+Colors.RESET);
        }if (!deletedfiles.isEmpty()) {
            System.out.println(Colors.BLUE + "\ndeleted files:" + Colors.RESET);
            for (String file : deletedfiles)
                System.out.println(Colors.BLUE+"   " + file+Colors.RESET);
        }
        if (!untracked.isEmpty()) {
            System.out.println(Colors.RED + "\nUntracked files:" + Colors.RESET);
            for (String file : untracked)
                System.out.println(Colors.RED+"   " + file+Colors.RESET);
        }
        System.out.println(Colors.CYAN + "======================================" + Colors.RESET);
    }

    public static void log() {
        System.out.println("Displaying commit logs");
    }

    public static void commit(String[] args) {
        System.out.println("Committed changes to repository");
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
