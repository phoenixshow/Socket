package com.phoenix.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.util.Log;

import java.util.List;

public class WifiUtils {
	// 定义WifiManager对象
	private WifiManager mWifiManager;
	// 定义WifiInfo对象
	private WifiInfo mWifiInfo;

	private StringBuffer mStringBuffer = new StringBuffer();
	private List<ScanResult> listResult;
	private ScanResult mScanResult;

	// 定义一个WifiLock
	private WifiLock mWifiLock;
	// 网络连接列表
	private List<WifiConfiguration> mWifiConfiguration;

	/**
	 * 构造方法
	 */
	public WifiUtils(Context context) {
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		mWifiInfo = mWifiManager.getConnectionInfo();
	}

	/**
	 * 打开Wifi网卡
	 */
	public void startWifi() {
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	/**
	 * 关闭Wifi网卡
	 */
	public void stopWifi() {
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}

	/**
	 * 检查当前Wifi网卡状态
	 */
	public String checkWifiState() {
		String state = "(x___x)……额~没有获取到状态……(x___x)";
		if (mWifiManager.getWifiState() == 0) {
			state = "正在关闭";
		} else if (mWifiManager.getWifiState() == 1) {
			state = "已经关闭";
		} else if (mWifiManager.getWifiState() == 2) {
			state = "正在打开";
		} else if (mWifiManager.getWifiState() == 3) {
			state = "已经打开";
		}
		return state;
	}

	/**
	 * 扫描周边网络
	 */
	public String scan() {
		mWifiManager.startScan();
//		// 得到配置好的网络连接
//		mWifiConfiguration = mWifiManager.getConfiguredNetworks();
		return "正在扫描，请等待扫描结果";
	}

	/**
	 * 得到扫描结果
	 */
	public String getScanResult() {
		// 每次点击扫描之前清空上一次的扫描结果
		if (mStringBuffer != null) {
			mStringBuffer = new StringBuffer();
		}
		listResult = mWifiManager.getScanResults();
		if (listResult != null) {
			for (int i = 0; i < listResult.size(); i++) {
				mScanResult = listResult.get(i);
				mStringBuffer = mStringBuffer.append("NO.").append(i + 1).append(" :\n")
						.append("网络名称：").append(mScanResult.SSID).append("\n")
						.append("接入点的地址：").append(mScanResult.BSSID).append("\n")
						.append("功能：").append(mScanResult.capabilities).append("\n")
						.append("频率：").append(mScanResult.frequency).append("\n")
						.append("信号强度：").append(mScanResult.level).append("\n\n");
			}
		}
		return mStringBuffer.toString();
	}

	/**
	 * 连接信息
	 */
	public String connect() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		return "网络名称:"+getSSID() + "\r\n" +
				"MAC地址:"+getMacAddress() + "\r\n" +
				"接入点的BSSID:"+getBSSID() + "\r\n" +
				"IP地址:"+getIPAddress() + "\r\n" +
				"连接的ID:"+getNetworkId() + "\r\n" +
				"WifiInfo的所有信息包:"+getWifiInfo();
	}

	// 得到WifiInfo的所有信息包
	public String getWifiInfo() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
	}

	// 得到网络名称
	public String getSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getSSID();
	}

	// 得到MAC地址
	public String getMacAddress() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
	}

	// 得到接入点的BSSID
	public String getBSSID() {
		return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
	}

	/**
	 * 得到连接的ID
	 */
	public int getNetworkId() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
	}

	/**
	 * 得到IP地址
	 */
	public int getIPAddress() {
		return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
	}

	/**
	 * 断开当前连接的网络
	 */
	public void disconnectWifi() {
		int netId = getNetworkId();
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
//		mWifiInfo = null;
	}

	// 断开指定ID的网络
	public void disconnectWifi(int netId) {
		mWifiManager.disableNetwork(netId);
		mWifiManager.disconnect();
	}

	/**
	 * 检查当前网络状态
	 * @return String
	 */
	public String checkNetWorkState() {
		mWifiInfo = mWifiManager.getConnectionInfo();
		String result = "网络已断开";
		if (mWifiInfo != null) {
			result = "网络正常";
		}
		return result;
	}

	/**
	 * 在手机屏幕关闭之后，并且其他的应用程序没有在使用wifi的时候，系统大概在两分钟之后，
	 * 会关闭wifi，使得wifi处于睡眠状态。这样的做法，有利于电源能量的节省和延长电池寿命等。
	 * Android为wifi提供了一种叫WifiLock的锁，能够阻止wifi进入睡眠状态，
	 * 使wifi一直处于活跃状态。这种锁，在下载一个较大的文件的时候，比较适合使用
	 */
	// 锁定WifiLock
	public void acquireWifiLock() {
		mWifiLock.acquire();
	}

	// 解锁WifiLock
	public void releaseWifiLock() {
		// 判断是否持有/锁定
		if (mWifiLock.isHeld()) {
			mWifiLock.release();
		}
	}

	/**
	 * 创建一个WifiLock
	 * @param lockName 锁的名称
	 * @return
	 */
	public WifiLock creatWifiLock(String lockName) {
		mWifiLock = mWifiManager.createWifiLock(lockName);
		return mWifiLock;
	}

	// 得到配置好的网络
	public List<WifiConfiguration> getConfiguration() {
		return mWifiConfiguration;
	}

	// 指定配置好的网络进行连接
	public void connectConfiguration(int index) {
		// 索引大于配置好的网络索引返回
		if (index >= mWifiConfiguration.size()) {
			return;
		}
		// 连接配置好的指定ID的网络
		mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId,
				true);
	}
}
