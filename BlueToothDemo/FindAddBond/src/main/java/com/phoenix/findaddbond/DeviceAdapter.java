package com.phoenix.findaddbond;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by flashing on 2016/12/29.
 */
public class DeviceAdapter extends BaseAdapter {
    private List<BluetoothDevice> mData;
    private Context mContext;

    public DeviceAdapter(List<BluetoothDevice> data, Context context) {
        this.mData = data;
        this.mContext = context.getApplicationContext();//取得的Context是和Application关联的，生命周期是应用的创建到销毁
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View itemView = convertView;
        //复用View，优化性能
        if (itemView == null){
            //simple_list_item_2是标题+描述的样式，并且标题字号较大
            itemView = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_2, parent, false);
        }

        TextView line1 = (TextView) itemView.findViewById(android.R.id.text1);
        TextView line2 = (TextView) itemView.findViewById(android.R.id.text2);

        //获取对应的蓝牙设备
        BluetoothDevice device = (BluetoothDevice) getItem(position);

        //显示名称
        line1.setText(device.getName());
        //显示地址
        line2.setText(device.getAddress());
        return itemView;
    }

    public void refresh(List<BluetoothDevice> data){
        mData = data;
        notifyDataSetChanged();
    }
}
