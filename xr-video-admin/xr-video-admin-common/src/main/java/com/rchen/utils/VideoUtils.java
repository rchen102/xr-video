package com.rchen.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author : crz
 */
public class VideoUtils {

    private String ffmpegEXE;

    public VideoUtils(String ffmpegEXE) {
        this.ffmpegEXE = ffmpegEXE;
    }

    /**
     * 合并视频和音频
     * @param videoInputPath 源视频目录
     * @param bgmInputPath  BGM 目录
     * @param seconds 视频时长
     * @param videoOutputPath 合并后视频输出目录
     */
    public void mergeVideoAudio(String videoInputPath,
                      String bgmInputPath,
                      double seconds,
                      String videoOutputPath) {
        // 1. 去除源视频的音轨，生成临时视频文件
        String arrayPathItem[] =  videoInputPath.split("\\.");
        String pathPrefix = "";
        for (int i = 0 ; i < arrayPathItem.length-1 ; i ++) {
            pathPrefix += arrayPathItem[i];
        }
        String tmpFilePath = pathPrefix + "-tmp" + ".mp4";
        removeSound(videoInputPath, tmpFilePath);

        // 2. 合并临时视频和音频文件
        merge(tmpFilePath, bgmInputPath, seconds, videoOutputPath);

        // 3. 删除初始视频文件，删除临时文件
        deleteFile(tmpFilePath);
        deleteFile(videoInputPath);
    }

    /**
     * 合并本身没有音轨的视频和音频文件
     */
    private void merge(String videoInputPath,
                       String auidoInputPath,
                       double seconds,
                       String videoOutputPath) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegEXE);

        command.add("-i");
        command.add(videoInputPath);

        command.add("-i");
        command.add(auidoInputPath);

        command.add("-t");
        command.add(String.valueOf(seconds));

        command.add("-y");
        command.add(videoOutputPath);

        excecuteCommand(command);
    }

    public void getCover(String videoInputPath, String coverOutputPath) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegEXE);

        // 指定截取第1秒
        command.add("-ss");
        command.add("00:00:01");

        command.add("-i");
        command.add(videoInputPath);

        // 截取 1 秒中的 1 帧
        command.add("-vframes");
        command.add("1");

        command.add("-y");
        command.add(coverOutputPath);

        excecuteCommand(command);
    }

    public void removeSound(String videoInputPath, String videoOutputPath) {
        List<String> command = new ArrayList<>();
        command.add(ffmpegEXE);

        command.add("-i");
        command.add(videoInputPath);

        command.add("-vcodec");
        command.add("copy");

        command.add("-an");
        command.add(videoOutputPath);

        excecuteCommand(command);
    }

    private void deleteFile(String filePath) {
        File file = new File(filePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
        }
    }

    /**
     * 执行 CMD 命令
     * @param command
     */
    private void excecuteCommand(List<String> command) {
        // 执行 cmd 命令
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = null;
        try {
            process = builder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 执行过程中会产生错误流，需要释放资源
        BufferedReader br = null;
        try {
            InputStream errorStream = process.getErrorStream();
            br = new BufferedReader(new InputStreamReader(errorStream));
            String line = "";
            while ( (line = br.readLine()) != null) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
