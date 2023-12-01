package io.github.sinri.keel.poi.excel.entity;

import io.github.sinri.keel.core.TechnicalPreview;

import javax.annotation.Nonnull;
import java.util.List;

@TechnicalPreview(since = "3.0.13")
public interface KeelSheetTemplatedMatrix {
    static KeelSheetTemplatedMatrix create(@Nonnull KeelSheetMatrixRowTemplate template) {
        return new KeelSheetTemplatedMatrixImpl(template);
    }

    KeelSheetMatrixRowTemplate getTemplate();

    KeelSheetMatrixTemplatedRow getRow(int index);

    List<KeelSheetMatrixTemplatedRow> getRows();

    List<List<String>> getRawRows();

    KeelSheetTemplatedMatrix addRawRow(List<String> rawRow);

    default KeelSheetTemplatedMatrix addRawRows(List<List<String>> rawRows) {
        rawRows.forEach(this::addRawRow);
        return this;
    }

    default KeelSheetMatrix transformToMatrix() {
        KeelSheetMatrix keelSheetMatrix = new KeelSheetMatrix();
        keelSheetMatrix.setHeaderRow(getTemplate().getColumnNames());
        keelSheetMatrix.addRows(getRawRows());
        return keelSheetMatrix;
    }

}