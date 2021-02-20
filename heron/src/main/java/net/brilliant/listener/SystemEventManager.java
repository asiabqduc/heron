/**
 * 
 */
package net.brilliant.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.inject.Inject;

import org.quartz.SchedulerException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import com.opencsv.exceptions.CsvException;

import net.brilliant.auth.helper.AuthDataDispatchRepositoryHelper;
import net.brilliant.ccs.exceptions.EcosphereResourceException;
import net.brilliant.common.CSVUtilityHelper;
import net.brilliant.common.DateTimeUtility;
import net.brilliant.esi.common.SchedulingConstants;
import net.brilliant.esi.domain.entity.SchedulePlan;
import net.brilliant.esi.helper.SchedulerServicesHelper;
import net.brilliant.framework.component.CompCore;
import net.brilliant.model.Context;

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

  @Inject
  private ResourceLoader resourceLoader;

  @EventListener(ApplicationReadyEvent.class)
	public void onApplicationReadyEventListener() {
		try {
			dataServiceDispatchHelper.dispatch();
			initSchedulers();
		} catch (Exception e) {
			log.error(e);
		}
		log.info("Leave onApplicationReadyEventListener");
	}

	private void initSchedulers() throws SchedulerException, ClassNotFoundException, IOException, CsvException{
    Context context = prepareSchedulesData();
	  log.info("Enter initSchedulers");
	  globalSchedulerService.initSchedulers(context);
    //globalSchedulerService.start(context);
    log.info("Leave initSchedulers");
	}

	private Context prepareSchedulesData() throws IOException, CsvException{
    Resource resource = this.resourceLoader.getResource("classpath:/master/schedulers.osx");
    if (null==resource)
      throw new EcosphereResourceException("Unable to get resource from path: " );

    InputStream inputStream = null;
    try {
      inputStream = resource.getInputStream();
    } catch (IOException e) {
      e.printStackTrace();
    }

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
}
