/**
 * 
 */
package net.brilliant.dmx.manager;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.css.service.config.ConfigurationService;
import net.brilliant.css.service.general.AttachmentService;
import net.brilliant.dmx.helper.ResourcesStorageServiceHelper;
import net.brilliant.dmx.repository.BusinessUnitDataManager;
import net.brilliant.dmx.repository.ContactRepositoryManager;
import net.brilliant.dmx.repository.InventoryItemRepositoryManager;
import net.brilliant.domain.entity.Attachment;
import net.brilliant.entity.config.Configuration;
import net.brilliant.entity.general.GeneralCatalogue;
import net.brilliant.framework.component.CompCore;
import net.brilliant.framework.entity.Entity;
import net.brilliant.model.Context;
import net.brilliant.osx.helper.OfficeSuiteServiceProvider;
import net.brilliant.osx.model.ConfigureUnmarshallObjects;
import net.brilliant.osx.model.DataWorkbook;
import net.brilliant.osx.model.MarshallingObjects;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OSXWorkbook;
import net.brilliant.osx.model.OfficeMarshalType;
import net.brilliant.osx.model.OsxBucketContainer;
import sun.rmi.runtime.Log;

/**
 * @author ducbui
 *
 */
@Component
public class GlobalDmxManager extends CompCore {
	private static final long serialVersionUID = -759495846609992244L;

	public static final int NUMBER_OF_CATALOGUE_SUBTYPES_GENERATE = 500;
	public static final int NUMBER_TO_GENERATE = 15000;
	public static final String DEFAULT_COUNTRY = "Việt Nam";

	@Inject
	private InventoryItemRepositoryManager itemDmxRepository;

	@Inject
	private ContactRepositoryManager contactDmxRepository;

	@Inject
	protected AttachmentService attachmentService;

	@Inject
	protected ResourcesStorageServiceHelper resourcesStorageServiceHelper;

	@Inject
	protected ConfigurationService configurationService;
	
	/*@Inject
	protected OfficeSuiteServiceProvider officeSuiteServiceProvider;*/

	@Inject
	protected BusinessUnitDataManager businessUnitDataManager;
	
  @Inject
  private ResourceLoader resourceLoader;

	@SuppressWarnings("unchecked")
	public Context marshallData(Context context) throws CerberusException {
		List<String> databookIdList = null;
		Map<String, List<String>> datasheetIdMap = null;
		String archivedResourceName = null;
		List<String> marshallingObjects = null;
		try {
			if (!context.containsKey(OSXConstants.MARSHALLING_OBJECTS))
				return context;

			databookIdList = (List<String>)context.get(OSXConstants.PROCESSING_DATABOOK_IDS);
			datasheetIdMap = (Map<String, List<String>>)context.get(OSXConstants.MAPPING_DATABOOKS_DATASHEETS);

			if (Boolean.TRUE.equals(context.get(OSXConstants.FROM_ATTACHMENT))) {
				archivedResourceName = (String)context.get(OSXConstants.RESOURCE_NAME);
				this.marshallDataFromArchivedInAttachment(archivedResourceName, databookIdList, datasheetIdMap);
			} else {
				marshallDataFromArchived(context);
			}

			marshallingObjects = (List<String>)context.get(OSXConstants.MARSHALLING_OBJECTS);
			if (null == context.get(OSXConstants.MARSHALLED_CONTAINER))
				return context;

			if (marshallingObjects.contains(ConfigureUnmarshallObjects.BUSINESS_UNITS.getDataSheetId())){
				//Should be a thread
				businessUnitDataManager.unmarshallBusinessObjects(context);
			}
			
			if (marshallingObjects.contains(MarshallingObjects.INVENTORY_ITEMS.getName()) || marshallingObjects.contains(MarshallingObjects.MEASURE_UNITS.getName())){
				//Should be a thread
				itemDmxRepository.unmarshallBusinessObjects(context);
			}

			if (marshallingObjects.contains(MarshallingObjects.CONTACTS.name())){
				//Should be a thread
				contactDmxRepository.unmarshallBusinessObjects(context);
			}
		} catch (Exception e) {
			 throw new CerberusException (e);
		}
		return context;
	}

