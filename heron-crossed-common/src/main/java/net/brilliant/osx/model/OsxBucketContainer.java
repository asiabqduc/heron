/**
 * 
 */
package net.brilliant.osx.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author bqduc
 *
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OsxBucketContainer {
  @Setter @Getter
	private OfficeSuiteTarget suiteTargeted;

	@Setter @Getter @Builder.Default
	private Map<Object, OSXWorkbook> bucketData = new HashMap<>();

  @Setter @Getter
	private List<Object> container;

	public OsxBucketContainer(Object[] values) {
		this.container = new ArrayList<>();
		for (int idx = 0; idx < values.length; idx++) {
			this.container.add(values[idx]);
		}
	}

	public static OsxBucketContainer instance() {
		OsxBucketContainer dataBucket = new OsxBucketContainer();
		return dataBucket;
	}

	public static OsxBucketContainer getInstance(OfficeSuiteTarget suiteTargeted) {
		OsxBucketContainer dataBucket = new OsxBucketContainer();
		dataBucket.setSuiteTargeted(suiteTargeted);
		return dataBucket;
	}

	public Object getBucketedData(Object key){
		return bucketData.get(key);
	}

	public OsxBucketContainer putAll(Map<Object, OSXWorkbook> values) {
		this.bucketData.putAll(values);
		return this;
	}

	public OsxBucketContainer put(Object key, OSXWorkbook value) {
		this.bucketData.put(key, value);
		return this;
	}

	public Object get(Object key) {
		if (this.bucketData.containsKey(key))
			return this.bucketData.get(key);

		return null;
	}

	public boolean containsKey(Object key) {
		return this.bucketData.containsKey(key);
	}

	public Object pull(Object key) {
		if (this.bucketData.containsKey(key))
			return this.bucketData.remove(key);

		return null;
	}

	public Set<Object> getKeys(){
		return this.bucketData.keySet();
	}

  public Collection<OSXWorkbook> getValues(){
    return this.bucketData.values();
  }
	
}
