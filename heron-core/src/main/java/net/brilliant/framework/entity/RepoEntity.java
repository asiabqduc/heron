package net.brilliant.framework.entity;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import lombok.Getter;
import lombok.Setter;
import net.brilliant.domain.CoreObject;

@MappedSuperclass
public abstract class RepoEntity extends CoreObject implements Entity {
	private static final long serialVersionUID = 5174493359368640877L;

  @Setter @Getter
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private java.lang.Long id;

  @Setter @Getter
	@Column(name = "system")
	private Boolean system = Boolean.TRUE;

}