	/**
	 * Archive resource data to database unit
	 */
	public void archive(final String archivedFileName, final InputStream inputStream, final String encryptionKey) throws CerberusException {
		Attachment attachment = null;
		Optional<Attachment> optAttachment = null;
    logger.info("Enter GlobalDmxManager.archive(String, InputStream, String)");
		try {
			optAttachment = this.attachmentService.getByName(archivedFileName);
			if (!optAttachment.isPresent()) {
				attachment = resourcesStorageServiceHelper.buidAttachment(archivedFileName, inputStream, encryptionKey);
				this.attachmentService.save(attachment);
			}
		} catch (Exception e) {
			throw new CerberusException(e);
		}
    logger.info("Enter GlobalDmxManager.archive(String, InputStream, String)");
	}

  /**
   * Archive resource data to database unit
   */
  public Context archive(final Context context) throws CerberusException {
    String encryptionKey = null;
    logger.info("Enter GlobalDmxManager.archive(Context)");
    if (!context.containsKey(OSXConstants.RESOURCE_REPO)){
      logger.info("There is no input resource ro be archived. ");
      return context;
    }
    InputStream inputStream = this.getResourceInputStream((String)context.get(OSXConstants.RESOURCE_REPO));

    if (!context.containsKey(OSXConstants.RESOURCE_NAME)){
      logger.info("There is no archive name to be processed. ");
      return context;
    }
    String archivedRepoName = (String)context.get(OSXConstants.RESOURCE_NAME);
    logger.info("Archive with resource name: {}", archivedRepoName);

    if (context.containsKey(OSXConstants.ENCRYPTION_KEY)){
      encryptionKey = (String)context.get(OSXConstants.ENCRYPTION_KEY);
    }

    this.archive(archivedRepoName, inputStream, encryptionKey);
    logger.info("Leave GlobalDmxManager.archive(Context)");
    return context;
  }

  public OsxBucketContainer marshallDataFromArchivedInAttachment(String archivedName, List<String> databookIds, Map<String, List<String>> datasheetIds) throws CerberusException {
		Optional<Attachment> optAttachment = this.attachmentService.getByName(archivedName);
		if (!optAttachment.isPresent())
			return null;

		Optional<Configuration> optConfig = null;
		OsxBucketContainer osxBucketContainer = null;
		InputStream inputStream = null;
		Context defaultExecutionContext = null;
		try {
			inputStream = CommonUtility.buildInputStream(archivedName, optAttachment.get().getData());
			if (null==inputStream)
				return null;

			optConfig = configurationService.getByName(archivedName);
			if (optConfig.isPresent()) {
				defaultExecutionContext = resourcesStorageServiceHelper.syncExecutionContext(optConfig.get(), optAttachment.get().getData());
			}

			defaultExecutionContext.put(OSXConstants.PROCESSING_DATABOOK_IDS, databookIds);
			if (CommonUtility.isNotEmpty(datasheetIds)) {
				defaultExecutionContext.put(OSXConstants.PROCESSING_DATASHEET_IDS, datasheetIds);
			}
			osxBucketContainer = OfficeSuiteServiceProvider.builder().build().extractOfficeDataFromZip(defaultExecutionContext);
		} catch (Exception e) {
			 throw new CerberusException(e);
		}
		return osxBucketContainer;
	}

