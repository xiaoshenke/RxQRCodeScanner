# RxQRCodeScanner

RxQRCodeScanner is a RxJava version of [ZxingScanner](https://github.com/xiaoshenke/ZxingScanner).It is totally rewrited using RxJava functions.It is very simple to use and integrate to your library.  

##  Demo screenshot

![DemoIcon](https://cl.ly/2f0a0O39202w)
                       

### Usage                     
Step1 add camera permission.In your AndroidManifest.xml          

````
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.VIBRATE" />

````                                     
Step2 In your activity/fragment/view xml file,add SurfaceView and ScanView node.                     

````
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/activity_main"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <SurfaceView
        android:id="@+id/surface"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_gravity="center" />

    <wuxian.me.rxqrcodescanner.view.ScanView
        android:id="@+id/scanview"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_gravity="center" />
</FrameLayout>
````
Step3 create a RxQRCodeScanner variable in your activity,then subscribe a Subscriber<String>,you will receive a string in your call function.        
           
````
RxQRCodeScanner scanner = new RxQRCodeScanner.Builder()
                    .context(this)
                    .surfaceView(surfaceView)
                    .scanView(scanView)
                    .build();

scanner.start().subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                Toast.makeText(MainActivity.this, "qrcode is " + s, Toast.LENGTH_LONG).show();
            }
        });                   
                   
````
the third paramer is the qrcode result callback.

Step4 don't forget to call scanner.quit() in your onPause() method.                         

````
@Override
protected void onPause() {
	super.onPause();
	if (scanner != null) {
		scanner.quit();
		scanner = null;
	}
}
````

wola,now you have successfully integrated QRCode function to your application!  

 
##  Other        
* If you aren't satisfied with the sanner ui,you can implement IScanView to custom your own ui. 


Check the code to know more details !


            






