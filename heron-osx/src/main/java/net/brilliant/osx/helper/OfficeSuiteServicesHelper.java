/**
 * 
 */
package net.brilliant.osx.helper;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import lombok.Builder;
import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.model.Context;
import net.brilliant.osx.model.OSXWorkbook;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OfficeMarshalType;
import net.brilliant.osx.model.OsxBucketContainer;

/**
 * @author ducbui
 *
 */
@Component
@Builder
public class OfficeSuiteServicesHelper implements Serializable {
	/**
   * 
   */
  private static final long serialVersionUID = 4535045388946671757L;

  protected Context initConfigData(final File zipFile) {
		Context executionContext = Context.builder().build();

		Map<String, String> secretKeyMap = CollectionsUtility.createHashMapData("Vietbank_14.000.xlsx", "thanhcong");
		Map<String, List<String>> sheetIdMap = CollectionsUtility.createMap();
		sheetIdMap.put("Vietbank_14.000.xlsx", CollectionsUtility.arraysAsList(new String[] {"File Tổng hợp", "Các trưởng phó phòng", "9"}));

		executionContext.put(OSXConstants.COMPRESSED_FILE, zipFile);
		executionContext.put(OSXConstants.ENCRYPTION_KEYS, secretKeyMap);
		executionContext.put(OSXConstants.ZIP_ENTRY, CollectionsUtility.arraysAsList(new String[] {"Vietbank_14.000.xlsx", "data-catalog.xlsx"}));
		executionContext.put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING);
		executionContext.put(OSXConstants.PROCESSING_DATASHEET_IDS, sheetIdMap);
		return executionContext;
	}

	public OsxBucketContainer loadDefaultZipConfiguredData(final File sourceZipFile) throws CerberusException {
		OsxBucketContainer bucketContainer = null;
		Context executionContext = null;
		try {
			executionContext = this.initConfigData(sourceZipFile);
			bucketContainer = OfficeSuiteServiceProvider
					.builder()
					.build()
					.readOfficeDataInZip(executionContext);
		} catch (Exception e) {
			throw new CerberusException(e);
		}
		return bucketContainer;
	}

	public OsxBucketContainer loadDefaultZipConfiguredData(final Context executionContext) throws CerberusException {
		return OfficeSuiteServiceProvider
					.builder()
					.build()
					.readOfficeDataInZip(executionContext);
	}

	public OsxBucketContainer loadZipDataFromInputStream(final String originFileName, final InputStream inputStream) throws CerberusException {
		OsxBucketContainer bucketContainer = null;
		Context executionContext = null;
		File targetDataFile = null;
		try {
			targetDataFile = CommonUtility.createFileFromInputStream(originFileName, inputStream);
			executionContext = this.initConfigData(targetDataFile);
			bucketContainer = OfficeSuiteServiceProvider
					.builder()
					.build()
					.readOfficeDataInZip(executionContext);
		} catch (Exception e) {
			throw new CerberusException(e);
		}
		return bucketContainer;
	}

	public OSXWorkbook unmarshallContacts(Context executionContext) {
		OSXWorkbook fetchedDataWorkbook = null;
		return fetchedDataWorkbook;
	}
}
