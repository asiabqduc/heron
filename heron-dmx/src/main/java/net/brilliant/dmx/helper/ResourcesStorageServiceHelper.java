/**
 * 
 */
package net.brilliant.dmx.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Component;

import net.brilliant.ccs.exceptions.EcosphereResourceException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonConstants;
import net.brilliant.common.CommonUtility;
import net.brilliant.common.MimeTypes;
import net.brilliant.common.SimpleEncryptionEngine;
import net.brilliant.comp.helper.ResourcesServicesHelper;
import net.brilliant.css.service.config.ConfigurationService;
import net.brilliant.css.service.general.AttachmentService;
import net.brilliant.domain.entity.Attachment;
import net.brilliant.entity.config.Configuration;
import net.brilliant.entity.config.ConfigurationDetail;
import net.brilliant.exceptions.CommonException;
import net.brilliant.model.Context;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OfficeMarshalType;

/**
 * @author ducbq
 *
 */
@Component
public class ResourcesStorageServiceHelper {
	@Inject
	private ResourcesServicesHelper resourcesServicesHelper;
	
	@Inject
	private AttachmentService attachmentService;
	
	@Inject
	private ConfigurationService configurationService;

	public Context syncExecutionContext(Configuration config, byte[] dataBytes) throws EcosphereResourceException {
		if (null == config) {
			return null;
		}

		Context executionContext = Context.builder().build();

		/*String masterFileName = config.getValue();
		Map<String, String> secretKeyMap = CollectionsUtility.createMap();
		for (ConfigurationDetail configDetail :config.getConfigurationDetails()) {
			if (OSXConstants.ENCRYPTION_KEYS.equalsIgnoreCase(configDetail.getValueExtended())) {
				secretKeyMap.put(configDetail.getName(), SimpleEncryptionEngine.decode(configDetail.getValue()));
			}
		}*/

		executionContext.put(OSXConstants.MASTER_BUFFER_DATA_BYTES, dataBytes);
		//executionContext.put(OSXConstants.PARAM_MASTER_FILE_NAME, masterFileName);
		//executionContext.put(OSXConstants.ENCRYPTION_KEYS, secretKeyMap);
		executionContext.put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING);
		return executionContext;
	}

	public Context buildDefaultDataExecutionContext() throws EcosphereResourceException {
		Context executionContext = Context.builder().build();

		String defaultContactsData = "Vietbank_14.000.xlsx", defaultCataloguesData = "data-catalog.xlsx";
		//File zipFile = resourcesServicesHelper.loadClasspathResourceFile("data/marshall/develop_data.zip");
		Map<String, String> secretKeyMap = CollectionsUtility.createHashMapData(defaultContactsData, "thanhcong");
		Map<String, List<String>> sheetIdMap = CollectionsUtility.createMap();
		sheetIdMap.put(defaultContactsData, CollectionsUtility.arraysAsList(new String[] {"File Tổng hợp", "Các trưởng phó phòng", "9"}));

		executionContext.put(OSXConstants.MASTER_BUFFER_DATA_BYTES, resourcesServicesHelper.loadClasspathResourceBytes("data/marshall/develop_data.zip"));
		executionContext.put(OSXConstants.MASTER_ARCHIVED_FILE_NAME, "data/marshall/develop_data.zip");
		executionContext.put(OSXConstants.ENCRYPTION_KEYS, secretKeyMap);
		executionContext.put(OSXConstants.ZIP_ENTRY, CollectionsUtility.arraysAsList(new String[] {defaultContactsData, defaultCataloguesData}));
		executionContext.put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING);
		executionContext.put(OSXConstants.PROCESSING_DATASHEET_IDS, sheetIdMap);
		return executionContext;
	}

	public void archiveResourceData(final Context executionContextParams) throws CommonException {
		Attachment attachment = null;
		Optional<Attachment> attachmentChecker = null;
		Configuration archivedConfig = null;
		Map<String, String> secretKeyMap = null;
		byte[] masterDataBuffer = null;
		String masterDataFileName = null;
		Optional<Configuration> archivedConfigChecker = null;
		try {
			if (!(executionContextParams.containsKey(OSXConstants.MASTER_BUFFER_DATA_BYTES) || executionContextParams.containsKey(OSXConstants.MASTER_ARCHIVED_FILE_NAME)))
				throw new CommonException("There is no archiving file!");

			masterDataFileName = (String)executionContextParams.get(OSXConstants.MASTER_ARCHIVED_FILE_NAME);
			attachmentChecker = this.attachmentService.getByName(masterDataFileName);
			if (attachmentChecker.isPresent())
				return; //throw new EcosphereException("The archiving file is persisted already!");

			masterDataBuffer = (byte[]) executionContextParams.get(OSXConstants.MASTER_BUFFER_DATA_BYTES);
			attachment = this.buidAttachment(masterDataFileName, masterDataBuffer, (String)executionContextParams.get(OSXConstants.MASTER_FILE_ENCRYPTION_KEY));
			this.attachmentService.save(attachment);
			//Build configuration & dependencies accordingly
			archivedConfigChecker = this.configurationService.getByName(masterDataFileName);
			if (archivedConfigChecker.isPresent())
				return;

			archivedConfig = Configuration.builder()
					.group("ArchivedMasterData")
					.name(masterDataFileName)
					.value(masterDataFileName)
					.build();

			secretKeyMap = (Map)executionContextParams.get(OSXConstants.ENCRYPTION_KEYS);
			for (String key :secretKeyMap.keySet()) {
				archivedConfig.addConfigurationDetail(ConfigurationDetail.builder()
						.name(OSXConstants.ENCRYPTION_KEYS)
						.value(SimpleEncryptionEngine.encode(secretKeyMap.get(key)))
						.valueExtended(key)
						.build())
				;
			}
			this.configurationService.save(archivedConfig);
		} catch (Exception e) {
			throw new CommonException(e);
		}
	}

	public static Attachment buidAttachment(final File file) throws CommonException {
		Attachment attachment = null;
		int lastDot = file.getName().lastIndexOf(CommonConstants.FILE_EXTENSION_SEPARATOR);
		String fileExtension = file.getName().substring(lastDot+1);
		try {
			attachment = Attachment.builder()
					.name(file.getName())
					.data(IOUtils.toByteArray(new FileInputStream(file)))
					.mimetype(MimeTypes.getMimeType(fileExtension))
					.build();
		} catch (IOException e) {
			throw new CommonException(e);
		}
		return attachment;
	}

	public Attachment buidAttachment(final String fileName, final InputStream inputStream, String encryptionKey) throws CommonException {
		Attachment attachment = null;
		int lastDot = fileName.lastIndexOf(CommonConstants.FILE_EXTENSION_SEPARATOR);
		String fileExtension = fileName.substring(lastDot+1);
		String procEncyptionKey = null;
		try {
			if (CommonUtility.isNotEmpty(encryptionKey))
				procEncyptionKey = SimpleEncryptionEngine.encode(encryptionKey);

			attachment = Attachment.builder()
					.name(fileName)
					.data(IOUtils.toByteArray(inputStream))
					.mimetype(MimeTypes.getMimeType(fileExtension))
					.encryptionKey(procEncyptionKey)
					.build();
		} catch (IOException e) {
			throw new CommonException(e);
		}
		return attachment;
	}

	public Attachment buidAttachment(final String fileName, final byte[] bytes, String encryptionKey) throws CommonException {
		Attachment attachment = null;
		int lastDot = fileName.lastIndexOf(CommonConstants.FILE_EXTENSION_SEPARATOR);
		String fileExtension = fileName.substring(lastDot+1);
		String procEncyptionKey = null;
		try {
			if (CommonUtility.isNotEmpty(encryptionKey))
				procEncyptionKey = SimpleEncryptionEngine.encode(encryptionKey);

			attachment = Attachment.builder()
					.name(fileName)
					.data(bytes)
					.mimetype(MimeTypes.getMimeType(fileExtension))
					.encryptionKey(procEncyptionKey)
					.build();
		} catch (Exception e) {
			throw new CommonException(e);
		}
		return attachment;
	}

	public static InputStream buidInputStreamFromAttachment(final Attachment attachment) throws CommonException {
		InputStream inputStream = null;
		try {
			inputStream = CommonUtility.buildInputStream(attachment.getName(), attachment.getData());
		} catch (Exception e) {
			throw new CommonException(e);
		}
		return inputStream;
	}	
}
