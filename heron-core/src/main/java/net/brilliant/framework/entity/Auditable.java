/**
 * 
 */
package net.brilliant.framework.entity;

import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * @author ducbq
 *
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable<T> extends RepoEntity {
	private static final long serialVersionUID = -4047672987189874306L;

	/*@Setter @Getter
	@CreatedBy
  @Column(name="created_by")
  protected T createdBy;

  @Setter @Getter
  @CreatedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name="creation_date")
  protected Date creationDate;

  @Setter @Getter
  @LastModifiedBy
  @Column(name="last_modified_by")
  private T lastModifiedBy;

  @Setter @Getter
  @LastModifiedDate
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name="last_modified")
  protected Date lastModified;*/

}
