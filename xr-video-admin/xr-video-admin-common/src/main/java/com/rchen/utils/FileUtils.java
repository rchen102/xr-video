package com.rchen.utils;

import java.io.File;

/**
 * @Author : crz
 */
public class FileUtils {
    // 删除文件
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            return true;
        }
        return false;
    }
}
