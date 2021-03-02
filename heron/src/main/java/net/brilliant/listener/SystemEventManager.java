/**
 * 
 */
package net.brilliant.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.opencsv.exceptions.CsvException;

import net.brilliant.auth.helper.AuthDataDispatchRepositoryHelper;
import net.brilliant.ccs.GlobalSharedConstants;
import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.ccs.exceptions.EcosphereResourceException;
import net.brilliant.common.CSVUtilityHelper;
import net.brilliant.common.DateTimeUtility;
import net.brilliant.css.service.general.AttachmentService;
import net.brilliant.dmx.manager.GlobalDmxManager;
import net.brilliant.domain.entity.Attachment;
import net.brilliant.esi.common.SchedulingConstants;
import net.brilliant.esi.domain.entity.SchedulePlan;
import net.brilliant.esi.helper.SchedulerServicesHelper;
import net.brilliant.framework.component.CompCore;
import net.brilliant.model.Context;
import net.brilliant.osx.model.OSXConstants;

/**
 * @author ducbq
 *
 */
@Component
public class SystemEventManager extends CompCore {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7564662407839189753L;

	@Inject
	private AuthDataDispatchRepositoryHelper dataServiceDispatchHelper;
	//private DataServiceDispatchHelper dataServiceDispatchHelper;

  @Inject
  private SchedulerServicesHelper globalSchedulerService;

  /*@Inject
  private ResourceLoader resourceLoader;*/

  @Inject
  private GlobalDmxManager globalDmxManager;

  @Inject
  private AttachmentService attachmentService;

  @EventListener(ApplicationReadyEvent.class)
	public void onApplicationReady() {
    logger.info("Enter onApplicationReady");
		try {
		  configureMasterData();
		} catch (Exception e) {
			logger.error(e);
		}
		logger.info("Leave onApplicationReady");
	}

  protected void configureMasterData() throws IOException, CerberusException, ClassNotFoundException, SchedulerException, CsvException{
    logger.info("Enter initializeMasterData");
    this.dataServiceDispatchHelper.dispatch();

    Optional<Attachment> optAttachment = this.attachmentService.getByName(GlobalSharedConstants.APP_DEFAULT_CATALOUE_DATA);
    if (!optAttachment.isPresent()) {
      logger.info("There is no attachment represents the default catalog data!");
      Context context = Context.builder()
          .build()
          //.put(OSXConstants.INPUT_STREAM, this.globalDmxManager.getResourceInputStream(GlobalSharedConstants.APP_DATA_REPO_DIRECTORY + GlobalSharedConstants.APP_DEFAULT_CATALOUE_DATA))
          .put(OSXConstants.RESOURCE_REPO, GlobalSharedConstants.APP_DATA_REPO_DIRECTORY + GlobalSharedConstants.APP_DEFAULT_CATALOUE_DATA)
          .put(OSXConstants.RESOURCE_NAME, GlobalSharedConstants.APP_DEFAULT_CATALOUE_DATA)
          ;
      this.globalDmxManager.archive(context);
    }

    configureDefaultSchedulers();

    logger.info("Leave initializeMasterData");
  }

	private void configureDefaultSchedulers() throws SchedulerException, ClassNotFoundException, IOException, CsvException, CerberusException{
    logger.info("Enter initSchedulers");
    Context context = prepareSchedulesData();
	  globalSchedulerService.initSchedulers(context);
    //globalSchedulerService.start(context);
    logger.info("Leave initSchedulers");
	}

	private Context prepareSchedulesData() throws IOException, CsvException, CerberusException{
    InputStream inputStream = this.globalDmxManager.getResourceInputStream(GlobalSharedConstants.APP_DATA_REPO_DIRECTORY +"schedulers.osx");
    CSVUtilityHelper csvUtility = new CSVUtilityHelper();
    List<String[]> csvContextData = csvUtility.fetchCsvData(inputStream, SchedulingConstants.CSV_ELEMENT_SEPARATOR, 0);
    Context context = new Context();
    return context
        .put(SchedulingConstants.CTX_SCHEDULE_PLANS, 
            SchedulePlan.builder().code("20210215-SP-01")
            .name("Default schedule plan")
            .startTime(DateTimeUtility.getSystemDateTime())
            .jobType("System Job")
            .type("Root Schedule Plan")
            .build())
        .put(SchedulingConstants.CTX_JOB_SCHEDULE_ELEMENTS, csvContextData);
	}

	/*private InputStream getResourceInputStream(final String resourceName) throws IOException{
    Resource resource = this.resourceLoader.getResource(GlobalSharedConstants.APP_DATA_REPO_DIRECTORY + "schedulers.osx");
    if (null==resource)
      throw new EcosphereResourceException("Unable to get resource from path: " );

    return resource.getInputStream();
	}*/
}
