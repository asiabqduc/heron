
package net.brilliant.esi.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.brilliant.ccs.GlobalSharedConstants;
import net.brilliant.framework.entity.RepoEntity;

/**
 * 
 * @author ducbq
 * 
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "esi_schedule_plan")
public class SchedulePlan extends RepoEntity {
  /**
   * 
   */
  private static final long serialVersionUID = -2182183507700287961L;

  @Setter @Getter
  @Column(name = GlobalSharedConstants.PROP_CODE, unique = true, nullable=false, length=20)
  private String code;

  @Setter @Getter
	@Size(min = 1, max = 120, message = "{LongString}")
	@Column(name = GlobalSharedConstants.PROP_NAME, nullable=false)
	private String name;

	@Setter @Getter
	@Column(name = "start_time")
	private Date startTime;

  @Setter @Getter
  @Column(name = "end_time")
  private Date endTime;

  @Setter @Getter
  @Column(name = "last_trigger_time")
  private Date lastTriggerTime;

  @Setter @Getter
  @Column(name = "next_trigger_time")
  private Date nextTriggerTime;

  @Setter @Getter
	@Builder.Default
	@Column(name = "enabled", nullable=false)
	private Boolean enabled = Boolean.TRUE;

  @Setter @Getter
  @Column(name = "job_type", length=20)
  private String jobType; 

  @Setter @Getter
	@Column(name = "type", length=20)
	private String type; //Interval, Daily, Weekly, Monthly

  @Setter @Getter
  @Column(name = "info")
  private String info;
}