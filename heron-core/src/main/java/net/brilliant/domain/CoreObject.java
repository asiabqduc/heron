/**
 * 
 */
package net.brilliant.domain;

import java.beans.Transient;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

/**
 * @author ducbq
 *
 */
public class CoreObject implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 866395332802166189L;

  @javax.persistence.Transient
  @Setter @Getter
  private Long id;

  @javax.persistence.Transient
  @Setter @Getter
  private Boolean active = Boolean.TRUE;

  /**
   * Determines whether another object is equal to this business object.  The result is 
   * <code>true</code> if and only if the argument is not null and is a business object that 
   * has the same id field values as this object.
   * @param object the reference object with which to compare
   * @return <code>true</code> if this object is the same as the argument;
   * <code>false</code> otherwise.
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;

    CoreObject other = (CoreObject) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id)) {
      return false;
    }

    return true;
  }

  public int compareTo(Object obj) {
    if (obj.hashCode() > hashCode())
      return 1;
    else if (obj.hashCode() < hashCode())
      return -1;

    return 0;
  }

  public String toString() {
    return new StringBuilder("Id: ").append(this.id).toString();
  }

  @Transient
  public boolean isSelected(Long id) {
    if (null != id) {
      return this.getId().equals(id);
    }
    return false;
  }
}
