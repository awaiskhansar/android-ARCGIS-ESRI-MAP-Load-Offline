package com.esri.android.maptest;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.esri.arcgisruntime.data.TileCache;
import com.esri.arcgisruntime.geometry.Geometry;
import com.esri.arcgisruntime.geometry.Polygon;
import com.esri.arcgisruntime.internal.jni.CoreBasemap;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Basemap;
import com.esri.arcgisruntime.mapping.MobileMapPackage;
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;

import java.io.File;

import static com.esri.android.maptest.R.id.mapView;

public class MainActivity extends AppCompatActivity implements  OnMapClickListner {

    public MainActivity()
    {}

    private static final String TAG = "MMPK";
    private static final String FILE_EXTENSION = ".mmpk";
    private static final String TPK_FILE_EXTENSION = ".tpk";
    private static File extStorDir;
    private static String extSDCardDirName;
    private static String filename;
    private static String TPKfilename;
    private static String mmpkFilePath;
    private static String tpkFilePath;
    private MapView mMapView;
    private GraphicsOverlay graphicsOverlay ;
    private MobileMapPackage mapPackage;
    ArcGISTiledLayer tiledLayer;
    private ArcGISMap mMap;

    // define permission to request
    String[] reqPermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private int requestCode = 2;
   // GraphicsOverlay graphicsOverlay;
    TileCache tileCache;
    /**
     * Create the mobile map package file location and name structure
     */
    private static String createMobileMapPackageFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + filename + FILE_EXTENSION;
    }
    private static String createTilePackageFilePath(){
        return extStorDir.getAbsolutePath() + File.separator + extSDCardDirName + File.separator + TPKfilename + TPK_FILE_EXTENSION;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loadMap();//Load Esri map
        initializeTouchListner();
    }






    private GraphicsOverlay addGraphicsOverlay(MapView mapView) {
        //create the graphics overlay
        graphicsOverlay = new GraphicsOverlay();
        //add the overlay to the map view
       // mapView.getGraphicsOverlays().add(graphicsOverlay);
        return graphicsOverlay;
    }

    private void initializeTouchListner()
    {
        IdentifyFeatureLayerTouchListener identifyFeatureLayerTouchListener =  new IdentifyFeatureLayerTouchListener(MainActivity.this, mMapView,this);

        mMapView.setOnTouchListener(identifyFeatureLayerTouchListener);
    }

