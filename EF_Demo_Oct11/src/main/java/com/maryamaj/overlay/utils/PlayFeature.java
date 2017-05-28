//package com.maryamaj.overlay.utils;
//
//import java.util.Timer;
//import java.util.TimerTask;
//
//import com.immersion.uhl.Device;
//import com.immersion.uhl.IVTBuffer;
//import com.immersion.uhl.Launcher;
//import com.immersion.uhl.MagSweepEffectDefinition;
//import com.immersion.uhl.internal.EffectHandle;
//import com.immersion.uhl.internal.ImmVibe;
//import com.maryamaj.overlay.applicationmain.Feature;
//
//import android.content.Context;
//import android.os.Handler;
//import android.util.Log;
//
//public final class PlayFeature {
//
//
//	private static boolean isTaskCompleted = false;
//	private static int i_ = 0;
//
//	private static final String TAG="PlayFeature";
//
//
//	public void playHaptic1(Context c) {
//
//		Device dev=Device.newDevice(c);
//
//		System.out.println("---sample--"+ImmVibe.getInstance().getBuiltInEffects());
//
//		IVTBuffer buffer = new IVTBuffer(ImmVibe.getInstance().getBuiltInEffects());
//		dev.playIVTEffect(buffer, buffer.getEffectCount()-1);
//	}
//
//	public static void playHaptic2(Context c)
//	{
//	    Launcher mLauncher;
//	    try {
//	    	mLauncher = new Launcher(c);
//	        mLauncher.play(Launcher.TRIPLE_STRONG_CLICK_100);
//	        PlayFeature.class.wait(3000);
//	        mLauncher.stop();
//	    } catch (Exception e) {
//	        Log.e(TAG, "Failed to play built-in effect, index "+Launcher.TRIPLE_STRONG_CLICK_100 +": "+e);
//	    }
//	}
//
//
//	public static void stopPlay(){
//
//		//TODO
//	}
//
//	public static void playHaptic(Feature t, Context context) {
//
//		//tactile output
//		MagSweepEffectDefinition med = null;
//		EffectHandle effectHandle = null;
//		Feature tFeature=t;
//		i_=0;
//
//
//		med=null;
//		effectHandle=null;
//		Device dev=Device.newDevice(context);
//		int duration = tFeature.getDuration();
//		int[] toPlayTactileValues= tFeature.getValues();
//
//		Log.d("device capability", dev.getCapabilityInt32(ImmVibe.VIBE_DEVCAPTYPE_EDITION_LEVEL)+"");
//		Log.d("max duration", dev.getCapabilityInt32(ImmVibe.VIBE_DEVCAPTYPE_MAX_EFFECT_DURATION )+"");
//
//		if(toPlayTactileValues.length > 0)
//		{
//			try {
//				med=new MagSweepEffectDefinition(duration, 0, ImmVibe.VIBE_STYLE_SMOOTH, 0, ImmVibe.VIBE_MIN_MAGNITUDE, 0,  ImmVibe.VIBE_MIN_MAGNITUDE, 0);//entire time
//				effectHandle = (EffectHandle) dev.playMagSweepEffect(med);
//
//				// Device.getCapabilityInt32 for the ImmVibe.VIBE_DEVCAPTYPE_MAX_EFFECT_DURATION device capability type, inclusive.
//				callAsynchronousTask(toPlayTactileValues,effectHandle, med, duration);
//
//			} catch (Exception e) {
//				System.out.println("---caught exception---");
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	public static void callAsynchronousTask(final int[] values, EffectHandle h,MagSweepEffectDefinition m, int d) {
//
//
//		final EffectHandle handle=h;
//		final MagSweepEffectDefinition med= m;
//		int duration=d;
//
//		TimerTask doAsynchronousTask = null;
//		Timer timer = new Timer();
//		if (isTaskCompleted && timer!=null){
//
//			timer.cancel();
//			isTaskCompleted=false;
//		}
//
//		timer=new Timer();
//
//		final Handler handler = new Handler();
//
//		doAsynchronousTask = new TimerTask() {
//			@Override
//			public void run() {
//				handler.post(new Runnable() {
//					public void run() {
//						if(i_!=values.length)
//						{
//							try {
//								if(handle!=null && handle.isPlaying())
//								{
//									med.setMagnitude((int)values[i_++]);
//									handle.modifyPlayingMagSweepEffect(med);
//								}
//							} catch (Exception e) {
//								Log.w("CALLASYNC", e.getMessage());
//								System.out.println("---in callAsynchronousTask exception---");
//								i_ = 0;
//							}
//						}
//					}
//				});
//				isTaskCompleted = true;
//			}
//
//		};
//
//		timer.schedule(doAsynchronousTask, 0, (int)duration/values.length); //execute in every ms
//	}
//}
