
package net.brilliant.esi.domain.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
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
@Table(name = "esi_job_schedule")
public class JobSchedule extends RepoEntity {
	/**
   * 
   */
  private static final long serialVersionUID = -6399087635347557087L;

  @Setter @Getter
  @Column(name = GlobalSharedConstants.PROP_CODE, unique = true, nullable=false, length=20)
  private String code;

  @Setter @Getter
  @Size(min = 1, max = 120, message = "{LongString}")
  @Column(name = GlobalSharedConstants.PROP_NAME, nullable=false)
  private String name;

	@Setter @Getter
	@Size(min = 1, max = 150, message = "{LongString}")
	@Column(name = "display_name")
	private String displayName;

	@Setter @Getter
	@Builder.Default
	@Column(name = "enabled", nullable=false)
	private Boolean enabled = Boolean.TRUE;

	@Setter @Getter
	@Column(name = "cron_expression", length=20)
	private String cronExpression;

  @Setter @Getter
  @Column(name = "category", length=20)
  private String category;

  @Setter @Getter
  @Column(name = "job_class", length=120)
  private String jobClass;

  @Setter @Getter
  @Column(name = "last_trigger_time")
  private Date lastTriggerTime;

  @Setter @Getter
	@ManyToOne
	@JoinColumn(name = "schedule_plan_id")
	private SchedulePlan schedulePlan;

  @Column(name = "info", columnDefinition="TEXT")
  private String info;

  @Setter @Getter
  @Transient
  private String cronExpressionReadable;

  @Setter @Getter
  @Transient
  private String status;
}