package com.moormic.f1.game.repository.prediction.excel;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moormic.f1.game.model.prediction.PlayerPrediction;
import com.moormic.f1.game.repository.game.ExcelGameRepository;
import com.moormic.f1.game.repository.prediction.PlayerPredictionRepository;
import com.moormic.f1.game.util.ExcelWorkbookUtil;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@Repository
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExcelPlayerPredictionRepository implements PlayerPredictionRepository {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final List<String> EXCLUDED_SHEETS = List.of("Scores", "Rules", "Metadata");
    private final ExcelGameRepository gameRepository;

    public List<PlayerPrediction> get(Integer round) {
        var workbook = gameRepository.get();
        var sheets = workbook.sheetIterator();
        var predictions = new ArrayList<PlayerPrediction>();

        while (sheets.hasNext()) {
            var sheet = sheets.next();
            var sheetName = sheet.getSheetName();

            if (!StringUtils.isBlank(sheetName) && !EXCLUDED_SHEETS.contains(sheetName)) {
                var rows = sheet.rowIterator();
                var firstRow = Optional.ofNullable(rows)
                        .filter(Iterator::hasNext)
                        .map(Iterator::next)
                        .orElseThrow(() -> new RuntimeException("Unable to find prediction header row for sheet " + sheetName));

                var columnMapping = ExcelWorkbookUtil.columnMapping(firstRow);
                var row = ExcelWorkbookUtil.getRowForRound(columnMapping, rows, round);
                var prediction = rowToExcelPrediction(row, columnMapping);
                predictions.add(playerPrediction(sheetName, prediction));
            }
        }

        gameRepository.close(workbook);
        return predictions;
    }

    private ExcelPlayerPrediction rowToExcelPrediction(Row row, Map<Integer, String> columnMapping) {
        var cells = row.cellIterator();
        var predictionMap = new HashMap<String, Object>();

        while (cells.hasNext()) {
             var cell = cells.next();
             var cellColumnIndex = cell.getColumnIndex();
             var cellValue = Optional.ofNullable(ExcelWorkbookUtil.cellValue(cell));
             var cellColumnName = Optional.ofNullable(columnMapping.get(cellColumnIndex));

             if (cellValue.isPresent() && cellColumnName.isPresent()) {
                 predictionMap.put(cellColumnName.get(), cellValue.get());
             }
        }

        return OBJECT_MAPPER.convertValue(predictionMap, new TypeReference<>(){});
    }

    private PlayerPrediction playerPrediction(String playerName, ExcelPlayerPrediction prediction) {
        return new PlayerPrediction(
                playerName,
                prediction.getRound(),
                prediction.getPole(),
                Stream.of(prediction.getP1Driver(), prediction.getP2Driver(), prediction.getP3Driver())
                        .filter(Objects::nonNull)
                        .collect(toList()),
                prediction.getFastestLapDriver(),
                prediction.getNumDnfDrivers(),
                Stream.of(prediction.getDnf1Driver(), prediction.getDnf2Driver(), prediction.getDnf3Driver())
                        .filter(Objects::nonNull)
                        .collect(toList())
        );
    }

}