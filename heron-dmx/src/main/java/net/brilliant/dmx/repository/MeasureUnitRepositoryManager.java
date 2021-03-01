/**
 * 
 */
package net.brilliant.dmx.repository;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.css.service.general.MeasureUnitService;
import net.brilliant.dmx.helper.DmxCollaborator;
import net.brilliant.dmx.helper.DmxConfigurationHelper;
import net.brilliant.dmx.repository.base.DmxRepositoryBase;
import net.brilliant.entity.config.ConfigurationDetail;
import net.brilliant.entity.general.MeasureUnit;
import net.brilliant.framework.entity.Entity;
import net.brilliant.model.Context;
import net.brilliant.osx.model.ConfigureMarshallObjects;
import net.brilliant.osx.model.DataWorkbook;
import net.brilliant.osx.model.DataWorksheet;
import net.brilliant.osx.model.MarshallingObjects;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OsxBucketContainer;

/**
 * @author ducbui
 *
 */
@Component
public class MeasureUnitRepositoryManager extends DmxRepositoryBase {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5094772767804079070L;

	@Inject 
	private DmxCollaborator dmxCollaborator;
	
	@Inject 
	private MeasureUnitService measureUnitService;
	
	@Inject 
	private DmxConfigurationHelper dmxConfigurationHelper;

	private Map<String, Byte> configDetailIndexMap = CollectionsUtility.createMap();

	@Override
	protected Context doUnmarshallBusinessObjects(Context executionContext) throws CerberusException {
		DataWorkbook dataWorkbook = null;
		OsxBucketContainer osxBucketContainer = (OsxBucketContainer)executionContext.get(OSXConstants.MARSHALLED_CONTAINER);
		if (CommonUtility.isEmpty(osxBucketContainer))
			throw new CerberusException("There is no measure unit data in OSX container!");

		String workingDatabookId = dmxCollaborator.getConfiguredDataCatalogueWorkbookId();
		if (osxBucketContainer.containsKey(workingDatabookId)){
			dataWorkbook = (DataWorkbook)osxBucketContainer.get(workingDatabookId);
		}

		List<Entity> marshalledObjects = unmarshallBusinessObjects(dataWorkbook, CollectionsUtility.createDataList(MarshallingObjects.MEASURE_UNITS.getName()));
		if (CommonUtility.isNotEmpty(marshalledObjects)) {
			for (Entity entityBase :marshalledObjects) {
				measureUnitService.saveOrUpdate((MeasureUnit)entityBase);
			}
		}
		return executionContext;
	}

	@Override
	protected List<Entity> doUnmarshallBusinessObjects(DataWorkbook dataWorkbook, List<String> datasheetIds) throws CerberusException {
		Map<String, ConfigurationDetail> configDetailMap = null;
		if (CommonUtility.isEmpty(configDetailIndexMap)) {
			configDetailMap = dmxConfigurationHelper.fetchInventoryItemConfig(ConfigureMarshallObjects.MEASURE_UNITS.getConfigName());
			for (String key :configDetailMap.keySet()) {
				configDetailIndexMap.put(key, Byte.valueOf(configDetailMap.get(key).getValue()));
			}
		}

		List<Entity> marshallingObjects = CollectionsUtility.createDataList();
		MeasureUnit marshallingObject = null;
		DataWorksheet dataWorksheet = dataWorkbook.getDatasheet(ConfigureMarshallObjects.MEASURE_UNITS.getName());
		if (CommonUtility.isNotEmpty(dataWorksheet)) {
			for (Integer key :dataWorksheet.getKeys()) {
				try {
					marshallingObject = (MeasureUnit)unmarshallBusinessObject(dataWorksheet.getDataRow(key));
				} catch (CerberusException e) {
					logger.error(e);
				}
				if (null != marshallingObject) {
					marshallingObjects.add(marshallingObject);
				}
			}
		}
		return marshallingObjects;
	}

	@Override
	protected Entity doUnmarshallBusinessObject(List<?> marshallingDataRow) throws CerberusException {
		MeasureUnit marshalledObject = null;
		try {
			if (1 > measureUnitService.count("code", marshallingDataRow.get(this.configDetailIndexMap.get("idxCode")))) {
				marshalledObject = MeasureUnit.builder()
						.code((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxCode")))
						.name((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxName")))
						.nameLocal((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxNameLocal")))
						.info((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxInfo")))
						.build();
			}
		} catch (Exception e) {
			logger.error(e);
		}

		return marshalledObject;
	}

}