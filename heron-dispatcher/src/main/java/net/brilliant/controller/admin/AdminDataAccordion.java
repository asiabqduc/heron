package net.brilliant.controller.admin;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.model.Context;
import net.brilliant.osx.helper.OfficeSuiteServiceProvider;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OSXWorkbook;
import net.brilliant.osx.model.OSXWorksheet;

/**
 * @author ducbq
 */
@Slf4j
@Named
@ViewScoped
public class AdminDataAccordion implements Serializable {
  private static final long serialVersionUID = 1474083820747048334L;

  @Inject
  private ResourceLoader resourceLoader;

  private List<String> processingSheetIds = CollectionsUtility.createList();

  @PostConstruct
  public void initDataModel() {
    processingSheetIds.add("inventory-items");
    processingSheetIds.add("business-units");
  }

  public void onLoadMasterData() {
    log.info("On loading master data.");
    try {
      loadDataFromExcel();
    } catch (CerberusException e) {
      log.error(e.getMessage(), e);
    }
  }

  protected void loadDataFromExcel() throws CerberusException{
    Context context = Context.builder().build();
    InputStream inputStream = null;
    try {
      Resource resource = this.resourceLoader.getResource("classpath:/master/data-catalog.xlsx");
      if (null==resource){
        log.error("Unable to get resource from path: {}", "/master/data-catalog.xlsx");
        return;
      }

      inputStream = resource.getInputStream();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    if (null==inputStream){
      log.error("The input stream was empty, please check the data in the configured path!");
      return;
    }

    context.put(OSXConstants.INPUT_STREAM, inputStream);
    context.put(OSXConstants.PROCESSING_DATASHEET_IDS, processingSheetIds);
    context.put(OSXConstants.STARTED_ROW_INDEX, new Integer[] {1, 1, 1});
    OSXWorkbook workbook = OfficeSuiteServiceProvider.builder().build().readExcelFile(context);
    processLoadedData(workbook);
  }

  protected void processLoadedData(OSXWorkbook osxWorkbook){
    log.info("+++++++++++++++++");
    for (OSXWorksheet worksheet :osxWorkbook.datasheets()){
      log.info("Number of rows: " + worksheet.getDataRow(worksheet.getKeys().size()));
    }
    log.info("+++++++++++++++++");
  }
}
