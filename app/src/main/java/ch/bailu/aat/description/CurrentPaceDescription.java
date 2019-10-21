package ch.bailu.aat.description;

import android.content.Context;

import ch.bailu.aat.gpx.GpxInformation;
import ch.bailu.aat.gpx.GpxList;
import ch.bailu.aat.gpx.interfaces.GpxDeltaInterface;
import ch.bailu.aat.util.ToDo;

public class CurrentPaceDescription extends PaceDescription {
    public CurrentPaceDescription(Context c) {
        super(c);
    }

    @Override
    public String getLabel() {
        return ToDo.translate("Current Pace");
    }


    @Override
    public void onContentUpdated(int iid, GpxInformation info) {
        if (setPaceFromLastPoint(info) == false) {
            setCache(info.getSpeed());
        }
    }


    private boolean setPaceFromLastPoint(GpxInformation info) {
        final GpxList track = info.getGpxList();

        if (track != null) {
            if (track.getPointList().size() > 0) {
                final GpxDeltaInterface delta = ((GpxDeltaInterface) info.getGpxList().getPointList().getLast());
                if (delta != null) {
                    setCache(speedToPace(delta.getSpeed()));
                    return true;
                }
            }

        }
        return false;
    }
}
