package net.brilliant.controller.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import lombok.extern.slf4j.Slf4j;
import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.model.Context;
import net.brilliant.osx.helper.OfficeSuiteServiceProvider;
import net.brilliant.osx.helper.OfficeSuiteServicesHelper;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OSXWorkbook;
import net.brilliant.osx.model.OSXWorksheet;
import net.brilliant.osx.model.OfficeMarshalType;
import net.brilliant.osx.model.OsxBucketContainer;

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

  public void onLoadMasterDataFromCompressed() {
    log.info("On loading master data.");
    final String masterDataDirectory = "classpath:/master/";
    final String masterDataFile = "data-catalog-high";
    try {
      //loadDataFromCompressedZip(masterDataDirectory + "data-catalog.zip");
      loadDataFromCompressedZip(
          masterDataDirectory + masterDataFile + ".zip", 
          null, 
          new String[] {masterDataFile+".xlsx"},  
          CollectionsUtility.createMap(),
          CollectionsUtility.createHashMapData(masterDataFile, CollectionsUtility.createDataList("contacts", "inventory-items", "business-units")));
    } catch (CerberusException e) {
      log.error(e.getMessage(), e);
    }
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
    OSXWorkbook workbook = null;//OfficeSuiteServiceProvider.builder().build().readExcelFile(context);
    //processLoadedData(workbook);
  }
  /*
    String dataFile = "data-catalog-high.xlsx";
    try {
      long started = System.currentTimeMillis();
      InputStream inputStream = new FileInputStream("D:/git/heron/heron/src/main/resources/master/data-catalog-high.zip");
      OsxBucketContainer bucketContainer = OfficeSuiteServicesHelper.builder().build().loadZipDataFromInputStream(dataFile, inputStream);
      started = System.currentTimeMillis()-started;
      System.out.println("Taken: "+started);
      displayWorkbook((OSXWorkbook)bucketContainer.get(dataFile));
      System.out.println();
    } catch (Exception e) {
      e.printStackTrace();
    }
  */
  protected void loadDataFromCompressedZip(
      String compressedZipFile, 
      String compressedZipFileSecretKey, 
      String[] processingDataFileEntries,  
      Map<String, String> secretKeys,
      Map<String, List<String>> sheetIdList) throws CerberusException{
    Context context = Context.builder().build();
    InputStream compressedZipInputStream = null;
    try {
      Resource resource = this.resourceLoader.getResource(compressedZipFile);
      if (null==resource){
        log.error("Unable to get resource from path: {}", compressedZipFile);
        return;
      }

      compressedZipInputStream = resource.getInputStream();
    } catch (IOException e) {
      log.error(e.getMessage(), e);
    }

    if (null==compressedZipInputStream){
      log.error("The input stream was empty, please check the data in the configured path!");
      return;
    }

    //String originalFileName = compressedZipFile.substring(compressedZipFile.lastIndexOf("/")+1, compressedZipFile.lastIndexOf("."));
    context = Context.builder().build()
        .put(OSXConstants.COMPRESSED_FILE, CommonUtility.createFileFromInputStream(compressedZipFile, compressedZipInputStream))
        .put(OSXConstants.ENCRYPTION_KEYS, secretKeys)
        .put(OSXConstants.ZIP_ENTRY, CollectionsUtility.arraysAsList(processingDataFileEntries))
        .put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING)
        .put(OSXConstants.PROCESSING_DATASHEET_IDS, sheetIdList);

    OsxBucketContainer bucketContainer = OfficeSuiteServiceProvider
    .builder()
    .build()
    .readOfficeDataInCompressedZip(context);
    processLoadedData(bucketContainer);
  }

  protected void processLoadedData(OsxBucketContainer bucketContainer){
    if (null==bucketContainer)
      return;

    for (OSXWorkbook workbook :bucketContainer.getValues()){
      log.info("+++++++++++++++++");
      for (OSXWorksheet worksheet :workbook.datasheets()){
        log.info("Number of rows: " + worksheet.getDataRow(worksheet.getKeys().size()));
      }
    }
    log.info("+++++++++++++++++");
  }
}
