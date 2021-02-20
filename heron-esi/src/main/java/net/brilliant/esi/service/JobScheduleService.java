package net.brilliant.esi.service;

import java.util.Optional;

import net.brilliant.ccs.exceptions.ObjectNotFoundException;
import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.framework.service.GenericService;

public interface JobScheduleService extends GenericService<JobSchedule, Long> {

	/**
	 * Get one JobSchedule with the provided code.
	 * 
	 * @param code
	 *            The JobSchedule code
	 * @return The JobSchedule
	 * @throws ObjectNotFoundException
	 *             If no such JobSchedule exists.
	 */
	Optional<JobSchedule> getByCode(String code) throws ObjectNotFoundException;
}
