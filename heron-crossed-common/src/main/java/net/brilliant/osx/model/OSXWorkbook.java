/**
 * 
 */
package net.brilliant.osx.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.Builder;

/**
 * @author bqduc
 *
 */
@Builder
public class OSXWorkbook extends OSXContainer {
	@Builder.Default
	private Map<Object, OSXWorksheet> worksheets = new HashMap<>();

	public OSXWorkbook put(Object key, OSXWorksheet worksheet) {
		this.worksheets.put(key, worksheet);
		return this;
	}

	public Set<?> getKeys(){
		return this.worksheets.keySet();
	}

	public Collection<OSXWorksheet> datasheets(){
		return this.worksheets.values();
	}

	public OSXWorksheet getDatasheet(Object key){
    if (!this.worksheets.containsKey(key))
      return null;

    return this.worksheets.get(key);
	}
}
