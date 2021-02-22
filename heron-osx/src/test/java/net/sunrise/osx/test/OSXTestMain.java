/**
 * 
 */
package net.sunrise.osx.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

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
 * @author bqduc
 *
 */
public class OSXTestMain {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	  loadZipData();
	  //doTestReadXlsx();
	}

	protected static void doTestReadXlsx() {
		Map<Object, Object> params = CollectionsUtility.createMap();
		String[] sheetIds = new String[]{/*"languages", "items", "localized-items"*/"inventory-items", "business-units"}; 
		OsxBucketContainer dataBucket = null;
		String dataSheetSource = "D:\\workspace\\aquariums.git\\aquarium\\aquarium-admin\\src\\main\\resources\\config\\data\\data-catalog.xlsx";
		List<String> sheetIdList = CollectionsUtility.createList("inventory-items", "business-units");
		dataSheetSource = "D:/git/heron/heron/src/main/resources/master-data/data-catalog.xlsx";
		try {
			params.put(OSXConstants.INPUT_STREAM, new FileInputStream(dataSheetSource));
			params.put(OSXConstants.PROCESSING_DATASHEET_IDS, sheetIdList);
			params.put(OSXConstants.STARTED_ROW_INDEX, new Integer[] {1, 1, 1});
			OSXWorkbook workbookContainer = OfficeSuiteServiceProvider.builder()
			.build()
			.readExcelFile(params);
			
			OSXWorksheet osxWorksheet = workbookContainer.getDatasheet(sheetIds[0]);
			displayDatasheet(osxWorksheet);
			/*List<?> details = null;
			List<?> forthcomingBooks = (List<?>)workbookContainer.get("Forthcoming");
			List<?> onlineBooks = (List<?>)dataBucket.getBucketData().get("online-books");
			for (Object currentItem :forthcomingBooks) {
				System.out.println(currentItem);
			}

			for (Object currentItem :onlineBooks) {
				details = (List<?>)currentItem;
				System.out.println(details.size());
			}*/
			//System.out.println(dataBucket.getBucketData().get("Forthcoming"));
			//System.out.println(dataBucket.getBucketData().get("online-books"));
		} catch (Exception e) {
			e.printStackTrace();;
		}

	}

  protected static Context initContextData(
      final Map<String, String> secretKeys, 
      final Map<String, List<String>> sheetIdList, 
      final String[] zipEntries,
      final String originalFileName,
      final InputStream compressedZipInputStream) {

    return Context.builder().build()
        .put(OSXConstants.COMPRESSED_FILE, CommonUtility.createFileFromInputStream(originalFileName, compressedZipInputStream))
        .put(OSXConstants.ENCRYPTION_KEYS, secretKeys)
        .put(OSXConstants.ZIP_ENTRY, CollectionsUtility.arraysAsList(zipEntries))
        .put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING)
        .put(OSXConstants.PROCESSING_DATASHEET_IDS, sheetIdList);
  }
	
	
	protected static void loadZipData(){
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
	}

	private static void displayWorkbook(OSXWorkbook workbook){
	  for (OSXWorksheet worksheet :workbook.datasheets()){
	    System.out.println("+++++++++++++++++" + worksheet.getId() + "+++++++++++++++++");
	    for (Integer key :worksheet.getKeys()){
	      System.out.println(worksheet.getDataRow(key));
	    }
	  }
    System.out.println("+++++++++++++++++");
  }

	private static void displayDatasheet(OSXWorksheet osxWorksheet){
	  System.out.println("+++++++++++++++++");
	  for (Integer key :osxWorksheet.getKeys()){
	    System.out.println(osxWorksheet.getDataRow(key));
	  }
    System.out.println("+++++++++++++++++");
	}
}
