package at.tugraz.ist.akm.content;

import android.net.Uri;

public class SmsContent {
	public static class ContentUri {
		public final static Uri BASE_URI = Uri.parse("content://sms");
		public final static Uri INBOX_URI = Uri.withAppendedPath(BASE_URI,
				"inbox");
		public final static Uri OUTBOX_URI = Uri.withAppendedPath(BASE_URI,
				"outbox");
		public final static Uri SENT_URI = Uri.withAppendedPath(BASE_URI,
				"sent");
		public final static Uri DRAFT_URI = Uri.withAppendedPath(BASE_URI,
				"draft");
		public final static Uri UNDELIVERED_URI = Uri.withAppendedPath(
				BASE_URI, "undelivered");
		public final static Uri FAILED_URI = Uri.withAppendedPath(BASE_URI,
				"failed");
		public final static Uri QUEUED_URI = Uri.withAppendedPath(BASE_URI,
				"queued");
	}

	public static class Content {
		/**
		 * column name
		 */
		public final static String ID = "_id";
		/**
		 * column name
		 */
		public final static String THREAD_ID = "thread_id";
		/**
		 * column name
		 */
		public final static String ADDRESS = "address";
		/**
		 * column name
		 */
		public final static String PERSON = "person";
		/**
		 * column name
		 */
		public final static String DATE = "date";
		/**
		 * column name
		 */
		public final static String PROTOCOL = "protocol";
		/**
		 * column name
		 */
		public final static String READ = "read";
		/**
		 * column name
		 */
		public final static String STATUS = "status";
		/**
		 * column name
		 */
		public final static String MESSAGE_TYPE = "type";
		/**
		 * column values
		 */
        public static final String MESSAGE_TYPE_ALL    = "0";
        public static final String MESSAGE_TYPE_INBOX  = "1";
        public static final String MESSAGE_TYPE_SENT   = "2";
        public static final String MESSAGE_TYPE_DRAFT  = "3";
        public static final String MESSAGE_TYPE_OUTBOX = "4";
        public static final String MESSAGE_TYPE_FAILED = "5"; // for failed outgoing messages
        public static final String MESSAGE_TYPE_QUEUED = "6"; // for sms to send later
        		
		/**
		 * column name
		 */
		public final static String REPLY_PATH_PRESENT = "reply_path_present";
		/**
		 * column name
		 */
		public final static String SUBJECT = "subject";
		/**
		 * column name
		 */
		public final static String BODY = "body";
		/**
		 * column name
		 */
		public final static String SERVICE_CENTER = "service_center";
		/**
		 * column name
		 */
		public final static String LOCKED = "locked";
		/**
		 * column name
		 */
		public final static String ERROR_CODE = "error_code";
		/**
		 * column name
		 */
		public final static String SEEN = "seen";
	}
}