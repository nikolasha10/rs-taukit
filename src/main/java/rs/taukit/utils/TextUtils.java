package rs.taukit.utils;

import io.cucumber.datatable.DataTable;
import io.cucumber.datatable.DataTableTypeRegistry;
import io.cucumber.datatable.DataTableTypeRegistryTableConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.IntStream;

public class TextUtils {
    public static List<List<String>> correctDatatable(DataTable dataTable) {
        List<List<String>> data = dataTable.asLists();
        List<List<String>> corrected = new ArrayList<>();
        IntStream.range(0, data.size()).boxed().forEach(rowIndex -> {
            List<String> row = data.get(rowIndex);
            IntStream.range(0, row.size()).boxed().forEach(cellIndex -> {
                String cell = Optional.ofNullable(row.get(cellIndex)).orElse("");
                String[] cellLines = cell.split("\n");
                for (int i = 0; i < cellLines.length; i++) {
                    if (corrected.size() <= rowIndex + i) {
                        corrected.add(new ArrayList<>());
                    }
                    while (corrected.get(rowIndex + i).size() < row.size()) {
                        corrected.get(rowIndex + i).add("");
                    }
                    corrected.get(rowIndex + i).set(cellIndex, cellLines[i]);
                }
            });
        });
        return corrected;
    }

    private static final DataTableTypeRegistry registry = new DataTableTypeRegistry(Locale.ENGLISH);
    public static final DataTable.TableConverter tableConverter = new DataTableTypeRegistryTableConverter(registry);

}
