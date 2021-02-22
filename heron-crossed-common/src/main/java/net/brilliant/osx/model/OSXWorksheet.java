/**
 * 
 */
package net.brilliant.osx.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author bqduc
 *
 */
@Builder
public class OSXWorksheet extends OSXContainer {
  @Setter @Getter
  private String id;

	@Builder.Default
	private Map<Integer, List<?>> dataRows = new HashMap<>();

	public List<?> getDataRow(Integer rowIdx) {
		if (!dataRows.containsKey(rowIdx))
			return null;

		return dataRows.get(rowIdx);
	}

	public void addDataRows(Integer key, List<?> dataRows) {
		this.dataRows.put(key, dataRows);
	}

	public Set<Integer> getKeys() {
		return this.dataRows.keySet();
	}

	public Collection<List<?>> getValues() {
		return this.dataRows.values();
	}

  public int getSize() {
    return this.dataRows.size();
  }
}
