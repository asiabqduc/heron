/**
 * 
 */
package net.brilliant.dmx.repository;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.brilliant.dmx.postconstruct.GlobalDataServiceHelper;
import net.brilliant.framework.component.CompCore;

/**
 * @author ducbq
 *
 */
@Component
public class GlobaRepositoryManagement extends CompCore {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4395890941278552748L;

	@Inject
	private GlobalDataServiceHelper globalDataServiceHelper;

	public void initMasterData() {
		logger.info(String.join(LOG_ENTRY_ENTER, "GlobalDataRepository", "::", "initMasterData"));
		initInventoryItems();
		logger.info(String.join(LOG_ENTRY_LEAVE, "GlobalDataRepository", "::", "initMasterData"));
	}

	protected void initInventoryItems() {
		//String logSpec = String.join(this.getClass().getSimpleName(), "::", Thread.currentThread().getStackTrace()[1].getMethodName());
		//log.info(String.join(LOG_ENTRY_ENTER, logSpec));
		//log.info(String.join(LOG_ENTRY_LEAVE, logSpec));
	}

	public void initiateMasterData() {
		logger.info("Enter initiateMasterData");
		globalDataServiceHelper.initialize();
		logger.info("Leave initiateMasterData");
	}
}
