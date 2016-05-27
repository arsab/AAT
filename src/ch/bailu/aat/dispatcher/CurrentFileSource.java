package ch.bailu.aat.dispatcher;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import ch.bailu.aat.helpers.AppBroadcaster;
import ch.bailu.aat.services.ServiceContext;

public class CurrentFileSource extends ContentSource {

    private final ServiceContext scontext;


    private BroadcastReceiver onFileProcessed = new BroadcastReceiver () {
        @Override
        public void onReceive(Context context, Intent intent) {
            File file = new File(scontext.getDirectoryService().getCurrent().getPath());

            if (AppBroadcaster.hasFile(intent, file.getAbsolutePath())) {
                forceUpdate();
            }
        }

    };

    public CurrentFileSource (ServiceContext sc) {
        scontext = sc;
        AppBroadcaster.register(sc.getContext(), onFileProcessed, AppBroadcaster.FILE_CHANGED_INCACHE);
    }


    @Override
    public void close() {
        scontext.getContext().unregisterReceiver(onFileProcessed);
    }

    @Override
    public void forceUpdate() {
        updateGpxContent(scontext.getDirectoryService().getCurrent());
    }
}
