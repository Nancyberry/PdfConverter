package com.nancy.pdf;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import java.io.*;
import java.util.Scanner;

/**
 * Created by nan.zhang on 9/22/15.
 */
public class ExtractPageContent {
    /**
     * The original PDF that will be parsed.
     */
    public static final String PREFACE = "/Users/nan.zhang/Desktop/350.pdf";
    /**
     * The resulting text file.
     */
    public static final String MED = "/Users/nan.zhang/Desktop/med.txt";
    public static final String RESULT = "/Users/nan.zhang/Desktop/result.txt";

    public static final String SKIP_LINE = "English as a Second Language Podcast";
    public static final String DIALOG_START = "[start of ";
    public static final String DIALOG_END = "[end of ";

    /**
     * Parses a PDF to a plain text file.
     *
     * @param pdf the original PDF
     * @param txt the resulting text
     * @throws IOException
     */
    public static void parsePdf(String pdf, String txt) throws IOException {
        PdfReader reader = new PdfReader(pdf);
        PdfReaderContentParser parser = new PdfReaderContentParser(reader);
//        File file = new File(txt);
//        PrintWriter out = new PrintWriter(new FileWriter(file));
        PrintWriter out = new PrintWriter(new FileOutputStream(txt));
        TextExtractionStrategy strategy;
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            strategy = parser.processContent(i, new SimpleTextExtractionStrategy());
            out.print(strategy.getResultantText());
        }
        out.flush();
        out.close();
        reader.close();
    }

    public static String formatText(String txt, String result) throws IOException {
        Scanner scanner = new Scanner(new File(txt));
        PrintWriter out = new PrintWriter(new FileWriter(result));
        StringBuilder sb = new StringBuilder();

        int blankLinesCount = 0;

        // read dialog content first
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.contains(DIALOG_START)) {
                scanner.nextLine();

                while (scanner.hasNextLine()) {
                    String l = scanner.nextLine();
                    if (l.contains(DIALOG_END)) {
                        break;
                    }

                    // skip page start and end
                    if (l.contains(SKIP_LINE)) {
                        skipLines(scanner);
                    } else {
                        if (l.equals(" ")) {
                            if (blankLinesCount < 1) {
                                out.println();
                                out.println();
                                sb.append("\n\n");
                                ++blankLinesCount;
                            }
                        } else {
                            out.print(l);
                            sb.append(l);
                            blankLinesCount = 0;
                        }
                    }
                }

                break;
            }
        }


        scanner = new Scanner(new File(txt));
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();

            if (line.contains("COMPLETE TRANSCRIPT")) {
                break;
            }

            if (line.contains(SKIP_LINE)) {
                skipLines(scanner);
            } else {
                if (line.equals(" ")) {
                    if (blankLinesCount < 1) {
                        out.println();
                        out.println();
                        sb.append("\n\n");
                        ++blankLinesCount;
                    }
                } else {
                    if (line.charAt(0) == '*') {
                        out.println();
                        sb.append("\n");
                    }
                    out.print(line);
                    sb.append(line);
                    blankLinesCount = 0;
                }
            }
        }

        out.flush();
        out.close();
        scanner.close();

        return sb.toString();
    }

    private static void skipLines(Scanner scanner) {
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.matches("^[0-9]{1,3}( *)$")) {
                break;
            }
        }
//        if (scanner.hasNextLine()) {
//            scanner.nextLine();
//        }
    }

    /**
     * Main method.
     *
     * @param args no arguments needed
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        parsePdf(PREFACE, MED);
        formatText(MED, RESULT);
    }
}
