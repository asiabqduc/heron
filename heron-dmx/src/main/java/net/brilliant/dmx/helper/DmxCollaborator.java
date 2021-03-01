/**
 * 
 */
package net.brilliant.dmx.helper;

import org.springframework.stereotype.Component;

import net.brilliant.osx.model.MarshallingObjects;

/**
 * This class to be hold all configuration for OSX and data loading and can be load data from database in future.
 * First, loading configuration from database and holds in this class for using afterward
 * 
 * @author ducbq
 *
 *
 */
@Component
public class DmxCollaborator {
	public String getConfiguredContactWorkbookId() {
		return "Vietbank_14.000.xlsx";
	}

	public String[] getConfiguredContactWorksheetIds() {
		return new String[] {"File Tổng hợp"};
	}

	public String getConfiguredDataCatalogueWorkbookId() {
		return "data-catalog.xlsx";
	}

	public String[] getConfiguredDataCatalogueWorksheetIds() {
		return new String[] {
				MarshallingObjects.ITEMS.getName(), 
				MarshallingObjects.LOCALIZED_ITEMS.getName(), 
				MarshallingObjects.LANGUAGES.getName(),
				MarshallingObjects.INVENTORY_ITEMS.getName()
		};
	}

	public String getConfiguredDataPackage() {
		return "/data/marshall/develop_data.zip";
	}

	public String getConfiguredDataLoadingEntry() {
		return "data/marshall/develop_data.zip";
	}
}
