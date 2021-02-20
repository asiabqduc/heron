/**
 * 
 */
package net.brilliant.comm.comp;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import net.brilliant.ccs.exceptions.CommunicatorException;
import net.brilliant.comm.domain.CorpMimeMessage;
import net.brilliant.model.Context;

/**
 * @author ducbq
 *
 */
@Component(value="emailCommunicator")
public class EmailCommunicator implements Communicator {

	@Inject 
	private CommunicatorServiceHelper communicatorServiceHelper;

	@Override
	public void sendEmail(CorpMimeMessage mailMessage) throws CommunicatorException {
		System.out.println("net.paramount.comm.component.EmailServiceImpl.send(MailMessage)");
	}

	@Override
	public void send(Context context) throws CommunicatorException {
		communicatorServiceHelper.sendEmail(context);
	}
}
