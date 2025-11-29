package com.investmentcalc;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * SwingWorker that writes a schedule CSV to a temporary file and moves it into place.
 * It reports progress based on the number of lines written and supports cancellation.
 */
public class CsvExportWorker extends SwingWorker<Void, Integer> {
    private final InvestmentResult result;
    private final boolean monthly;
    private final File targetFile;
    private final int totalRows;

    public CsvExportWorker(InvestmentResult result, boolean monthly, File targetFile) {
        this.result = result;
        this.monthly = monthly;
        this.targetFile = targetFile;
        if (monthly) {
            List<MonthlyData> md = result.getMonthlyData();
            this.totalRows = (md == null) ? 1 : (md.size() + 1);
        } else {
            List<YearlyData> yd = result.getYearlyData();
            this.totalRows = (yd == null) ? 1 : (yd.size() + 1);
        }
    }

    @Override
    protected Void doInBackground() throws Exception {
        Path parent = (targetFile.getParentFile() != null) ? targetFile.getParentFile().toPath() : null;
        Path tempFile;
        if (parent != null) {
            tempFile = Files.createTempFile(parent, targetFile.getName(), ".tmp");
        } else {
            tempFile = Files.createTempFile(targetFile.getName(), ".tmp");
        }

        try (Writer w = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8);
             CountingWriter cw = new CountingWriter(w, totalRows, this)) {
            // Delegate actual CSV writing to CsvExporter which writes via the provided Writer
            CsvExporter.writeScheduleCsv(result, monthly, cw);
            if (isCancelled()) throw new IOException("Export cancelled");
        } catch (Exception e) {
            // cleanup temp file on error
            try { Files.deleteIfExists(tempFile); } catch (Exception ex) { /* ignore */ }
            throw e;
        }

        try {
            Files.move(tempFile, targetFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            try { Files.deleteIfExists(tempFile); } catch (Exception ex) { /* ignore */ }
            throw e;
        }

        setProgress(100);
        return null;
    }

    // CountingWriter counts newline characters written and updates worker progress.
    private static class CountingWriter extends Writer {
        private final Writer out;
        private final int totalRows;
        private final CsvExportWorker worker;
        private int linesWritten = 0;

        CountingWriter(Writer out, int totalRows, CsvExportWorker worker) {
            this.out = out;
            this.totalRows = Math.max(1, totalRows);
            this.worker = worker;
        }

        @Override
        public void write(char[] cbuf, int off, int len) throws IOException {
            if (worker.isCancelled()) throw new IOException("Cancelled");
            out.write(cbuf, off, len);
            for (int i = off; i < off + len; i++) {
                if (cbuf[i] == '\n') {
                    linesWritten++;
                    int prog = Math.min(100, (int) ((linesWritten * 100L) / totalRows));
                    worker.setProgress(prog);
                }
            }
        }

        @Override
        public void flush() throws IOException { out.flush(); }

        @Override
        public void close() throws IOException { out.close(); }

        @Override
        public void write(int c) throws IOException {
            if (worker.isCancelled()) throw new IOException("Cancelled");
            out.write(c);
            if (c == '\n') {
                linesWritten++;
                int prog = Math.min(100, (int) ((linesWritten * 100L) / totalRows));
                worker.setProgress(prog);
            }
        }

        @Override
        public void write(String str, int off, int len) throws IOException {
            if (worker.isCancelled()) throw new IOException("Cancelled");
            out.write(str, off, len);
            for (int i = off; i < off + len; i++) {
                if (str.charAt(i) == '\n') {
                    linesWritten++;
                    int prog = Math.min(100, (int) ((linesWritten * 100L) / totalRows));
                    worker.setProgress(prog);
                }
            }
        }
    }
}
