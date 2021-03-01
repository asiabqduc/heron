/**
 * 
 */
package net.brilliant.dmx.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.common.CommonUtility;
import net.brilliant.css.service.general.MeasureUnitService;
import net.brilliant.css.service.org.BusinessUnitService;
import net.brilliant.css.service.stock.InventoryItemService;
import net.brilliant.css.service.stock.InventoryService;
import net.brilliant.dmx.helper.DmxCollaborator;
import net.brilliant.dmx.helper.DmxConfigurationHelper;
import net.brilliant.dmx.repository.base.DmxRepositoryBase;
import net.brilliant.domain.entity.general.Catalogue;
import net.brilliant.entity.config.ConfigurationDetail;
import net.brilliant.entity.general.BusinessUnit;
import net.brilliant.entity.general.GeneralCatalogue;
import net.brilliant.entity.general.MeasureUnit;
import net.brilliant.entity.general.Quantity;
import net.brilliant.entity.stock.InventoryCore;
import net.brilliant.entity.stock.InventoryDetail;
import net.brilliant.entity.stock.InventoryItem;
import net.brilliant.framework.entity.Entity;
import net.brilliant.global.GlobalConstants;
import net.brilliant.model.Context;
import net.brilliant.osx.model.ConfigureMarshallObjects;
import net.brilliant.osx.model.DataWorkbook;
import net.brilliant.osx.model.DataWorksheet;
import net.brilliant.osx.model.OSXConstants;
import net.brilliant.osx.model.OsxBucketContainer;
import net.brilliant.service.general.CatalogueService;

/**
 * @author ducbui
 *
 */
@Component
public class InventoryItemRepositoryManager extends DmxRepositoryBase {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4990550616110685770L;

	@Inject
	private CatalogueService catalogueService;
	
	@Inject 
	private DmxCollaborator dmxCollaborator;

	@Inject
	private InventoryItemService inventoryItemService; 
	
	@Inject
	private BusinessUnitService businessUnitService; 

	@Inject
	private InventoryService productService; 

	@Inject
	private MeasureUnitService measureUnitService;

	@Inject 
	private DmxConfigurationHelper dmxConfigurationHelper;

	private Map<String, Byte> configDetailIndexMap = CollectionsUtility.createMap();
	
	private Map<String, Catalogue> catalogueMap = CollectionsUtility.createMap();

	private Map<String, MeasureUnit> measureUnitMap = CollectionsUtility.createMap();

	@Override
	protected Context doUnmarshallBusinessObjects(Context executionContext) throws CerberusException {
		List<String> marshallingObjects = null;
		DataWorkbook dataWorkbook = null;
		OsxBucketContainer osxBucketContainer = null;
		try {
			marshallingObjects = (List<String>)executionContext.get(OSXConstants.MARSHALLING_OBJECTS);
			if (CommonUtility.isEmpty(executionContext.get(OSXConstants.MARSHALLED_CONTAINER)))
				return executionContext;

			String workingDatabookId = dmxCollaborator.getConfiguredDataCatalogueWorkbookId();
			osxBucketContainer = (OsxBucketContainer)executionContext.get(OSXConstants.MARSHALLED_CONTAINER);
			if (CommonUtility.isEmpty(osxBucketContainer))
				throw new CerberusException("There is no data in OSX container!");

			if (osxBucketContainer.containsKey(workingDatabookId)){
				dataWorkbook = (DataWorkbook)osxBucketContainer.get(workingDatabookId);
			}

			List<Entity> marshalledObjects = unmarshallBusinessObjects(dataWorkbook, CollectionsUtility.createDataList(workingDatabookId));
			if (CommonUtility.isNotEmpty(marshalledObjects)) {
				for (Entity entityBase :marshalledObjects) {
					inventoryItemService.saveOrUpdate((InventoryItem)entityBase);
				}
			}
		} catch (Exception e) {
			throw new CerberusException(e);
		}

		return executionContext;
	}

	@Override
	protected List<Entity> doUnmarshallBusinessObjects(DataWorkbook dataWorkbook, List<String> datasheetIds) throws CerberusException {
		Map<String, ConfigurationDetail> configDetailMap = null;
		if (CommonUtility.isEmpty(configDetailIndexMap)) {
			configDetailMap = dmxConfigurationHelper.fetchInventoryItemConfig(ConfigureMarshallObjects.INVENTORY_ITEMS.getConfigName());
			for (String key :configDetailMap.keySet()) {
				configDetailIndexMap.put(key, Byte.valueOf(configDetailMap.get(key).getValue()));
			}
		}

		List<Entity> results = CollectionsUtility.createDataList();
		InventoryCore currentBizObject = null;
		DataWorksheet dataWorksheet = dataWorkbook.getDatasheet(ConfigureMarshallObjects.INVENTORY_ITEMS.getName());
		if (CommonUtility.isNotEmpty(dataWorksheet)) {
			System.out.println("Processing sheet: " + dataWorksheet.getId());
			for (Integer key :dataWorksheet.getKeys()) {
				try {
					currentBizObject = (InventoryCore)unmarshallBusinessObject(dataWorksheet.getDataRow(key));
				} catch (CerberusException e) {
					e.printStackTrace();
				}

				if (null != currentBizObject) {
					try {
						this.productService.saveOrUpdate(currentBizObject);
					} catch (Exception e) {
						e.printStackTrace();
					}
					results.add(currentBizObject);
				}
			}
		}
		return results;
	}

