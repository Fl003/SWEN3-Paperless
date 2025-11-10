package at.technikum.paperless.ocrworker.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.Comparator;
import java.util.concurrent.TimeUnit;

@Service
public class OcrEngine {
    public OcrEngine(@Value("${app.ocr.lang:deu+eng}") String lang,
                     @Value("${app.ocr.psm:6}") String psm,
                     @Value("${app.ocr.timeoutMs:20000}") int timeoutMs) {
        this.lang = lang; this.psm = psm; this.timeoutMs = timeoutMs;
    }
    private final String lang, psm; private final int timeoutMs;

    public String ocr(byte[] bytes, String contentType, String originalName) throws Exception {
        String mime = contentType != null ? contentType : guess(bytes, originalName);
        if ("application/pdf".equalsIgnoreCase(mime)) return ocrPdf(bytes);
        return ocrImage(bytes);
    }

    private String ocrImage(byte[] data) throws Exception {
        File f = File.createTempFile("ocr-", ".bin"); Files.write(f.toPath(), data);
        try { return runTesseract(f); } finally { Files.deleteIfExists(f.toPath()); }
    }

    private String ocrPdf(byte[] pdf) throws Exception {
        Path dir = Files.createTempDirectory("pdf-ocr-");
        Path in = dir.resolve("in.pdf"); Files.write(in, pdf);
        Process p = new ProcessBuilder("pdftoppm", "-r", "300", in.toString(), "page", "-png")
                .directory(dir.toFile()).redirectErrorStream(true).start();
        if (!p.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) { p.destroyForcibly(); throw new RuntimeException("pdftoppm timeout"); }
        if (p.exitValue()!=0) throw new RuntimeException("pdftoppm failed");

        var pages = Files.list(dir).filter(x->x.getFileName().toString().matches("page-\\d+\\.png"))
                .sorted(Comparator.comparing(Path::toString)).toList();

        StringBuilder out = new StringBuilder();
        for (int i=0;i<pages.size();i++){
            if (i>0) out.append("\n\n---- PAGE ").append(i+1).append(" ----\n\n");
            out.append(runTesseract(pages.get(i).toFile()));
            Files.deleteIfExists(pages.get(i));
        }
        Files.deleteIfExists(in); Files.deleteIfExists(dir);
        return out.toString().trim();
    }

    private String runTesseract(File input) throws Exception {
        Process pr = new ProcessBuilder("tesseract", input.getAbsolutePath(), "stdout",
                "-l", lang, "--psm", psm, "--oem", "1").redirectErrorStream(true).start();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (InputStream is = pr.getInputStream()) { is.transferTo(bos); }
        if (!pr.waitFor(timeoutMs, TimeUnit.MILLISECONDS)) { pr.destroyForcibly(); throw new RuntimeException("tesseract timeout"); }
        if (pr.exitValue()!=0) throw new RuntimeException("tesseract exit " + pr.exitValue());
        return bos.toString().trim();
    }

    private String guess(byte[] b, String name){
        if (b.length>4 && b[0]==0x25 && b[1]==0x50 && b[2]==0x44 && b[3]==0x46) return "application/pdf"; // %PDF
        if (name!=null && name.toLowerCase().endsWith(".pdf")) return "application/pdf";
        return "image/*";
    }
}
