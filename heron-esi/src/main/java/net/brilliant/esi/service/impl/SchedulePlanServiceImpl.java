package net.brilliant.esi.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import net.brilliant.esi.domain.entity.SchedulePlan;
import net.brilliant.esi.repository.SchedulePlanRepository;
import net.brilliant.esi.service.SchedulePlanService;
import net.brilliant.framework.repository.BaseRepository;
import net.brilliant.framework.service.GenericServiceImpl;


@Service
public class SchedulePlanServiceImpl extends GenericServiceImpl<SchedulePlan, Long> implements SchedulePlanService{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7409137083581193634L;

	@Inject 
	private SchedulePlanRepository repository;
	
	protected BaseRepository<SchedulePlan, Long> getRepository() {
		return this.repository;
	}
}
