package org.luckpp.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BmwParser {

    private static final Pattern CHANGE_VERSION_PATTERN = Pattern.compile("[a-zA-Z0-9]{7}/[\\d]{3}");
    private static final Pattern PART_NUMBER_PATTERN = Pattern.compile("[a-zA-Z0-9]{7}-[\\d]{2}");

    public static void processDirecory(String directoryPath) throws IOException {
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            for (final File file: directory.listFiles() ) {
                String fileName = file.getName();
                if (fileName.endsWith(".pdf")) {
                    processFile(file.getAbsolutePath());
                }
            }
        }
    }

    private static void processFile(String src) throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(src + ".txt"));
        String result = parsePdf(src);
        String lines[] = result.split("\n");
        for (String line : lines) {
            if (line.matches("^.+1 PC[ \\t]+[\\d]+.[\\d]+[ \\t]+EUR$")) {
                String changeVersion = extract(line, CHANGE_VERSION_PATTERN);
                String partNumber = extract(line, PART_NUMBER_PATTERN);
                String[] tokens = line.split("1[ ]PC");
                String price = tokens[1].trim();
                String[] priceTokens = price.split(" ");
                String priceNumber = priceTokens[0].replace(".", ",");
                String priceCurrency = priceTokens[1];
                String data = changeVersion + "\t" + partNumber + "\t" + "PC" + "\t" + priceNumber + "\t" + priceCurrency;
                pw.println(data);
            }
        }
        pw.close();
    }

    public static String extract(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            return null;
        }
    }

    /**
     * Example from https://itextpdf.com/en/resources/examples/itext-5-legacy/parsing-pdfs
     * @param pdfPath
     * @throws IOException
     * @return
     */
    private static String parsePdf(String pdfPath) throws IOException {
        StringBuilder sb = new StringBuilder();
        PdfReader reader = new PdfReader(pdfPath);
        for (int page = 1; page <= reader.getNumberOfPages(); page++) {
            String pageText = PdfTextExtractor.getTextFromPage(reader, page);
            sb.append(pageText);
            //System.out.println(pageText);
        }
        return new String(sb);
    }

    private static String parsePdfStrategy(String pdfPath) {
        try {
            PdfReader reader = new PdfReader(pdfPath);
            StringBuffer sb = new StringBuffer();
            PdfReaderContentParser parser = new PdfReaderContentParser(reader);
            TextExtractionStrategy strategy;
            for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
                sb.append(strategy.getResultantText());
            }
            reader.close();
            return sb.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException("Not able to read file " + pdfPath, e);
        }
    }
}
