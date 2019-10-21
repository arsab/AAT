package ch.bailu.aat.services.sensor;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.gpx.InfoID;
import ch.bailu.aat.services.ServiceContext;
import ch.bailu.aat.services.VirtualService;
import ch.bailu.aat.services.sensor.list.SensorList;
import ch.bailu.aat.util.AppBroadcaster;

public class SensorService extends VirtualService {
    private final SensorList sensorList;

    private final Sensors bluetoothLE;
    private final Sensors internal;


    public SensorService(ServiceContext sc) {
        super(sc);

        sensorList = new SensorList(sc.getContext());
        bluetoothLE = Sensors.factoryBle(sc, sensorList);
        internal = Sensors.factoryInternal(sc.getContext(), sensorList);


        AppBroadcaster.register(getContext(), onBluetoothStateChanged, BluetoothAdapter.ACTION_STATE_CHANGED);

        AppBroadcaster.register(getContext(), onSensorDisconected, AppBroadcaster.SENSOR_DISCONECTED + InfoID.SENSORS);

        AppBroadcaster.register(getContext(), onSensorReconnect, AppBroadcaster.SENSOR_RECONNECT + InfoID.SENSORS);

        updateConnections();
    }


    public static boolean isSupported() {
        return (Build.VERSION.SDK_INT >= 18);
    }


    final BroadcastReceiver onBluetoothStateChanged = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

            if (state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF) {
                updateConnections();
            }
        }


    };


    final BroadcastReceiver onSensorDisconected = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnections();
        }
    };


    final BroadcastReceiver onSensorReconnect = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateConnections();
            scann();                        // rescan to get them in cache if they were not
        }
    };


    @Override
    public void appendStatusText(StringBuilder builder) {
        builder.append(toString());
    }


    @Override
    public synchronized void close() {
        bluetoothLE.close();
        internal.close();
        sensorList.close();
        getContext().unregisterReceiver(onBluetoothStateChanged);
        getContext().unregisterReceiver(onSensorDisconected);
        getContext().unregisterReceiver(onSensorReconnect);
    }

    public synchronized void updateConnections() {
        bluetoothLE.updateConnections();
        internal.updateConnections();
        sensorList.broadcast();
    }

    public  synchronized void scann() {
        bluetoothLE.scann();
    }


    @NonNull
    @Override
    public synchronized String toString() {
        return bluetoothLE.toString();

    }

    public synchronized GpxInformation getInformation(int iid) {
        GpxInformation information = getInformationOrNull(iid);


        if (information == null) {
            information = GpxInformation.NULL;
        }

        return information;
    }


    public synchronized GpxInformation getInformationOrNull(int iid) {
        return sensorList.getInformation(iid);
    }


    public SensorList getSensorList() {
        return sensorList;
    }
}
