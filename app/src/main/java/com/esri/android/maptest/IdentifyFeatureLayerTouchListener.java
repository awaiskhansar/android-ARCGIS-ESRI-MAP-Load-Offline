package com.esri.android.maptest;


import android.content.Context;
import android.view.MotionEvent;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureTable;
import com.esri.arcgisruntime.layers.LayerContent;
import com.esri.arcgisruntime.mapping.GeoElement;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.IdentifyLayerResult;
import com.esri.arcgisruntime.mapping.view.MapView;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by AHussain on 5/8/2017.
 */

public class IdentifyFeatureLayerTouchListener extends DefaultMapViewOnTouchListener {

    OnMapClickListner onMapClickListner;
    public IdentifyFeatureLayerTouchListener(Context context, MapView mapView,OnMapClickListner onMapClickListner) {
        super(context, mapView);
        this.onMapClickListner=onMapClickListner;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        // get the screen point where user tapped
        android.graphics.Point screenPoint = new android.graphics.Point((int) e.getX(), (int) e.getY());
        // ...
        final ListenableFuture<List<IdentifyLayerResult>> identifyFuture = mMapView.identifyLayersAsync(screenPoint, 5,
                false);

// add a listener to the future
        identifyFuture.addDoneListener(new Runnable() {


            @Override
            public void run() {
                try {
                    // get the identify results from the future - returns when the operation is complete
                    List<IdentifyLayerResult> identifyLayersResults = identifyFuture.get();

                    // iterate all the layers in the identify result
                    for (IdentifyLayerResult identifyLayerResult : identifyLayersResults) {

                        // each identified layer should find only one or zero results, when identifying topmost GeoElement only
                        if (identifyLayerResult.getElements().size() > 0) {
                            GeoElement topmostElement = identifyLayerResult.getElements().get(0);
                            if (topmostElement instanceof Feature) {
                                Feature identifiedFeature = (Feature)topmostElement;

                                // Use feature as required, for example access attributes or geometry, select, build a table, etc...
                                processIdentifyFeatureResult(identifiedFeature, identifyLayerResult.getLayerContent());
                            }
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    //dealWithException(ex); // must deal with exceptions thrown from the async identify operation
                }
            }
        });
        return true;
    }

    private void processIdentifyFeatureResult(Feature identifiedFeature, LayerContent layerContent) {

        FeatureTable featureTable= identifiedFeature.getFeatureTable();
        Map<String, Object> objectMap = identifiedFeature.getAttributes();
//        MainActivity mainActivity = new MainActivity();
       String obj= objectMap.get("ID").toString();

        if(onMapClickListner!=null)
        {
            onMapClickListner.drawOnMap(identifiedFeature.getGeometry());

        }
//        mainActivity.addPolygonGraphic(identifiedFeature.getGeometry());
        String test="";
    }
}
