package com.ELang.ELang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    static String path = "";
    static boolean debug = false;

    public static void main(String[] args) throws IOException {

        // Parse command line arguments
        for (String arg : args) {
            switch (arg) {
                case "-v":
                case "--version":
                    Version(); // Print the version and exit
                    break;
                case "-d":
                case "--debug":
                    debug = true; // Enable debug mode
                    break;
                default:
                    // Path can be passed without --arg
                    if (path.equals("")) {
                        path = arg; // Set path
                    } else {
                        // Print "Unknown argument '{given argument}'" and exit
                        System.err.println("Unknown argument '".concat(arg).concat("'"));
                        System.exit(-1);
                    }
                    break;
            }
        }

        // If debug was enabled with the --debug flag
        if (debug) {
            System.out.println("Starting file '".concat(path).concat("':")); // Print given path
        }

        File file = new File(path); // Create file object from given path

        // Check if file exists, and is a file
        if (file.exists() && !file.isDirectory()) {
            String fileString = Files.readString(Path.of(file.getAbsolutePath())); // Read the file into a string

            // If debug was enabled with the --debug flag
            if (debug) {
                System.out.println("\n".concat(fileString).concat("\n")); // Print the file contents
            }

            // Make sure the given file is an E file
            if (fileString.contains("##E##\n") || fileString.contains("##E##\r\n") || fileString.contains("##E##\r")) {
                Interpreter interpreter = new Interpreter(fileString.replace("##E##\n", "").replace("##E##\r\n", "").replace("##E##\r", ""), debug, new File(System.getProperty("user.dir")).toURI().relativize(file.toURI()).toString()); // Create a new Interpreter instance
            } else {
                // Print "Input file is not an E file, or is missing '##E##'" and exit
                System.err.println("Input file is not an E file, or is missing '##E##'");
                System.exit(-1);
            }
        } else {
            // Print "No such file '{input file}'" and exit
            System.err.println("No such file '".concat(path).concat("'"));
            System.exit(-1);
        }
    }

    // Print the version and exit
    static void Version() {
        System.out.println("3E v".concat(Info.VERSION.toString()));
        System.exit(0);
    }
}