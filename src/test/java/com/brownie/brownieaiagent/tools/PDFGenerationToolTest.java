package com.brownie.brownieaiagent.tools;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
public class PDFGenerationToolTest {

    @Test
    public void testGeneratePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "demo.pdf";
        String content = "demo text";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}