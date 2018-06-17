package com.github.akuma1.aws.lambda.pdf.util;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class PdfMergeUtil {

    public static InputStream mergePdfStreams(List<InputStream> pdfStreams) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, out);
            document.open();
            PdfReader reader;
            for (InputStream pdfStream : pdfStreams) {
                reader = new PdfReader(pdfStream);
                copy.addDocument(reader);
                copy.freeReader(reader);
                reader.close();
            }
            document.close();
            out.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (DocumentException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println(e.getMessage());
            return null;
        }
    }

}