/**
 * 
 */
package net.brilliant.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;

/**
 * @author ducbq
 *
 */
public class CSVUtilityHelper {
  public List<String[]> fetchCsvData(InputStream inputStream, char separator, int skipLines) throws IOException, CsvException{
    CSVParser customCsvParser = new CSVParserBuilder()
        .withSeparator(separator)
        .build(); // custom separator
    CSVReader reader = new CSVReaderBuilder(new InputStreamReader(inputStream))
        .withCSVParser(customCsvParser)   // custom CSV parser
        .withSkipLines(skipLines)           // skip the first line, header info
        .build();

    return reader.readAll();
  }
}
