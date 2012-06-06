package at.tugraz.ist.akm.texting;

import java.util.List;

import android.content.Context;
import at.tugraz.ist.akm.content.query.ContactFilter;
import at.tugraz.ist.akm.content.query.TextMessageFilter;
import at.tugraz.ist.akm.phonebook.Contact;
import at.tugraz.ist.akm.phonebook.ContactModifiedCallback;
import at.tugraz.ist.akm.phonebook.PhonebookBridge;
import at.tugraz.ist.akm.sms.SmsBridge;
import at.tugraz.ist.akm.sms.SmsIOCallback;
import at.tugraz.ist.akm.sms.TextMessage;
import at.tugraz.ist.akm.texting.reports.VolatileIncomingReport;
import at.tugraz.ist.akm.texting.reports.VolatileOutgoingReport;
import at.tugraz.ist.akm.texting.reports.VolatilePhonebookReport;
import at.tugraz.ist.akm.trace.Logable;

public class TextingAdapter extends Logable implements TextingInterface,
		SmsIOCallback, ContactModifiedCallback {

	private Context mContext = null;
	private SmsBridge mSmsBridge = null;
	private PhonebookBridge mPhoneBook = null;

	private SmsIOCallback mExternalTextMessageCallback = null;
	private ContactModifiedCallback mExternalPhonebookModifiedCallback = null;

	private VolatileOutgoingReport mOutgoingStatistics = new VolatileOutgoingReport();
	private VolatileIncomingReport mIncomingStatistics = new VolatileIncomingReport();
	private VolatilePhonebookReport mPhonebookStatistics = new VolatilePhonebookReport();

	public TextingAdapter(Context context, SmsIOCallback smsIOCallback,
			ContactModifiedCallback contactModifiedCallback) {
		super(TextingAdapter.class.getSimpleName());
		mContext = context;

		mExternalTextMessageCallback = smsIOCallback;
		mExternalPhonebookModifiedCallback = contactModifiedCallback;

		mSmsBridge = new SmsBridge(mContext);
		mPhoneBook = new PhonebookBridge(mContext);
	}

	public void start() {
		logVerbose("power up ...");
		registerSmsCallbacks();
		registerPhonebookCallbacks();
		mSmsBridge.start();
		mPhoneBook.start();
	}

	public void stop() {
		logVerbose("power down ...");
		mPhoneBook.stop();
		mSmsBridge.stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tugraz.ist.akm.texting.TextingInterface#sendTextMessage(at.tugraz.
	 * ist.akm.sms.TextMessage)
	 */
	@Override
	public int sendTextMessage(TextMessage message) {
		mOutgoingStatistics
				.setNumPending(mOutgoingStatistics.getNumPending() + 1);
		return mSmsBridge.sendTextMessage(message);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * at.tugraz.ist.akm.texting.TextingInterface#fetchTextMessages(at.tugraz
	 * .ist.akm.content.query.TextMessageFilter)
	 */
	@Override
	public List<TextMessage> fetchTextMessages(TextMessageFilter filter) {
		return mSmsBridge.fetchTextMessages(filter);
	}

	@Override
	public List<Contact> fetchContacts(ContactFilter filter) {
		return mPhoneBook.fetchContacts(filter);
	}

	@Override
	public void smsSentCallback(Context context, List<TextMessage> message) {
		logVerbose("sms sent successfully");
		mOutgoingStatistics
				.setNumPending(mOutgoingStatistics.getNumPending() - 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentCallback(context, message);
		}
	}

	@Override
	public void smsSentErrorCallback(Context context, List<TextMessage> message) {
		logVerbose("failed to send sms");
		mOutgoingStatistics.setNumErroneous(mOutgoingStatistics
				.getNumErroneous() + 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsSentErrorCallback(context, message);
		}
	}

	@Override
	public void smsDeliveredCallback(Context context, List<TextMessage> message) {
		logVerbose("sms delivered");
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsDeliveredCallback(context, message);
		}

	}

	@Override
	public void smsReceivedCallback(Context context, List<TextMessage> message) {
		logVerbose("sms received");
		mIncomingStatistics
				.setNumReceived(mIncomingStatistics.getNumReceived() + 1);
		if (mExternalTextMessageCallback != null) {
			mExternalTextMessageCallback.smsReceivedCallback(context, message);
		}
	}

	@Override
	public void contactModifiedCallback() {
		logVerbose("contact modified");
		mPhonebookStatistics
				.setNumChanges(mPhonebookStatistics.getNumChanges() + 1);
		if (mExternalPhonebookModifiedCallback != null) {
			mExternalPhonebookModifiedCallback.contactModifiedCallback();
		}
	}

	@Override
	public int updateTextMessage(TextMessage message) {
		return mSmsBridge.updateTextMessage(message);
	}

	/**
	 * return all tread IDs for the specified address
	 * 
	 * @param address
	 *            the phone number
	 */
	@Override
	public List<Integer> fetchThreadIds(final String address) {
		return mSmsBridge.fetchThreadIds(address);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the outgoing state.
	 */
	@Override
	public VolatileOutgoingReport getOutgoingReport() {
		return new VolatileOutgoingReport(mOutgoingStatistics);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the incoming state.
	 */
	@Override
	public VolatileIncomingReport getIncomingReport() {
		return new VolatileIncomingReport(mIncomingStatistics);
	}

	/**
	 * This method don't touches anything internally but gives you a new
	 * instance you can do anything you want with.
	 * 
	 * @return A brief information about the contact (changes, updated) state.
	 */
	@Override
	public VolatilePhonebookReport getPhonebookReport() {
		return new VolatilePhonebookReport(mPhonebookStatistics);
	}

	private void registerSmsCallbacks() {
		mSmsBridge.setSmsSentCallback(this);
	}

	private void registerPhonebookCallbacks() {
		mPhoneBook
				.setContactModifiedCallback(this);
	}

}
