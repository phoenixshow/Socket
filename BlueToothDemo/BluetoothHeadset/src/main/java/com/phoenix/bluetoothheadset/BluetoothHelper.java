package com.phoenix.bluetoothheadset;

import android.content.Context;
import android.media.AudioManager;


/** 
 * @Description: 
 * @author yingjie.lin 
 * @date 2014年10月17日 上午11:00:02
 */

public class BluetoothHelper extends BluetoothHeadsetUtils {
	private final static String TAG = BluetoothHelper.class.getSimpleName();
	Context mContext;
//	int mCallvol;
	int mMediaVol;
	AudioManager mAudioManager;

	public BluetoothHelper(Context context) {
		super(context);
		mContext = context;
		mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		/**
		 * 声音类型，可取为STREAM_VOICE_CALL（通话）、STREAM_SYSTEM（系统声音）、STREAM_RING（铃声）、STREAM_MUSIC（音乐）、STREAM_ALARM（闹铃声）
		 */
//		mCallvol = mAudioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
		mMediaVol = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}

	//已断开连接
	@Override
	public void onHeadsetDisconnected() {
		mAudioManager.setBluetoothScoOn(false);
	}

	//已连接
	@Override
	public void onHeadsetConnected() {
		mAudioManager.setBluetoothScoOn(true); // 打开SCO
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	}

	//SCO音频已断开
	@Override
	public void onScoAudioDisconnected() {
		/**
		 * 直接设置音量大小
		 * int streamType, int index, intflags
		 */
//		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mCallvol, 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mMediaVol, 0);
	}

	//SCO音频已连接
	@Override
	public void onScoAudioConnected() {
//		mAudioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL), 0);
		mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
	}

}
