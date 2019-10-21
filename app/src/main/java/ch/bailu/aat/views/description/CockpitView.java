package ch.bailu.aat.views.description;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import ch.bailu.aat.description.AltitudeConfigurationDescription;
import ch.bailu.aat.description.CadenceDescription;
import ch.bailu.aat.description.ContentDescription;
import ch.bailu.aat.description.HeartRateDescription;
import ch.bailu.aat.dispatcher.DispatcherInterface;
import ch.bailu.aat.gpx.InfoID;
import ch.bailu.aat.preferences.location.SolidProvideAltitude;
import ch.bailu.aat.util.AppBroadcaster;
import ch.bailu.aat.util.ToDo;
import ch.bailu.aat.util.ui.AppLog;
import ch.bailu.aat.util.ui.AppTheme;


public class CockpitView extends ViewGroup {

    private final Layouter layouter = new Layouter();


    public CockpitView(Context context) {
        super(context);
    }

/*
    public void addAll(DispatcherInterface di, ContentDescription... des) {
        for (ContentDescription de : des) {
            add(di, de);
        }
    }
*/
    public void add(DispatcherInterface di, ContentDescription de) {
        add(di, de, InfoID.TRACKER);
    }


    public void addC(DispatcherInterface di, ContentDescription de, int... iid) {
        final NumberView v = new ColorNumberView(de, AppTheme.main);

        addView(v);
        di.addTarget(v, iid);
    }


    public NumberView add(DispatcherInterface di, ContentDescription de, int... iid) {
        final NumberView v = new NumberView(de, AppTheme.main);

        addView(v);
        di.addTarget(v, iid);
        return v;
    }



    public void addAltitude(DispatcherInterface di) {
        NumberView v = add(di, new AltitudeConfigurationDescription(getContext()), InfoID.LOCATION);
        SolidProvideAltitude.requestOnClick(v);
    }

    public void addHeartRate(DispatcherInterface di) {
        NumberView v = add(di, new HeartRateDescription(getContext()), InfoID.HEART_RATE_SENSOR);
        v.requestOnClickSensorReconect();
    }


    public void addCadence(DispatcherInterface di) {
        NumberView v = add(di, new CadenceDescription(getContext()), InfoID.CADENCE_SENSOR);
        v.requestOnClickSensorReconect();
    }



    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            layouter.layout(r-l,b-t);
        }

    }

    private class Layouter {
        private static final int MAX_CHARS_PER_LINE=50;
        private int parent_width=0;
        private int parent_height=0;

        private final Placer placer = new Placer();
        private final Placer realPlacer = new RealPlacer();


        public void layout(int w, int h) {
            int chars = 1;

            parent_width = w;
            parent_height = h;

            while (chars < MAX_CHARS_PER_LINE) {
                if (tryPlacement(chars)) {
                    doPlacement(chars);
                    break;
                } else chars++;
            }
        }




        private boolean tryPlacement(int charsPerLine) {
            placer.place(charsPerLine);
            return placeItems(placer);
        }

        private boolean doPlacement(int charsPerLine) {
            realPlacer.place(charsPerLine);
            return placeItems(realPlacer);
        }

        private boolean placeItems(Placer p) {
            boolean works=true;

            final int size = getChildCount();
            for (int i=0; i< size && works; i++) {
                works = p.placeItem(i);
            }
            return works;
        }

        private class Placer {
            private static final int MIN_CHARS = 4;
            private static final int RATIO=3;
            private int char_width, char_height, xpos, ypos;


            public void place(int charsPerLine) {
                xpos=ypos=0;
                calculateCharGeometry(charsPerLine);
            }

            private void calculateCharGeometry(int charsPerLine) {
                char_width=parent_width/charsPerLine;

                char_height=char_width*RATIO;
                if (char_height > parent_height) {
                    char_height=parent_height;
                    char_width=char_height/RATIO;
                }
            }


            public boolean placeItem(int index) {
                boolean works=true;
                int width=getWidthOfView(index);

                if (width>parent_width) {
                    works=false;
                } else if (width+xpos > parent_width) {
                    works=addLine();
                    if (works) works = placeItem(index);
                } else {
                    setGeometry(index, width, char_height);
                    xpos += width;
                }
                return works;
            }

            protected void setGeometry(int index, int width, int height) {}

            protected int getXPos() {return xpos;}
            protected int getYPos() {return ypos;}

            private boolean addLine() {
                ypos+=char_height;
                xpos=0;
                return ((char_height + ypos) <= parent_height);
            }


            private int getWidthOfView(int index) {
                final NumberView child = (NumberView) getChildAt(index);

                int len= child.getDescription().getValue().length();
                len = Math.max(len, MIN_CHARS);
                return len*char_width;
            }
        }


        private class RealPlacer extends Placer {


            @Override
            protected void setGeometry(int index, int width, int height) {
                final View child = getChildAt(index);

                child.layout(getXPos(),getYPos(), getXPos()+width, getYPos()+height);
            }
        }

    }


}
