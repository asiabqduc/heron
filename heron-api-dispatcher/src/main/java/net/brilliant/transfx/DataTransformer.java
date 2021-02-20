/**
 * 
 */
package net.brilliant.transfx;

import net.brilliant.ccs.exceptions.CerberusException;
import net.brilliant.framework.entity.RepoEntity;
import net.peaga.domain.base.Repository;

/**
 * @author ducbq
 *
 */
public interface DataTransformer {
  /**
  * This is the marshaling method which transforms all data from proxy object to an entity of a business object.
  * @param targetBusinessObject The target business object that will be transformed to. 
  * @param proxyObject The source proxy object which contained data for transforming from. 
  * @return Transformed business object.
  * @exception CerberusException On input error.
  * @see CerberusException
  */
	RepoEntity transformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject) throws CerberusException;

  /**
  * This is the marshaling method which transforms all data from proxy object to an entity of a business object.
  * @param targetBusinessObject The target business object that will be transformed to. 
  * @param proxyObject The source proxy object which contained data for transforming from. 
  * @param excludedAttributes The list of attributes not marshaling
  * @return Transformed business object.
  * @exception CerberusException On input error.
  * @see CerberusException
  */
	RepoEntity transformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject, String[] excludedAttributes) throws CerberusException;

	/**
  * This is the un-marshaling method which transforms all data from an entity of a business object to a proxy object.
  * @param targetBusinessObject The target business object that will be transformed to. 
  * @param proxyObject The source proxy object which contained data for transforming from. 
  * @return Transformed business object.
  * @exception CerberusException On input error.
  * @see CerberusException
  */
	Repository transformToProxy(final RepoEntity businessObject, Repository targetProxyObject) throws CerberusException;
}
