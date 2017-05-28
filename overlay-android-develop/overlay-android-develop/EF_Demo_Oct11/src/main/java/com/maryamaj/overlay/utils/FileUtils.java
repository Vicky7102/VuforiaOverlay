package com.maryamaj.overlay.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

//import org.opencv.core.Mat;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

@SuppressLint("SimpleDateFormat")
public class FileUtils {

	private String 					pID;
	private File 					file, imgFile, matFile;
	private static final String  	TAG= "FileUtils";
	private boolean success;



	public FileUtils(){

		long yourmilliseconds = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");    
		Date resultdate = new Date(yourmilliseconds);
		pID=sdf.format(resultdate);
		String fileName=pID;

		//		imgFile =new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ubitile/"+pID+"/");
		imgFile =new File(Environment.getExternalStorageDirectory().getAbsolutePath());
		matFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());



		file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ubitile/"+fileName+".txt");

		while (file.exists()){

			fileName=fileName+"-copy";
			file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Ubitile/"+fileName+".txt");
		}
	}


	public String getpID(){
		return pID;
	}

	public void writeToFile(Bitmap bmp, String name){

		String fileName=pID+"_"+name;
		FileOutputStream fOut=null;

		try {
			fOut = new FileOutputStream(this.imgFile.getAbsolutePath()+"/Ubitile/"+fileName+".png", false);   
			success =bmp.compress(Bitmap.CompressFormat.PNG, 100, fOut); // bmp is your Bitmap instance
			// PNG is a lossless format, the compression factor (100) is ignored
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (fOut != null) {
					fOut.flush();
					fOut.close();
					Log.d(TAG, success+"");    
				}
			} catch (IOException e) {
				Log.e(TAG, "could not write bitmap to file");
				e.printStackTrace();
			}
		}	
	}

	public static void moveFile(String inputPath, String outputPath) {
		InputStream in;
		OutputStream out;
		try {

			//create output directory if it doesn't exist
			File outputFile = new File (outputPath);
            if(outputFile.getParentFile().mkdirs() || outputFile.createNewFile())
                Log.d(TAG, "New file created at: " + outputPath);
			in = new FileInputStream(inputPath);
			out = new FileOutputStream(outputPath);

			byte[] buffer = new byte[1024];
			int read;
			while ((read = in.read(buffer)) != -1) {
				out.write(buffer, 0, read);
			}
			in.close();
			// write the output file
			out.flush();
			out.close();
//			 delete the original file
			if(!new File(inputPath).delete()) {
				Log.w(TAG, "Could not delete input file at: " + inputPath);
			}
		}

		catch (FileNotFoundException e) {
			Log.e(TAG, "File not found", e);
		}
		catch (Exception e) {
			Log.e(TAG, "File move exception", e);
		}

	}

    public static void downloadFileFromURL(String url, String downloadTo) {
            new AsyncDownloader().execute(url, downloadTo);
    }

    private static void downloadFile(String remoteUrl, String downloadTo) {
        int count;
        try {
            URL url = new URL(remoteUrl);
            URLConnection connection = url.openConnection();
            connection.connect();
//            int lenghtOfFile = connection.getContentLength();
            InputStream input = new BufferedInputStream(url.openStream(),
                    8192);
            File outputFile = new File(downloadTo);
            if(outputFile.getParentFile().mkdirs() || outputFile.createNewFile()) {
                Log.d(TAG, "New file created: " + downloadTo);
            }
            OutputStream output = new FileOutputStream(outputFile);
            byte data[] = new byte[1024];
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e(TAG, "File download error", e);
        }
    }

    private static class AsyncDownloader extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... args) {
            downloadFile(args[0], args[1]);
            return null;
        }
    }

//	public void matToTxt(Mat m, String name){
//
//		String s="";
//		FileOutputStream fOut=null;
//		Mat input=m;
//
//		try{
//			String fileName=pID+"_"+name;
//			fOut = new FileOutputStream(this.matFile.getAbsolutePath()+"/Ubitile/"+fileName+".txt", false);  
//			@SuppressWarnings("resource")
//			OutputStreamWriter fileWriter = new OutputStreamWriter(fOut);
//			for(int i=0;i<input.rows();i++){
//				for (int j=0;j<input.cols();j++){
//					s=Arrays.toString(input.get(i,j));
//					fileWriter.append(s+",");
//				}
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (fOut != null) {
//					fOut.flush();
//					fOut.close();
//					Log.d(TAG, success+"");    
//				}
//
//			}catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	public void writeToFile(String s){
		try {

			FileOutputStream fOut = new FileOutputStream(this.file.getAbsolutePath(), true);			
			OutputStreamWriter fileWriter = new OutputStreamWriter(fOut);
			fileWriter.append(s);
			fileWriter.close();
			fOut.close();
		} 

		catch (IOException e) {
			e.printStackTrace();
		}
	}

}

