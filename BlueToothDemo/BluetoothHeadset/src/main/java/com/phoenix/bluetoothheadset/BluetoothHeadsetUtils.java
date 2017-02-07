package com.phoenix.bluetoothheadset;

import java.util.List;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;
import android.os.CountDownTimer;

public abstract class BluetoothHeadsetUtils {
	private Context mContext;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothHeadset mBluetoothHeadset;
	private BluetoothDevice mConnectedHeadset;

	private AudioManager mAudioManager;

	private boolean mIsOnHeadsetSco;
	private boolean mIsStarted;

	/**
	 * Constructor
	 * @param context
	 */
	public BluetoothHeadsetUtils(Context context) {
		mContext = context;
		//如果设备支持蓝牙就有值，不支持蓝牙就为空，目前手机默认都支持蓝牙，所以没有做判断
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
	}

	/**
	 * 调用此方法启动BluetoothHeadsetUtils功能
	 * Call this to start BluetoothHeadsetUtils functionalities.
	 * @return The return value of startBluetooth() or startBluetooth11()
	 */
	public boolean start() {
		if (mBluetoothAdapter.isEnabled() == false){
			mIsStarted = false;
			return mIsStarted;
		}
		if (!mIsStarted) {
			mIsStarted = true;
			//当要连接的时候，就可以获取一个Profile
			mIsStarted = startBluetooth();
		}
		return mIsStarted;
	}

	/**
	 * 应该调用onResume或onDestroy。 取消注册广播接收器并停止Sco音频连接并取消倒计时
	 * Should call this on onResume or onDestroy. Unregister broadcast receivers
	 * and stop Sco audio connection and cancel count down.
	 */
	public void stop() {
		if (mIsStarted) {
			mIsStarted = false;
			stopBluetooth();
		}
	}

	/**
	 * 
	 * @return true if audio is connected through headset.
	 */
	public boolean isOnHeadsetSco() {
		return mIsOnHeadsetSco;
	}

	public abstract void onHeadsetDisconnected();//已断开连接
	public abstract void onHeadsetConnected();//已连接
	public abstract void onScoAudioDisconnected();//SCO音频已断开
	public abstract void onScoAudioConnected();

	/**
	 * Register a headset profile listener
	 * @return false if device does not support bluetooth or current platform
	 *         does not supports use of SCO for off call or error in getting
	 *         profile proxy.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private boolean startBluetooth() {
		// Device support bluetooth
		if (mBluetoothAdapter != null) {
			if (mAudioManager.isBluetoothScoAvailableOffCall()) {
				//调用getProfileProxy方法就可以按照它指定的进行工作(这里需要BluetoothProfile.HEADSET这个Profile)，要知道结果就要通过mHeadsetProfileListener来实现
				// All the detection and audio connection are done in
				// mHeadsetProfileListener
				if (mBluetoothAdapter.getProfileProxy(mContext, mHeadsetProfileListener, BluetoothProfile.HEADSET)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * API >= 11 Unregister broadcast receivers and stop Sco audio connection
	 * and cancel count down.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	protected void stopBluetooth() {
		if (mBluetoothHeadset != null) {
			// Need to call stopVoiceRecognition here when the app
			// change orientation or close with headset still turns on.
			mBluetoothHeadset.stopVoiceRecognition(mConnectedHeadset);
			mContext.unregisterReceiver(mHeadsetBroadcastReceiver);
			mBluetoothAdapter.closeProfileProxy(BluetoothProfile.HEADSET, mBluetoothHeadset);
			mBluetoothHeadset = null;
		}
	}

	/**
	 * API >= 11 Check for already connected headset and if so start audio
	 * connection. Register for broadcast of headset and Sco audio connection
	 * states.
	 */
	private BluetoothProfile.ServiceListener mHeadsetProfileListener = new BluetoothProfile.ServiceListener() {
		/**
		 * This method is never called, even when we closeProfileProxy on
		 * onPause. When or will it ever be called???
		 */
		@Override
		public void onServiceDisconnected(int profile) {
			stopBluetooth();
		}

		/**
		 * 当设备连接上的时候
		 * @param profile
         * @param proxy
         */
		@SuppressWarnings("synthetic-access")
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onServiceConnected(int profile, BluetoothProfile proxy) {
			// mBluetoothHeadset is just a headset profile,
			// it does not represent a headset device.
			mBluetoothHeadset = (BluetoothHeadset) proxy;

			//getConnectedDevices()就可以知道是哪个设备连接上了
			// If a headset is connected before this application starts,
			// ACTION_CONNECTION_STATE_CHANGED will not be broadcast.
			// So we need to check for already connected headset.
			List<BluetoothDevice> devices = mBluetoothHeadset.getConnectedDevices();
			if (devices.size() > 0) {
				//虽然是一个数组，但同一时间只能有一个耳机连上，所以直接取第0个
				// Only one headset can be connected at a time,
				// so the connected headset is at index 0.
				mConnectedHeadset = devices.get(0);
				//进行回调，可以做一些操作
				onHeadsetConnected();
			}

			//由于运行期间有可能会被打断，所以还需要收听一些广播，以便知道蓝牙是否被其它程序给占用了
			//连接状态改变的广播
			// During the active life time of the app, a user may turn on and
			// off the headset.
			// So register for broadcast of connection states.
			mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED));
			//声音状态改变的广播
			// Calling startVoiceRecognition does not result in immediate audio
			// connection.
			// So register for broadcast of audio connection states. This
			// broadcast will
			// only be sent if startVoiceRecognition returns true.
			mContext.registerReceiver(mHeadsetBroadcastReceiver, new IntentFilter(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED));
		}
	};

	/**
	 * API >= 11 Handle headset and Sco audio connection states.
	 */
	private BroadcastReceiver mHeadsetBroadcastReceiver = new BroadcastReceiver() {

		@SuppressWarnings("synthetic-access")
		@TargetApi(Build.VERSION_CODES.HONEYCOMB)
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int state;
			//连接状态改变的时候
			if (action.equals(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)) {
				//获取它的状态，看是否还连着
				state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);
				//如果还连着
				if (state == BluetoothHeadset.STATE_CONNECTED) {
					mConnectedHeadset = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

					// Calling startVoiceRecognition always returns false here,
					// that why a count down timer is implemented to call
					// startVoiceRecognition in the onTick.

					// override this if you want to do other thing when the
					// device is connected.
					onHeadsetConnected();
				}
				//如果已断开
				else if (state == BluetoothHeadset.STATE_DISCONNECTED) {
					mConnectedHeadset = null;

					// override this if you want to do other thing when the
					// device is disconnected.
					onHeadsetDisconnected();
				}
			} else // audio
			{
				state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_AUDIO_DISCONNECTED);
				if (state == BluetoothHeadset.STATE_AUDIO_CONNECTED) {
					// override this if you want to do other thing when headset
					// audio is connected.
					onScoAudioConnected();
				} else if (state == BluetoothHeadset.STATE_AUDIO_DISCONNECTED) {
					mIsOnHeadsetSco = false;

					// override this if you want to do other thing when headset
					// audio is disconnected.
					onScoAudioDisconnected();
				}
			}
		}
	};
}
