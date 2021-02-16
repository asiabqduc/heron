package net.brilliant.esi.service.impl;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import net.brilliant.esi.domain.entity.JobSchedule;
import net.brilliant.esi.repository.JobScheduleRepository;
import net.brilliant.esi.service.JobScheduleService;
import net.brilliant.framework.repository.BaseRepository;
import net.brilliant.framework.service.GenericServiceImpl;


@Service
public class JobScheduleServiceImpl extends GenericServiceImpl<JobSchedule, Long> implements JobScheduleService {
	/**
   * 
   */
  private static final long serialVersionUID = 7619068697797499651L;
  @Inject 
	private JobScheduleRepository repository;
	
	protected BaseRepository<JobSchedule, Long> getRepository() {
		return this.repository;
	}
}