  public OsxBucketContainer marshallArchivedOfficeData(Context context) throws CerberusException {
    OsxBucketContainer osxBucketContainer = null;
    InputStream inputStream = null;
    OSXWorkbook workbook = null;
    String archivedName = null;
    Optional<Attachment> optAttachment = null;
    Context executionContext = null;
    logger.info("Enter marshallArchivedOfficeData");
    try {
      archivedName = (String) context.get(OSXConstants.RESOURCE_NAME);
      optAttachment = this.attachmentService.getByName(archivedName);
      if (!optAttachment.isPresent())
        return null;

      logger.info("Start processing resource: {}", archivedName);
      executionContext = Context.builder().build().putAll(context);
      inputStream = CommonUtility.buildInputStream(archivedName, optAttachment.get().getData());
      if (null == inputStream)
        return null;

      /*
       * optConfig = configurationService.getByName(archivedName); if (optConfig.isPresent()) { defaultExecutionContext
       * = resourcesStorageServiceHelper.syncExecutionContext(optConfig.get(), optAttachment.get().getData()); }
       */
      logger.info("Get input stream");
      executionContext.put(OSXConstants.INPUT_STREAM, inputStream);
      workbook = OfficeSuiteServiceProvider.builder().build().readExcelFile(executionContext);
      if (workbook != null) {
        osxBucketContainer = OsxBucketContainer.builder().build().put(archivedName, workbook);
      }
    } catch (Exception e) {
      throw new CerberusException(e);
    }
    return osxBucketContainer;
  }

  public Context marshallDataFromArchived(Context executionContext) throws CerberusException {
		InputStream inputStream;
		OsxBucketContainer osxBucketContainer = null;
		Context workingExecutionContext = null;
		try {
			inputStream = (InputStream)executionContext.get(OSXConstants.INPUT_STREAM);
			workingExecutionContext = (Context)Context.builder().build().putAll(executionContext);
			workingExecutionContext.put(OSXConstants.MASTER_BUFFER_DATA_BYTES, FileCopyUtils.copyToByteArray(inputStream));
			workingExecutionContext.put(OSXConstants.OFFICE_EXCEL_MARSHALLING_DATA_METHOD, OfficeMarshalType.STREAMING);
			osxBucketContainer = OfficeSuiteServiceProvider.builder().build().extractOfficeDataFromZip(workingExecutionContext);
			executionContext.put(OSXConstants.MARSHALLED_CONTAINER, osxBucketContainer);
		} catch (Exception e) {
			 throw new CerberusException(e);
		}
		return executionContext;
	}

	public List<Entity> marshallContacts(String archivedResourceName, String dataWorkbookId, List<String> datasheetIdList) throws CerberusException {
		List<Entity> contacts = null;
		DataWorkbook dataWorkbook = null;
		OsxBucketContainer osxBucketContainer = null;
		List<String> databookIdList = null;
		Map<String, List<String>> datasheetIdMap = null;
		try {
			databookIdList = CollectionsUtility.createDataList(dataWorkbookId);
			datasheetIdMap = CollectionsUtility.createHashMapData(dataWorkbookId, datasheetIdList);
			osxBucketContainer = this.marshallDataFromArchivedInAttachment(archivedResourceName, databookIdList, datasheetIdMap);
			if (null != osxBucketContainer && osxBucketContainer.containsKey(dataWorkbookId)){
				dataWorkbook = (DataWorkbook)osxBucketContainer.get(dataWorkbookId);
			}

			contacts = contactDmxRepository.unmarshallBusinessObjects(dataWorkbook, datasheetIdList);
		} catch (Exception e) {
			 throw new CerberusException (e);
		}
		return contacts;
	}

	protected List<GeneralCatalogue> marshallItems(){
		List<GeneralCatalogue> marshalledList = CollectionsUtility.createDataList();
		
		return marshalledList;
	}

	public InputStream getResourceInputStream(String path) throws CerberusException {
	  InputStream inputStream = null;
	  try {
	    Resource resource = this.resourceLoader.getResource(path);
	    if (null==resource){
	      throw new CerberusException("Unable to get resource from path: " + path);
	    }

	    inputStream = resource.getInputStream();
    } catch (Exception e) {
      throw new CerberusException(e);
    }
	  return inputStream;
	}
}
