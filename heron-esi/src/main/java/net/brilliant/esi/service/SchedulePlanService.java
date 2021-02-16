package net.brilliant.esi.service;

import java.util.Optional;

import net.brilliant.esi.domain.entity.SchedulePlan;
import net.brilliant.exceptions.ObjectNotFoundException;
import net.brilliant.framework.service.GenericService;

public interface SchedulePlanService extends GenericService<SchedulePlan, Long> {

	/**
	 * Get one SchedulePlan with the provided code.
	 * 
	 * @param code
	 *            The SchedulePlan code
	 * @return The SchedulePlan
	 * @throws ObjectNotFoundException
	 *             If no such SchedulePlan exists.
	 */
	Optional<SchedulePlan> getByCode(String code) throws ObjectNotFoundException;
}
