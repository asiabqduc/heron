/**
 * 
 */
package net.brilliant.common;

import java.util.List;

import net.brilliant.model.GenderType;

/**
 * @author bqduc
 *
 */
public class GenderTypeUtility {
	//private static final List<String> statusPatterns = CollectionsUtility.arraysAsList(new String[]{"Hiệu lực", "Mất hiệu lực", "Có hiệu lực", "Đang làm việc", "Làm việc"});
	
	private static final List<String> femalePatterns = CollectionsUtility.arraysAsList(new String[]{"NU", "NỮ", "FEMALE", "BÀ", "CÔ"});
	private static final List<String> malePatterns = CollectionsUtility.arraysAsList(new String[]{"NAM", "MALE", "ÔNG"});

	public static GenderType getGenderType(String src){
		if (femalePatterns.contains(src.toUpperCase()))
			return GenderType.Female;

		if (malePatterns.contains(src.toUpperCase()))
			return GenderType.Male;

		return GenderType.Unknown;
	}

	
}
