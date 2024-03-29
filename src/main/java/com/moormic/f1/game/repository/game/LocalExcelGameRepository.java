package com.moormic.f1.game.repository.game;

import com.moormic.f1.game.config.GameConfig;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class LocalExcelGameRepository implements ExcelGameRepository {
    private final GameConfig config;

    public Workbook get() {
        try {
            FileInputStream file = new FileInputStream(config.getUrl());
            return new XSSFWorkbook(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to load excel game file. Error: " + e.getMessage());
        }
    }

    public void save(Workbook workbook) {
        try {
            HSSFFormulaEvaluator.evaluateAllFormulaCells(workbook); // update any formulae based on updated values
            FileOutputStream file = new FileOutputStream(config.getUrl());
            workbook.write(file);
        } catch (IOException e) {
            throw new RuntimeException("Unable to save excel game file. Error: " + e.getMessage());
        }
    }

    public void close(Workbook workbook) {
        try {
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("Unable to close workbook " + e.getMessage());
        }
    }

}
