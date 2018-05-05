package com.hrrock.snapbook.utils;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by hp-u on 10/4/2017.
 */

public class FileSearch {
    public static ArrayList<String> getDirectoryPaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        if (listfiles != null) {
            for (int i = 0; i < listfiles.length; i++) {
                if (listfiles[i].isDirectory()) {
                    pathArray.add(listfiles[i].getAbsolutePath());
                }
            }
        }
        return pathArray;
    }

    public static ArrayList<String> getFilePaths(String directory) {
        ArrayList<String> pathArray = new ArrayList<>();
        File file = new File(directory);
        File[] listfiles = file.listFiles();
        for (int i = 0; i < listfiles.length; i++) {
            if (listfiles[i].isFile()) {
                pathArray.add(listfiles[i].getAbsolutePath());
            }
        }
        return pathArray;
    }
}

