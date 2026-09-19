package com.brownie.brownieaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "demo.txt";
        String readFile = tool.readFile(fileName);
        Assertions.assertNotNull(readFile);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "demo.txt";
        String content = "hello world";
        String writeFile = tool.writeFile(fileName, content);
        Assertions.assertNotNull(writeFile);
    }
}