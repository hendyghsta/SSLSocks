package link.infra.sslsocks.service;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class StunnelIntentService extends IntentService {
	public static final String ACTION_STARTNOVPN = "link.infra.sslsocks.service.action.STARTNOVPN";
	public static final String ACTION_RESUMEACTIVITY = "link.infra.sslsocks.service.action.RESUMEACTIVITY";

	private StunnelProcessManager processManager = new StunnelProcessManager();
	public String pendingLog;

	public StunnelIntentService() {
		super("StunnelIntentService");
	}

	/**
	 * Starts this service to perform action Foo with the given parameters. If
	 * the service is already performing a task this action will be queued.
	 *
	 * @see IntentService
	 */
	public static void start(Context context) {
		Intent intent = new Intent(context, StunnelIntentService.class);
		intent.setAction(ACTION_STARTNOVPN);
		context.startService(intent);
	}

	public static void checkStatus(Context context) {
		Intent localIntent = new Intent(ACTION_RESUMEACTIVITY);
		// Broadcasts the Intent to receivers in this app.
		LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		if (intent != null) {
			final String action = intent.getAction();
			if (ACTION_STARTNOVPN.equals(action)) {
				handleStart();
			}
		}
	}

	/**
	 * Handle start action in the provided background thread with the provided
	 * parameters.
	 */
	private void handleStart() {
		LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
		IntentFilter resumeIntentFilter = new IntentFilter(ACTION_RESUMEACTIVITY);
		final StunnelIntentService ctx = this;
		BroadcastReceiver resumeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				ServiceUtils.broadcastStarted(ctx);
				ServiceUtils.broadcastPreviousLog(ctx);
			}
		};
		manager.registerReceiver(resumeReceiver, resumeIntentFilter);

		ServiceUtils.showNotification(this);
		processManager.start(this);
	}

	public void onDestroy() {
		processManager.stop(this);
		ServiceUtils.removeNotification(this);
		ServiceUtils.broadcastStopped(this);
		super.onDestroy();
	}
}
