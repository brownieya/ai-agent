package com.brownie.brownieaiagent.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class TerminalOperationTool {

    @Tool(description = "Execute a command in the terminal")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        StringBuilder output = new StringBuilder();
        try {
            // dir、cd、type 等是 shell 内置命令，不是独立的可执行文件，
            // 必须通过 cmd/sh 去解释执行，不能直接 exec(command)
            boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
            ProcessBuilder processBuilder = isWindows
                    ? new ProcessBuilder("cmd.exe", "/c", command)
                    : new ProcessBuilder("sh", "-c", command);
            // 把标准错误合并进标准输出，避免报错信息丢失或子进程因 stderr 缓冲区写满而卡死
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            // Windows 下 cmd 默认使用系统本地代码页（中文系统通常是 GBK），不是 UTF-8
            String charsetName = isWindows ? System.getProperty("sun.jnu.encoding", "GBK") : "UTF-8";
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), Charset.forName(charsetName)))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                output.append("Command execution failed with exit code: ").append(exitCode);
            }
        } catch (IOException | InterruptedException e) {
            output.append("Error executing command: ").append(e.getMessage());
        }
        return output.toString();
    }
}