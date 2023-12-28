package io.github.sinri.keel.poi.excel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * @since 3.0.13
 * @since 3.0.18 Finished Technical Preview.
 */
public class KeelSheets implements AutoCloseable {
    @Deprecated(since = "3.0.20", forRemoval = true)
    public KeelSheets(@Nonnull String file) {
        this(new File(file));
    }

    @Deprecated(since = "3.0.20", forRemoval = true)
    public KeelSheets(@Nonnull File file) {
        try {
            autoWorkbook = WorkbookFactory.create(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Deprecated(since = "3.0.20", forRemoval = true)
    public KeelSheets(@Nonnull InputStream inputStream) {
        try {
            autoWorkbook = WorkbookFactory.create(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param workbook The generated POI Workbook Implementation.
     * @since 3.0.20
     */
    public KeelSheets(@Nonnull Workbook workbook) {
        autoWorkbook = workbook;
    }

    protected @Nonnull Workbook autoWorkbook;

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull String file) {
        return factory(new File(file));
    }

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull File file) {
        try {
            return new KeelSheets(WorkbookFactory.create(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.20
     */
    public static KeelSheets factory(@Nonnull InputStream inputStream) {
        try {
            return new KeelSheets(WorkbookFactory.create(inputStream));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @since 3.0.20 The great DAN and HONG discovered an issue with POI Factory Mode.
     */
    public static KeelSheets autoGenerate(@Nonnull InputStream inputStream) {
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(inputStream);
        } catch (IOException e) {
            try {
                workbook = new HSSFWorkbook(inputStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return new KeelSheets(workbook);
    }

    /**
     * Create a new Sheets.
     * Not use stream-write mode by default.
     */
    public KeelSheets() {
        autoWorkbook = new XSSFWorkbook();
    }

    public KeelSheets useStreamWrite() {
        if (autoWorkbook instanceof XSSFWorkbook) {
            autoWorkbook = new SXSSFWorkbook((XSSFWorkbook) autoWorkbook);
        } else {
            throw new IllegalStateException("Now autoWorkbook is not an instance of XSSFWorkbook.");
        }
        return this;
    }

    public KeelSheet generateReaderForSheet(@Nonnull String sheetName) {
        var sheet = this.getWorkbook().getSheet(sheetName);
        return new KeelSheet(sheet);
    }

    public KeelSheet generateReaderForSheet(int sheetIndex) {
        var sheet = this.getWorkbook().getSheetAt(sheetIndex);
        return new KeelSheet(sheet);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName, Integer pos) {
        Sheet sheet = this.getWorkbook().createSheet(sheetName);
        if (pos != null) {
            this.getWorkbook().setSheetOrder(sheetName, pos);
        }
        return new KeelSheet(sheet);
    }

    public KeelSheet generateWriterForSheet(@Nonnull String sheetName) {
        return generateWriterForSheet(sheetName, null);
    }

    public int getSheetCount() {
        return autoWorkbook.getNumberOfSheets();
    }

    /**
     * @return Raw Apache POI Workbook instance.
     */
    @Nonnull
    public Workbook getWorkbook() {
        return autoWorkbook;
    }

    public void save(OutputStream outputStream) {
        try {
            autoWorkbook.write(outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(File file) {
        try {
            save(new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void save(String fileName) {
        save(new File(fileName));
    }

    @Override
    public void close() {
        try {
            autoWorkbook.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
