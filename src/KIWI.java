package src;

import errors.*;
import java.io.*;
import java.nio.file.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import utils.*;

class Helper {

    protected static String updateIndex(String content, String filename, String hash) {
        StringBuilder sb = new StringBuilder();
        boolean replaced = false;
        String normalizedFile = normalizePath(filename);

        for (String line : content.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split(" ", 2);
            if (parts.length < 2) {
                continue;
            }

            String existingFile = normalizePath(parts[0]);

            if (existingFile.equals(normalizedFile)) {
                sb.append(normalizedFile).append(" ").append(hash).append("\n");
                replaced = true;
            } else {
                sb.append(line).append("\n");
            }
        }

        if (!replaced) {
            sb.append(normalizedFile).append(" ").append(hash).append("\n");
        }
        return sb.toString();
    }

    protected static String normalizePath(String path) {
        try {
            return new File(path).getCanonicalPath().replace("\\", "/").trim();
        } catch (IOException e) {
            return path.replace("\\", "/").trim();
        }
    }

    protected static void removeDeletedFilesFromIndex() throws IndexCorruptedException {
        File indexFile = new File(".kiwi/index/stage.index");
        if (!indexFile.exists()) {
            return;
        }

        try {
            List<String> lines = Files.readAllLines(indexFile.toPath());
            StringBuilder updatedContent = new StringBuilder();

            for (String line : lines) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] parts = line.split(" ", 2);
                if (parts.length < 2) {
                    continue;
                }

                String filename = normalizePath(parts[0]);
                String hash = parts[1].trim();
                File f = new File(filename);

                if (!f.exists()) {
                    File objectFile = new File(".kiwi/objects/" + hash);
                    if (objectFile.exists()) {
                        Files.deleteIfExists(objectFile.toPath());
                    }
                } else {
                    updatedContent.append(filename).append(" ").append(hash).append("\n");
                }
            }
            Files.writeString(indexFile.toPath(), updatedContent.toString());

        } catch (IOException e) {
            throw new IndexCorruptedException(e.getMessage());
        }
    }

    protected static void addSingleFile(String filename) throws FileStagingException, ObjectWriteException {
        File file = new File(filename);

        if (!file.exists() || file.isDirectory()) {
            throw new FileStagingException(filename, "File does not exist or is a directory.");
        }

        try {
            String hash = HashUtils.getFileHash(file);
            File indexFile = new File(".kiwi/index/stage.index");
            indexFile.createNewFile();

            String existingContent = "";
            if (indexFile.length() > 0) {
                existingContent = Files.readString(indexFile.toPath());
            }

            String normalizedFile = normalizePath(filename);
            String oldHash = null;
            for (String line : existingContent.split("\n")) {
                String l = line.trim();
                if (l.isEmpty()) {
                    continue;
                }
                String[] parts = l.split(" ", 2);
                if (parts.length < 2) {
                    continue;
                }
                String existingFile = normalizePath(parts[0]);
                if (existingFile.equals(normalizedFile)) {
                    oldHash = parts[1].trim();
                    break;
                }
            }

            // delete old object if file changed
            if (oldHash != null && !oldHash.equals(hash)) {
                File oldObjectFile = new File(".kiwi/objects/" + oldHash);
                Files.deleteIfExists(oldObjectFile.toPath());
            }

            File objectFile = new File(".kiwi/objects/" + hash);
            try {
                Files.copy(file.toPath(), objectFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                throw new ObjectWriteException(hash, e.getMessage());
            }

            String updatedContent = updateIndex(existingContent, filename, hash);
            Files.writeString(indexFile.toPath(), updatedContent);

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new FileStagingException(filename, e.getMessage());
        }
    }

    protected static void addAllFilesRecursively(File dir) throws FileStagingException, ObjectWriteException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String name = file.getName();
            if (name.equals(".kiwi") || name.startsWith(".")) {
                continue;
            }

            if (file.isDirectory()) {
                addAllFilesRecursively(file);
            } else {
                addSingleFile(file.getPath());
            }
        }
    }

    protected static String formatTimestamp(long millis) {
        java.text.SimpleDateFormat sdf
                = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(java.util.TimeZone.getDefault()); // your local timezone
        return sdf.format(new java.util.Date(millis));
    }

    protected static String addContent(File folder) throws IOException {
        StringBuilder sb = new StringBuilder();
        File[] files = folder.listFiles();
        if (files == null) return "";

        for (File file : files) {
            if (file.isDirectory()) {
                sb.append(addContent(file));
            } else {
                sb.append(file.getName());
            }
        }
        return sb.toString();
    }

    protected static void copyFolder(File src, File dest) throws IOException {
        Path source = src.toPath();
        Path target = dest.toPath();

        Files.walk(source).forEach(path -> {
            try {
                Path relative = source.relativize(path);
                Path destination = target.resolve(relative);

                if (Files.isDirectory(path)) {
                    Files.createDirectories(destination);
                } else {
                    Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


}

class VCSHANDLER extends Helper {

    public static void initRepository() throws RepoAlreadyExistsException, CommitException {
        File kiwiDir = new File(".kiwi");

        try {
            if (kiwiDir.exists()) {
                throw new RepoAlreadyExistsException("Repository already initialized in this directory!");
            }
            if (!kiwiDir.mkdir()) {
                throw new CommitException("Failed to create .kiwi directory!");
            }

            new File(".kiwi/objects").mkdirs();
            new File(".kiwi/commits").mkdirs();
            new File(".kiwi/index").mkdirs();

            new File(".kiwi/HEAD").createNewFile();

            System.out.println(Colors.GREEN + "Initialized KIWI repository" + Colors.RESET);

        } catch (IOException e) {
            throw new CommitException("Could not initialize repository: " + e.getMessage());
        }
    }

    public static void add(String[] args)
            throws RepoNotInitializedException, FileStagingException, ObjectWriteException, IndexCorruptedException {

        if (!new File(".kiwi").exists()) {
            throw new RepoNotInitializedException();
        }

        removeDeletedFilesFromIndex();

        if (args.length < 2) {
            System.out.println(Colors.YELLOW + "Usage: kiwi add <filename> [more_files] OR kiwi add ." + Colors.RESET);
            return;
        }

        if (args[1].equals(".")) {
            addAllFilesRecursively(new File("."));
            System.out.println(Colors.GREEN + "Staging completed." + Colors.RESET);
            return;
        }

        for (int i = 1; i < args.length; i++) {
            addSingleFile(args[i]);
        }
        System.out.println(Colors.GREEN + "Staging completed." + Colors.RESET);
    }

    public static void status(File dir, Map<String, String> indexMap,
            ArrayList<String> deletedfiles, ArrayList<String> modified, ArrayList<String> untracked) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            String name = file.getName();
            if (name.equals(".kiwi") || name.startsWith(".")) {
                continue;
            }
            if (file.isDirectory()) {
                status(file, indexMap, deletedfiles, modified, untracked);
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

    public static void status() throws RepoNotInitializedException, IndexCorruptedException {
        if (!new File(".kiwi").exists()) {
            throw new RepoNotInitializedException();
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
                if (line.isEmpty()) {
                    continue;
                }
                String[] parts = line.split(" ", 2);
                if (parts.length < 2) {
                    continue;
                }
                String filename = normalizePath(parts[0]);
                String hash = parts[1].trim();
                indexMap.put(filename, hash);
            }
        } catch (IOException e) {
            throw new IndexCorruptedException(e.getMessage());
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

        status(new File("."), indexMap, deletedfiles, modified, untracked);

        System.out.println();
        System.out.println(Colors.CYAN + "======================================" + Colors.RESET);

        if (modified.isEmpty() && untracked.isEmpty()) {
            System.out.println(Colors.GREEN + "ALL GOOD" + Colors.RESET);
        }

        if (!modified.isEmpty()) {
            System.out.println(Colors.YELLOW + "\nModified files:" + Colors.RESET);
            for (String file : modified) {
                System.out.println(Colors.YELLOW + "   " + file + Colors.RESET);
            }
        }

        if (!deletedfiles.isEmpty()) {
            System.out.println(Colors.BLUE + "\nDeleted files:" + Colors.RESET);
            for (String file : deletedfiles) {
                System.out.println(Colors.BLUE + "   " + file + Colors.RESET);
            }
        }

        if (!untracked.isEmpty()) {
            System.out.println(Colors.RED + "\nUntracked files:" + Colors.RESET);
            for (String file : untracked) {
                System.out.println(Colors.RED + "   " + file + Colors.RESET);
            }
        }

        System.out.println(Colors.CYAN + "======================================" + Colors.RESET);
    }

    
    public static void commit(String[] args)
            throws RepoNotInitializedException, CommitException {

        try {
            if (!new File(".kiwi").exists()) {
                throw new RepoNotInitializedException();
                }
            
                if (args.length < 2) {
                    System.out.println(Colors.YELLOW + ("Commit message not provided!") + Colors.RESET);
                    return;
                }

                long timestampMillis = System.currentTimeMillis();
                String Readable_time = formatTimestamp(timestampMillis);

                String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));


                String content = addContent(new File(".kiwi/objects"));
                String commitHash = CommitHashUtils.generateCommitHash(content);
                String CommitName = commitHash + " " + message + " " + Readable_time;
                
                File commitDir = new File(".kiwi/commits"+"/"+CommitName);
                commitDir.mkdirs();

                File commitObjectsDir = new File(".kiwi/commits/"+CommitName+"/objects");
                File commitIndexDir = new File(".kiwi/commits/"+CommitName+"/index");

                commitObjectsDir.mkdirs();
                commitIndexDir.mkdirs();

                copyFolder(new File(".kiwi/objects"), commitObjectsDir);
                copyFolder(new File(".kiwi/index"), commitIndexDir);

            }
            catch(Exception e)
        {
            throw new CommitException(e.getMessage()); 
        }
        }

    public static void log() {
        try {
            File commitsDir = new File(".kiwi/commits");

            File[] commitFolders = commitsDir.listFiles(File::isDirectory);
            if (commitFolders == null || commitFolders.length == 0) {
                System.out.println(Colors.YELLOW + "No commits yet!" + Colors.RESET);
                return;
            }
            Arrays.sort(commitFolders, Comparator.comparing(File::getName).reversed());

            System.out.println(Colors.CYAN + "\n ============ KIWI COMMIT HISTORY ============" + Colors.RESET);

            for (File folder : commitFolders) {

                String name = folder.getName().trim();

                String[] parts = name.split(" ", 3);
                if (parts.length < 3) {
                    continue;
                }

                String hash = parts[0].trim();
                String message = parts[1].trim();
                String date = parts[2].trim();

                System.out.println("Hash:     " + Colors.CYAN + hash + Colors.RESET);
                System.out.println("Timestamp:    " + Colors.MAGENTA + date + Colors.RESET);
                System.out.println("Message:  " + Colors.YELLOW + message + Colors.RESET);
                System.out.println(Colors.CYAN + " =============================================\n" + Colors.RESET);
            }

        } catch (Exception e) {
            System.out.println(Colors.RED + "[KIWI ERROR] Could not read log: " + e.getMessage() + Colors.RESET);
        }
    }
}
public class KIWI {
    public static void main(String[] args) {
        VCSHANDLER vcs = new VCSHANDLER();
        try {
            if (args.length == 0) {
                throw new InvalidCommandException("No command provided. Try 'kiwi init', 'kiwi add', or 'kiwi commit'.");
            }
            String command = args[0];

            switch (command) {
                case "init" ->
                    vcs.initRepository();
                case "status" ->
                    vcs.status();
                case "add" ->
                    vcs.add(args);
                case "commit" ->
                    vcs.commit(args);
                case "log" ->
                    vcs.log();
                default ->
                    throw new InvalidCommandException(command);
            }
        } catch (KiwiException e) {
            System.err.println(Colors.RED + "[KIWI ERROR] " + e.getMessage() + Colors.RESET);
        } catch (Exception e) {
            System.err.println(Colors.RED + "[SYSTEM ERROR] " + e.getMessage() + Colors.RESET);
        }
    }
}
