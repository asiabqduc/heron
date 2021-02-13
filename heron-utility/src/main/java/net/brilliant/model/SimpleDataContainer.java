/**
 * 
 */
package net.brilliant.model;

import java.util.List;

import lombok.Builder;
import net.brilliant.common.CollectionsUtility;
import net.brilliant.model.base.DataList;
import net.brilliant.model.base.IDataContainer;

/**
 * @author bqduc
 *
 */
@Builder
public class SimpleDataContainer implements IDataContainer<String> {
	@Builder.Default
	private List<String> headerItems = CollectionsUtility.createArrayList();

	@Builder.Default
	private DataList<List<String>> dataItems = CollectionsUtility.createDataList();

	@Override
	public DataList<List<String>> getDataItems() {
		return this.dataItems;
	}

	@Override
	public List<String> getHeaderItems() {
		return this.headerItems;
	}

	public void addHeaderItems(List<String> headerItems){
		this.headerItems.addAll(headerItems);
	}

	public void addDataItems(List<String> dataItems){
		this.dataItems.add(dataItems);
	}
}
