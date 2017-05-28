//package com.maryamaj.overlay.utils;
//
//
////Time logs
//
////Trial count, start time, stop time, end time
//// block name, start time, end time
////task name, start time, end time
////experiment: pid, start time, end time, calibtime
//
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.IOException;
//import java.io.OutputStreamWriter;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.Random;
//import java.util.Timer;
//import java.util.TimerTask;
//import java.util.concurrent.ThreadLocalRandom;
//
//import android.annotation.SuppressLint;
//import android.app.ProgressDialog;
//import android.content.Context;
//import android.os.Environment;
//import android.os.Handler;
//import android.util.Log;
//
//import com.immersion.uhl.Device;
//import com.immersion.uhl.IVTBuffer;
//import com.immersion.uhl.MagSweepEffectDefinition;
//import com.immersion.uhl.internal.EffectHandle;
//import com.immersion.uhl.internal.ImmVibe;
//
//@SuppressLint("Instantiatable")
//public class // ClobalClassTactile  {
//
//	//experiment
//	private long xStart=0;
//	private String pID;
//	private long calStart=0,calTime=0;
//
//	//tasks
////	private static int TASK_SETS=6;
//	private String [] taskset1={"darkness", "rectangle", "hardness", "wet-ness", "dust-storm","deliberate-ness", "joy", "flying"};
//	private String [] taskset2={"bumpiness", "unevenness(surface)","curve", "screaming", "heartbeat", "hope","wind","disgust" };
//	private String [] taskset3={"constraint","amazement", "fear","spiciness(taste)","snow","volume(3D-nesss)", "spongy-ness (material)","roughness"};
//	private String [] taskset4={"straight-ness (level)", "river","liquid","lifting","glossy-ness(surface)","anticipation","siren(sound)","informality"};
//	private String [] taskset5={"bounciness", "happiness","chasing", "slope", "density","belong", "energy","salty(taste)"};
//	private String [] taskset6={"leather(scent)","dry-ness", "flatness","randomness","flickering", "volcano", "lightweight", "trust"};
////	private String [] taskset7={"leather(scent)","dry-ness"};
//	private String [][] taskSets={taskset1,taskset2,taskset3,taskset4,taskset5,taskset6};
//
//
//	private String TAG="Global Class";
//
//	private String[] currentTaskSet;
//	private int setID;//can be 1, 2, 3 or 4 find details above
//	//	private String [] tasks;
//	//	private int NO_OF_TASKS= 8;
//	private int taskCounter=1;//1-8
//	private long kStart=0;
//	private int ease=99;
//
//
//	//blocks
//	private int [] blocksArray={1,2,3};//block order: auditory, gestural, touchscreen
//	private long bStartAuditory=0,bStartVisual=0,bStartGestural=0,bStartScreen=0;
//	private int blkCounter=0;
//	private String currentBlk ="";
//
//	//trials
//	private long tStart=0, tStop=0;
//	private long nStart = 0, nEvent=0; // time it takes to think
//
//	//tactile output
//	private int [] toPlayTactileValues;
//	private int duration;
//	private int i_ = 0;
//	private TimerTask doAsynchronousTask = null;
//	private boolean isTaskCompleted = false;
//	private MagSweepEffectDefinition med = null;
//	private EffectHandle effectHandle = null;
//	Timer timer = new Timer();
//	ProgressDialog dialog;
//
//
//	//file
//	public File file;
//	//	String tactileValuesOut="";
//	String tactileValuesRaw="";
//	String tactileValuesIn="";
//	String tactileValues3DMax="";
//
//
//
//	public static final byte[] ivt =
//		{
//		(byte)0x01, (byte)0x00, (byte)0x03, (byte)0x00, (byte)0x34, (byte)0x00, (byte)0x00, (byte)0x00,
//		(byte)0x00, (byte)0x00, (byte)0x0d, (byte)0x00, (byte)0x1d, (byte)0x00, (byte)0xf1, (byte)0xe0,
//		(byte)0x02, (byte)0xe2, (byte)0x00, (byte)0x00, (byte)0xf1, (byte)0xe0, (byte)0x01, (byte)0xe2,
//		(byte)0x05, (byte)0x8c, (byte)0xff, (byte)0x30, (byte)0xc8, (byte)0x00, (byte)0xc8, (byte)0x00,
//		(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x3e, (byte)0x00, (byte)0x00, (byte)0x5f,
//		(byte)0x81, (byte)0x3e, (byte)0x81, (byte)0x30, (byte)0xc8, (byte)0x00, (byte)0xc8, (byte)0x00,
//		(byte)0x00, (byte)0x00, (byte)0x00, (byte)0x20, (byte)0x3e, (byte)0x00, (byte)0x00, (byte)0x5f,
//		(byte)0x00, (byte)0x00, (byte)0x81, (byte)0x00
//		};
//
//
//
//	public // ClobalClassTactile(String id){
//
//		pID=id;
//		String s="";
//		//		int i =0;
//		//		tasks = new String[NO_OF_TASKS];
//		//		while ( i<NO_OF_TASKS)
//		//		tasks[i++]="task"+i;
//
//		String fileName=pID;
//
//
//		file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+fileName+".txt");
//		while (file.exists()){
//			fileName=fileName+"-copy";
//			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+fileName+".txt");
//		}
//
//		 setID = Integer.parseInt(pID)%taskSets.length;
//		 Log.d(TAG,"set id is:"+setID );
//
//		//		for (int i=0;i<taskSets.length; i++)
//		//			if(taskSetName.equalsIgnoreCase(taskSets[i].toString()))
//		currentTaskSet= shuffleArray(taskSets[setID]);
//		for (int i=0;i<currentTaskSet.length;i++)
//			s+=currentTaskSet[i]+",";
//
//		long yourmilliseconds = System.currentTimeMillis();
//		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
//		Date resultdate = new Date(yourmilliseconds);
//		//		System.out.println(sdf.format(resultdate));
//
//
//		writeToFile(pID+","+sdf.format(resultdate)+","+s+"\n");
//
////		getNextBlock();
//
//	}
//	public void shuffleBlocksArray(){
//		blocksArray = shuffleArray(blocksArray);
//	}
//
//	public void setExpStartTime(){
//
//		xStart= System.currentTimeMillis();
//	}
//
//	public void setExpEvent(){//experiment: pid, taskset, full experiment time
//		writeToFile("Experiment,"+pID+","+"taskSet"+setID+","+calTime+","+(System.currentTimeMillis()-xStart)+ "\n");
//	}
//
//	public void setCalibStart(){
//		calStart=System.currentTimeMillis();
//	}
//
//	public void setCalibTime(){
//		calTime= System.currentTimeMillis()-calStart;
//	}
//
//	public void setTaskStartTime(){
//
//		kStart = System.currentTimeMillis();
//	}
//
//	public void setEOE(int ease){
//		this.ease=ease;
//	}
//
//	public void setTaskEvent(int pref){//current task, full task time
//		writeToFile("Task,"+currentTaskSet[taskCounter-1]+","+(System.currentTimeMillis()-kStart)+","+ease+","+pref+"\n");
//		//		Log.d("current task", taskCounter+"");
//	}
//
//	public void setBlockStartTime(String s){//set bStart
//		currentBlk=s;
//
//		if (s.equalsIgnoreCase("auditory")){
//			bStartAuditory=System.currentTimeMillis();
//		}
//		else if (s.equalsIgnoreCase("visual")){
//			bStartVisual=System.currentTimeMillis();
//		}
//		else if (s.equalsIgnoreCase("gestural")){
//			bStartGestural=System.currentTimeMillis();
//		}
//		else if (s.equalsIgnoreCase("touch")){
//			bStartScreen=System.currentTimeMillis();
//		}
//	}
//
//	public void setBlockEvent(String s){ //block_name, startTime, endTime
//		long thisBlock=0;
//		if (s.equalsIgnoreCase("gestural")){
//			thisBlock=bStartGestural;
//		}
//		else if (s.equalsIgnoreCase("visual")){
//			thisBlock=bStartVisual;
//		}
//		else if (s.equalsIgnoreCase("auditory")){
//			thisBlock=bStartAuditory;
//
//		}
//		else if (s.equalsIgnoreCase("touch")){
//			thisBlock=bStartScreen;
//		}
//		writeToFile(s+","+(System.currentTimeMillis()-thisBlock) +"\n");
//	}
//
//	public void setTrialStartTime(){
//		tStart = System.currentTimeMillis();
//	}
//
//	public void setTrialStopTime(){
//		tStop = System.currentTimeMillis();
//	}
//
//	public void setTrialEvent(String tCount){// stop-start time and approximation of how many time the effect was played back until approved or retried
//
//
//		//"Trial,trialCount,thinktime,trialinputtime, trialtime
//		// rawvalues, inputvalues
//		writeToFile ("Trial"+ ","+(tCount)+","+nEvent+","+ (tStop-tStart)+","+(System.currentTimeMillis()-tStop)+ "\n"+ tactileValuesRaw+ "\n"+tactileValuesIn +"\n");// duration determined at playhaptic
//
//		//gesture axis values
//		if (currentBlk.equalsIgnoreCase("gestural"))
//			writeToFile(tactileValues3DMax+"\n");
//		//duration
//		writeToFile(duration+"\n");
//	}
//
//	public void setThinkStartTime(){
//		nStart = System.currentTimeMillis();
//		Log.d("nstart", nStart+"");
//	}
//
//	public void setThinkEvent(){
//		nEvent = (System.currentTimeMillis()-nStart);
//		Log.d("nstart,nevent", nStart+","+nEvent);
//
//	}
//
//	public void setRawValues(String s){
//		tactileValuesRaw= s;
//	}
//
//	public void setInValues(String s){
//		tactileValuesIn=s;
//	}
//
//	public void set3DMaxIndices(String s){
//		tactileValues3DMax=s;
//	}
//
//	public void setReplay() {
//		writeToFile("replay"+"\n");
//	}
//
//	public int getCurrentTaskNo(){
//		return taskCounter;
//	}
//
//	public String getCurrentTaskName(){
//		return currentTaskSet[taskCounter-1];
//	}
//
//	public void getNextTask(){
//
//		shuffleBlocksArray();
//		if (taskCounter<currentTaskSet.length){
//			taskCounter++;
//		}
//	}
//
//	public int getNoOfTasks(){
//		return currentTaskSet.length;
//	}
//
//	public int getblockArraySize(){
//		return blocksArray.length;
//	}
//	public int getCurrentBlk(){
//		return blkCounter-1;
//	}
//
//	public int getNextBlock(){
//		int next =blocksArray[blkCounter % blocksArray.length];
//		blkCounter++;
//		return next;
//	}
//
//	public String getpID(){
//		return pID;
//	}
//
//	public void writeToFile(String s){
//		try {
//
//			FileOutputStream fOut = new FileOutputStream(this.file.getAbsolutePath(), true);
//			OutputStreamWriter fileWriter = new OutputStreamWriter(fOut);
//			fileWriter.append(s);
//			fileWriter.close();
//			fOut.close();
//		}
//
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public void showPlaying(Context context)
//	{
//		dialog = ProgressDialog.show(context, "",
//				"Playing. Please wait...", true);
//	}
//
//	public void stopShowPlayProgress(Context context)
//	{
//		if (dialog !=null && dialog.isShowing()) {
//			dialog.cancel();
//		}
//	}
//
//	public void playhaptic(Context context) {
//
//		Device dev=Device.newDevice(context);
//
//		System.out.println("---sample--"+ImmVibe.getInstance().getBuiltInEffects());
//
//		IVTBuffer buffer = new IVTBuffer(ImmVibe.getInstance().getBuiltInEffects());
//		dev.playIVTEffect(buffer, buffer.getEffectCount()-1);
//	}
//
//	public void callAsynchronousTask(final int[] values) {
//
//		if (isTaskCompleted){
//			timer.cancel();
//
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
//								if(effectHandle!=null && effectHandle.isPlaying())
//								{
//									med.setMagnitude((int)values[i_++]);
//									effectHandle.modifyPlayingMagSweepEffect(med);
//									//									tactileValuesOut+=med.getMagnitude()+",";
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
//		//	    void java.util.Timer.schedule(TimerTask task, long delay, long period)
//		timer.schedule(doAsynchronousTask, 0, (int)duration/values.length); //execute in every ms
//	}
//
//	@SuppressLint("NewApi")
//	public void playhaptic(int[] valuesInt,int time, Context context) {
//
//		i_ = 0;
//		doAsynchronousTask = null;
//		med=null;
//		effectHandle=null;
//		Device dev=Device.newDevice(context);
//		duration = time;
//		toPlayTactileValues= valuesInt;
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
//				callAsynchronousTask(toPlayTactileValues);
//
//			} catch (Exception e) {
//				System.out.println("---caught exception---");
//				e.printStackTrace();
//			}
//		}
//
//	}
//
//	@SuppressLint("NewApi")
//	public int[] shuffleArray(int[] array){
//
//		Random rnd = ThreadLocalRandom.current();
//		for (int i = array.length - 1; i > 0; i--)
//		{
//			int index = rnd.nextInt(i + 1);
//			// Simple swap
//			int a = array[index];
//			array[index] = array[i];
//			array[i] = a;
//		}
//		return array;
//	}
//
//	@SuppressLint("NewApi")
//	public String[] shuffleArray(String[] array){
//
//		Random rnd = ThreadLocalRandom.current();
//		for (int i = array.length - 1; i > 0; i--)
//		{
//			int index = rnd.nextInt(i + 1);
//			// Simple swap
//			String a = array[index];
//			array[index] = array[i];
//			array[i] = a;
//		}
//		return array;
//	}
//
//}
