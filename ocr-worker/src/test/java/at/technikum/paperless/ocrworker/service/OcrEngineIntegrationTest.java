package at.technikum.paperless.ocrworker.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class OcrEngineIntegrationTest {

    private static boolean toolPresent(String cmd, String arg) {
        try {
            Process p = new ProcessBuilder(cmd, arg).redirectErrorStream(true).start();
            p.waitFor();
            return p.exitValue() == 0;
        } catch (Exception e) { return false; }
    }

    static {
        // skip whole class if tools missing
        org.junit.jupiter.api.Assumptions.assumeTrue(toolPresent("tesseract", "--version"),
                "tesseract not found on PATH");
        org.junit.jupiter.api.Assumptions.assumeTrue(toolPresent("pdftoppm", "-v"),
                "pdftoppm (Poppler) not found on PATH");
    }

    @Test
    void ocr_png_basic() throws Exception {
        // create a simple PNG with text "HELLO 123"
        BufferedImage img = new BufferedImage(600, 200, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE); g.fillRect(0,0,600,200);
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 72));
        g.drawString("HELLO 123", 40, 120);
        g.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", bos);
        byte[] png = bos.toByteArray();

        OcrEngine engine = new OcrEngine("eng", "6", 30000);
        String text = engine.ocr(png, "image/png", "hello.png");

        assertThat(text.toUpperCase()).contains("HELLO");
        assertThat(text).contains("123");
    }

    @Test
    void ocr_pdf_basic() throws Exception {
        // make a 1-page PDF with visible text using PDFBox
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(); doc.addPage(page);
            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                cs.beginText();
                cs.setFont(PDType1Font.HELVETICA_BOLD, 24);
                cs.newLineAtOffset(100, 700);
                cs.showText("SPRINT 4 OCR TEST");
                cs.endText();
            }
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            doc.save(bout);
            byte[] pdf = bout.toByteArray();

            OcrEngine engine = new OcrEngine("eng", "6", 30000);
            String text = engine.ocr(pdf, "application/pdf", "test.pdf");
            assertThat(text.toUpperCase()).contains("SPRINT 4 OCR TEST");
        }
    }
}