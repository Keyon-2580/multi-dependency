package cn.edu.fudan.se.multidependency.utils;

/**
 * @description:
 * @author: keyon
 * @time: 2022/10/21 10:01
 */

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

import java.io.File;
import java.util.Objects;

public class DirExplorer {
    private DirExplorer.Filter filter;
    private DirExplorer.FileHandler fileHandler;
    public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("win");
    private static final String WINDOWS_SEPARATOR = "\\\\";
    private static final String SINGLE_WINDOWS_SEPARATOR = "\\";
    private static final String LINUX_SEPARATOR = "/";

    public DirExplorer(DirExplorer.Filter filter, DirExplorer.FileHandler fileHandler) {
        this.filter = filter;
        this.fileHandler = fileHandler;
    }

    public void explore(File root) {
        this.explore(0, "", root);
    }

    private void explore(int level, String path, File file) {
        if (file.isDirectory()) {
            File[] var4 = (File[])Objects.requireNonNull(file.listFiles());
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                File child = var4[var6];
                this.explore(level + 1, path + "/" + child.getName(), child);
            }
        } else if (this.filter.filter(level, path, file)) {
            this.fileHandler.handle(level, path, file);
        }

    }

    public static Boolean fileFilter(String path) {
        if (path != null && !path.isEmpty()) {
            path = systemAvailablePath(path);
            return !path.toLowerCase().endsWith(".java") || path.toLowerCase().contains(systemAvailablePath("/test/")) || path.toLowerCase().contains(systemAvailablePath("/.mvn/")) || path.toLowerCase().endsWith("test.java") || path.toLowerCase().endsWith("tests.java") || path.toLowerCase().startsWith("test") || path.toLowerCase().endsWith("enum.java");
        } else {
            return true;
        }
    }

    public static String systemAvailablePath(String source) {
        if (!IS_WINDOWS && source.contains("\\\\")) {
            return source.replace("\\\\", "/");
        } else if (!IS_WINDOWS && source.contains("\\")) {
            return source.replace("\\", "/");
        } else if (IS_WINDOWS && !source.contains("\\\\") && source.contains("/")) {
            return source.replace("/", "\\\\");
        } else {
            return IS_WINDOWS && !source.contains("\\\\") && source.contains("\\") ? source.replace("\\", "\\\\") : source;
        }
    }

    public interface FileHandler {
        void handle(int var1, String var2, File var3);
    }

    public interface Filter {
        boolean filter(int var1, String var2, File var3);
    }
}
