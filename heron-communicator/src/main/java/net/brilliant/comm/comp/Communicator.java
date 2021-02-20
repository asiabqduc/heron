/**
 * 
 */
package net.brilliant.comm.comp;

import net.brilliant.ccs.exceptions.CommunicatorException;
import net.brilliant.comm.domain.CorpMimeMessage;
import net.brilliant.model.Context;

/**
 * @author ducbq
 *
 */
public interface Communicator {
	void sendEmail(CorpMimeMessage mailMessage) throws CommunicatorException;
	void send(Context context) throws CommunicatorException;
}