	@Override
	protected Entity doUnmarshallBusinessObject(List<?> marshallingDataRow) throws CerberusException {
		return unmarshallProduct(marshallingDataRow);
	}

	protected Entity unmarshallProduct(List<?> marshallingDataRow) throws CerberusException {
		GeneralCatalogue usageDirection = null, activeIngredient = null;
		BusinessUnit servicingBusinessUnit = null;
		InventoryCore marshalledObject = null;
		Catalogue bindingCategory = null;
		String stringValueOfCode = null;
		Object dataObject = null;
		MeasureUnit measureUnit = null;
		Quantity balanceQuantity = null;
		long count = 0;
		try {
			if (CommonUtility.isNotEmpty(marshallingDataRow.get(this.configDetailIndexMap.get("idxCode")))) {
				count = this.productService.count("countByCode", CollectionsUtility.createHashMapData("code", marshallingDataRow.get(this.configDetailIndexMap.get("idxCode")).toString()));
			} else if (CommonUtility.isNotEmpty(marshallingDataRow.get(this.configDetailIndexMap.get("idxBarcode")))) {
				count = this.productService.count("countByBarcode", CollectionsUtility.createHashMapData("barcode", marshallingDataRow.get(this.configDetailIndexMap.get("idxBarcode")).toString()));
			}
			if (count > 0)
				return null;

			measureUnit = this.fetchMeasureUnit((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxMeasureUnit")));

			dataObject = marshallingDataRow.get(this.configDetailIndexMap.get("idxUsageDirectionCode"));
			if (null != dataObject) {
				stringValueOfCode = dataObject.toString();
			}
			usageDirection = this.unmarshallItem(stringValueOfCode, (String)marshallingDataRow.get(this.configDetailIndexMap.get("idxUsageDirectionName")), null, null);


			dataObject = marshallingDataRow.get(this.configDetailIndexMap.get("idxActiveIngredientCode"));
			if (null != dataObject) {
				stringValueOfCode = dataObject.toString();
			}
			activeIngredient = this.unmarshallItem(stringValueOfCode, (String)marshallingDataRow.get(this.configDetailIndexMap.get("idxActiveIngredientName")), null, null);

			servicingBusinessUnit = fetchServicingBusinessUnit(marshallingDataRow.get(this.configDetailIndexMap.get("idxBusinessServicingCode")));

			bindingCategory = this.marshallingCatalogue((String)marshallingDataRow.get(this.configDetailIndexMap.get("idxGroupPath")));

			balanceQuantity = buildQuantity(marshallingDataRow.get(this.configDetailIndexMap.get("idxBalanceQuantity")), measureUnit);
			if (null==balanceQuantity||balanceQuantity.isZero()) {
				balanceQuantity = buildQuantity(marshallingDataRow.get(this.configDetailIndexMap.get("idxBalance")), measureUnit);
			}

			marshalledObject = InventoryCore.builder()
					//.category(bindingCategory)
					.barcode(CommonUtility.getString(marshallingDataRow.get(this.configDetailIndexMap.get("idxBarcode")), GlobalConstants.SIZE_BARCODE))
					//.activeIngredient(activeIngredient)
					//.usageDirection(usageDirection)
					//.composition(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxComposition")))) //Hàm lượng
					.name(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxName"))))
					.code(CommonUtility.getString(marshallingDataRow.get(this.configDetailIndexMap.get("idxRegistrationNo")), GlobalConstants.SIZE_CODE))
					//.packaging(CommonUtility.getString(marshallingDataRow.get(this.configDetailIndexMap.get("idxPackaging")), 150))
					.measureUnit(measureUnit)
					//.unitPrice(buildPrice(marshallingDataRow.get(this.configDetailIndexMap.get("idxUnitPrice"))))
					//.balanceQuantity(balanceQuantity)
					//.servicingBusinessUnit(servicingBusinessUnit)
					//.manufacturer(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxManufacturerName"))))
					//.manufacturerCountry(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxManufacturerCountry"))))
					//.contractor(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxContractor"))))
					//.contracorCategory(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxContractorCategory"))))
					//.contractorGroup(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxContractorGroup"))))
					//.governmentDecisionNo(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxDecisionNo"))))
					//.notificationNo(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxDecisionNo"))))
					//.externalCode(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxExternalCode"))))
					//.externalType(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxExternalType"))))
					//.vendor(fetchServicingBusinessUnit(CommonUtility.toString(marshallingDataRow.get(this.configDetailIndexMap.get("idxVendorCode")))))
					//.costPrice(buildPrice(marshallingDataRow.get(this.configDetailIndexMap.get("idxCostPrice"))))
					//.sellingPrice(buildPrice(marshallingDataRow.get(this.configDetailIndexMap.get("idxSellingPrice"))))
					//.progSellingPrice(buildPrice(marshallingDataRow.get(this.configDetailIndexMap.get("idxProgSellingPrice"))))
					//.progSellingQuantity(buildQuantity(marshallingDataRow.get(this.configDetailIndexMap.get("idxProgSellingQuantity")), measureUnit))
					.build()
					//.addProfile(this.buildProductProfile(marshallingDataRow))
					;
		} catch (Exception e) {
			logger.error(e);
		}
		return marshalledObject;
	}

	private InventoryDetail buildProductProfile(List<?> marshallingDataRow) {
		InventoryDetail productProfile = InventoryDetail.builder().build();
		return productProfile;
	}

	/*private Money buildPrice(Object priceInfo) {
		if (null==priceInfo)
			return null;

		return super.parseMoney(getDefaultCurrency()SystemOptionKey.COUNTRY_CODE.getDefaultValue(), CommonUtility.toBigDecimal(priceInfo.toString()));
	}*/

	private Quantity buildQuantity(Object quantityInfo, MeasureUnit measureUnit) {
		if (CommonUtility.isEmpty(quantityInfo))
			return null;

		Quantity quantity  = new Quantity(measureUnit, CommonUtility.toDouble(quantityInfo, 0));
		return quantity ;
	}

	/*private Currency getDefaultCurrency() {
		Currency defaultCurrency = Currency.builder()
		.code(CommonConstants.SYSTEM_CURRENCY_CODE)
		//.country(CommonConstants.SYSTEM_COUNTRY_CODE)
		.build()
		;
		defaultCurrency.setId(-1l);
		return defaultCurrency;
	}*/
	/**
	 * Example data 11.TUOI SONG>111.RAU CU, QUA>11111.RAU AN LA
	 */
	private Catalogue marshallingCatalogue(String inventoryGroupInfo) {
		int sepPos;
		String processingPart = null;
		String [] parts = null;
		String groupSep = ">";
		String partSep = "\\.";
		String processingInfo = inventoryGroupInfo;
		Catalogue parentCatalogue = null;
		Catalogue currentCatalogue = null;
		Optional<Catalogue> optCatalogue = null;
		while (processingInfo.length() > 0) {
			sepPos = processingInfo.indexOf(groupSep);
			if (sepPos != -1) {
				processingPart = processingInfo.substring(0, sepPos);
			} else {
				processingPart = processingInfo;
			}
			parts = processingPart.split(partSep);
			optCatalogue = this.catalogueService.getByCode(parts[0]);
			if (!optCatalogue.isPresent()) {
				currentCatalogue = Catalogue.builder()
						.parent(parentCatalogue)
						.code(parts[0])
						.name(parts[1])
						.build();
				parentCatalogue = this.catalogueService.saveOrUpdate(currentCatalogue);
				catalogueMap.put(parts[0], currentCatalogue);
			} else {
				parentCatalogue = optCatalogue.get();
				catalogueMap.put(optCatalogue.get().getCode(), optCatalogue.get());
			}
			if (sepPos < 0) {
				break;
			}
			processingInfo = processingInfo.substring(sepPos+1);
		}
		return (null!=currentCatalogue)?currentCatalogue:parentCatalogue;
	}

	private BusinessUnit fetchServicingBusinessUnit(Object businessObjectCode) {
		if (CommonUtility.isEmpty(businessObjectCode))
			return null;

		if (this.businessUnitMap.containsKey(businessObjectCode.toString())) {
			return this.businessUnitMap.get(businessObjectCode.toString());
		}

		BusinessUnit unmarshalledObject = businessUnitService.getObjectByCode(businessObjectCode.toString());
		if (null != unmarshalledObject) {
			this.businessUnitMap.put(unmarshalledObject.getCode(), unmarshalledObject);
		}
		return unmarshalledObject;
	}

	private MeasureUnit fetchMeasureUnit(String name) throws CerberusException {
		if (this.measureUnitMap.containsKey(name))
			return this.measureUnitMap.get(name);

		Optional<MeasureUnit> optMeasureUnit = this.measureUnitService.getByName(name);
		if (optMeasureUnit.isPresent()) {
			this.measureUnitMap.put(optMeasureUnit.get().getName(), optMeasureUnit.get());
			return optMeasureUnit.get();
		}

		String prefix = "UM"; //unit of measurement
		MeasureUnit fetchedObject = MeasureUnit.builder()
		.code(this.measureUnitService.nextSerial(prefix))
		.name(name)
		.build();

		this.measureUnitService.saveOrUpdate(fetchedObject);
		this.measureUnitMap.put(fetchedObject.getName(), fetchedObject);
		
		return fetchedObject;
	}
}
