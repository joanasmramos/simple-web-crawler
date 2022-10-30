package com.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for some file utilities.
 */
public class FileUtils {
    /**
     * Utility to get a FileWriter for a certain file.
     * 
     * @param filepath   File path to use.
     * @param appendMode Whether it should append or write/overwrite to the file.
     * @return FileWriter.
     */
    public static FileWriter getFileWriter(String filepath, boolean appendMode) {
        File file = new File(filepath);
        try {
            return new FileWriter(file, appendMode);
        } catch (IOException e) {
            System.out.println("[ERROR] Error creating FileWriter:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Utility to get a BufferedReader for a certain file.
     * 
     * @param filepath File path to use.
     * @return BufferedReader.
     */
    public static BufferedReader getBufferedReader(String filepath) {
        File file = new File(filepath);
        try {
            FileReader fr = new FileReader(file);
            return new BufferedReader(fr);
        } catch (IOException e) {
            System.out.println("[ERROR] Error creating BufferedReader:");
            e.printStackTrace();
            return null;
        }
    }

}
