package com.ELang.ELang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static String path = "";
    static boolean debug = false;

    public static void main(String[] args) throws IOException {
        for (String arg : args) {
            switch (arg) {
                case "-v":
                case "--version":
                    Version();
                    break;
                case "-d":
                case "--debug":
                    debug = true;
                    break;
                default:
                    if (path.equals("")) {
                        path = arg;
                    } else {
                        System.err.println("Unknown argument '".concat(arg).concat("'."));
                        System.exit(-1);
                    }
                    break;
            }
        }

        if (debug) {
            System.out.println("Starting file '".concat(path).concat("'."));
        }

        File file = new File(path);

        if (file.exists() && !file.isDirectory()) {
            String fileString = Files.readString(Path.of(file.getAbsolutePath()));
            System.out.println("\n".concat(fileString).concat("\n"));
            if (!fileString.contains("##3E##\n") && !fileString.contains("##E##\r\n") && !fileString.contains("##3E##\r")) {
                System.err.println("Input file is not an E file, or is missing '##E##'");
            } else {
                Interpreter interpreter = new Interpreter(fileString.replace("##E##\n", "").replace("##3E##\r\n", "").replace("##3E##\r", ""), debug);
            }
        } else {
            System.err.println("No such file '".concat(path).concat("'"));
            System.exit(-1);
        }
    }

    static void Version() {
        System.out.println("3E v".concat(Info.VERSION.toString()));
        System.exit(0);
    }
}