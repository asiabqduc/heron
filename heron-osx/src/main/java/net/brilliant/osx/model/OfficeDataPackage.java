/**
 * 
 */
package net.brilliant.osx.model;

import java.util.Map;

import lombok.Builder;
import net.brilliant.common.CollectionsUtility;

/**
 * @author bqduc
 *
 */
@Builder
public class OfficeDataPackage extends OSXContainer {
	@Builder.Default
	private Map<Object, OSXContainer> packageData = CollectionsUtility.createMap();

	public OSXContainer getDataPackage(final Object key){
		return packageData.get(key);
	}

	public OfficeDataPackage putAll(final Map<Object, ? extends OSXContainer> values) {
		this.packageData.putAll(values);
		return this;
	}

	public OfficeDataPackage put(final Object key, final OSXContainer value) {
		this.packageData.put(key, value);
		return this;
	}
}