private  void loadMap()
{


    // get sdcard resource name
    extStorDir = Environment.getExternalStorageDirectory();
    // get the directory
    extSDCardDirName = this.getResources().getString(R.string.config_data_sdcard_offline_dir);
    // get mobile map package filename
    filename = this.getResources().getString(R.string.config_mmpk_name);
    TPKfilename=this.getResources().getString(R.string.config_tpk_name);
    // create the full path to the mobile map package file
    mmpkFilePath = createMobileMapPackageFilePath();
    tpkFilePath=createTilePackageFilePath();
     tileCache = new TileCache(tpkFilePath);
    // retrieve the MapView from layout
    mMapView = (MapView) findViewById(mapView);
    graphicsOverlay = addGraphicsOverlay(mMapView);
    mMapView.getGraphicsOverlays().add(graphicsOverlay);

    boolean permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) ==
            PackageManager.PERMISSION_GRANTED;

    if (!permissionCheck) {
        requestPermissions();
        // If permissions are not already granted, request permission from the user.
    } else {
        // if permission was already granted, set up offline map, geocoding and routing functionality
        loadMobileMapPackage(mmpkFilePath);
    }


}
    private void requestPermissions() {

        // For API level 23+ request permission at runtime
        if (ContextCompat.checkSelfPermission(MainActivity.this, reqPermission[0]) == PackageManager.PERMISSION_GRANTED) {
        } else {
            // request permission
            ActivityCompat.requestPermissions(MainActivity.this, reqPermission, requestCode);
        }
    }
    /**
     * Handle the permissions request response
     *
     */
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
           // loadMobileMapPackage(mmpkFilePath);
        }else{
            // report to user that permission was denied
            Toast.makeText(MainActivity.this, getResources().getString(R.string.location_permission_denied),
                    Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * Load a mobile map package into a MapView
     *
     * @param mmpkFile Full path to mmpk file
     */

    private void loadMobileMapPackage(String mmpkFile){
        //[DocRef: Name=Open Mobile Map Package-android, Category=Work with maps, Topic=Create an offline map]
        // create the mobile map package
        mapPackage = new MobileMapPackage(mmpkFile);
        // load the mobile map package asynchronously
       // mapPackage.loadAsync();


        mapPackage.addDoneLoadingListener(new Runnable() {

            @Override
            public void run() {

                if (mapPackage.getLoadStatus() != LoadStatus.LOADED) {
                   // Snackbar.make(mMapView, String.format(getString(R.string.object_not_loaded), "MMPK"),
                          //  Snackbar.LENGTH_SHORT).show();
                    return;
                }

                // Get the first map.
                if (mapPackage.getMaps().size() == 0) {
                   // Snackbar.make(mMapView, String.format(getString(R.string.no_maps_in_mmpk), mmpkPath),
                            //Snackbar.LENGTH_SHORT).show();
                    return;
                }

                mMap = mapPackage.getMaps().get(0);
               // mMap = mapPackage.getMaps().get(0);
                tpkFilePath=createTilePackageFilePath();
                TileCache tileCache1 = new TileCache(tpkFilePath);
                tiledLayer = new ArcGISTiledLayer(tileCache1);
//                mMap.setBasemap(new Basemap(tiledLayer));

                mMap.setBasemap(Basemap.createStreets());
                // Set a Basemap from a raster tile cache package
//                String tpkPath = getTpkPath();
//                if (TextUtils.isEmpty(tpkPath)) {
//                    return;
//                }
                //TileCache tileCache = new TileCache(getTpkPath());
               // final ArcGISTiledLayer tiledLayer = new ArcGISTiledLayer(tileCache); TileCache tileCache = new TileCache(getTpkPath());
                //mMap.setBasemap(new Basemap(tiledLayer));

                // No need to explicitly load the map, just set it into the MapView; that will trigger loading when displayed.
                mMapView.setMap(mMap);
                mMap.addDoneLoadingListener(new Runnable() {
                    @Override
                    public void run() {

                        if (mapPackage.getLoadStatus() != LoadStatus.LOADED) {
                           // Snackbar.make(mMapView, String.format(getString(R.string.object_not_loaded), "Map"),
                                   // Snackbar.LENGTH_SHORT).show();
                            return;
                        }

//                        mMapView.setViewpointGeometryAsync(tiledLayer.getFullExtent());
                        mMapView.refreshDrawableState();


                        mMap.removeDoneLoadingListener(this);
                    }
                });
            }
        });

        // Load the MMPK.
        mapPackage.loadAsync();


        // add done listener which will invoke when mobile map package has loaded
//        mapPackage.addDoneLoadingListener(new Runnable() {
//            @Override
//            public void run() {
//                // check load status and that the mobile map package has maps
//                if(mapPackage.getLoadStatus() == LoadStatus.LOADED && mapPackage.getMaps().size() > 0){
//                    // add the map from the mobile map package to the MapView
//                    //mMapView.setMap(mapPackage.getMaps().get(0));
//
//                   loadMobileTilePackage(tileCache);
//
//                }else{
//                    // Log an issue if the mobile map package fails to load
//                    Log.e(TAG, mapPackage.getLoadError().getMessage());
//                }
//            }
//        });
        //[DocRef: END]
    }

    @Override
    protected void onPause(){
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        mMapView.resume();
    }

    @Override
    public void drawOnMap(Geometry geometry) {
        SimpleLineSymbol outlineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.DASH, Color.rgb(0, 0, 128), 1);
        SimpleFillSymbol fillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.DIAGONAL_CROSS, Color.rgb(0, 80, 0), outlineSymbol);
        Graphic nestingGraphic = new Graphic(geometry,fillSymbol);
        //add to graphics overlay
        graphicsOverlay.getGraphics().add(nestingGraphic);
    }
}

