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
public abstract class DataTransformerBase {
	protected RepoEntity doTransformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject) throws CerberusException {
		return targetBusinessObject;
	}

	protected RepoEntity doTransformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject, String[] excludedAttributes) throws CerberusException {
		return targetBusinessObject;
	}

	protected Repository doTransformToProxy(final RepoEntity businessObject, Repository targetProxyObject) throws CerberusException {
		return targetProxyObject;
	}

	public final RepoEntity transformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject) throws CerberusException {
		return doTransformToBusiness(proxyObject, targetBusinessObject);
	}

	public final RepoEntity transformToBusiness(final Repository proxyObject, RepoEntity targetBusinessObject, String[] excludedAttributes) throws CerberusException {
		return doTransformToBusiness(proxyObject, targetBusinessObject, excludedAttributes);
	}

	public final Repository transformToProxy(final RepoEntity businessObject, Repository targetProxyObject) throws CerberusException {
		return doTransformToProxy(businessObject, targetProxyObject);
	}
}
