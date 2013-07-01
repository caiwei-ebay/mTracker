package com.ebay.build.profiler.utils;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	public static final String DONE_EXT = ".done";
	public static final String XML_EXT = ".xml";

	public static String readFile(File file) {
		BufferedReader br = null;
		DataInputStream in = null;
		StringBuffer sBuffer = new StringBuffer();

		try {
			in = new DataInputStream(new FileInputStream(file));
			br = new BufferedReader(new InputStreamReader(in));

			String sCurrentLine = null;

			while ((sCurrentLine = br.readLine()) != null) {
				sBuffer.append(sCurrentLine);
				sBuffer.append("\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return sBuffer.toString();
	}
	
	public static void diskClean(File targetFolder, int retensionDays) {
		File[] doneFiles = loadDoneFiles(targetFolder);
		List<File> filesToDelete = new ArrayList<File>();
		for (File file : doneFiles) {
			long diff = System.currentTimeMillis() - file.lastModified();
			long interval = retensionDays * 24 * 60 * 60 * 1000;
			
			if (diff > interval) {
				filesToDelete.add(file);
			}
		}
		
		System.out.println("[INFO] Cleaning up " + filesToDelete.size() + " DONE files older than " + retensionDays + " days in target folder: " + targetFolder);
		for (File file : filesToDelete) {
			file.delete();
		}
	}
	
	public static File[] loadDoneFiles(File targetFolder) {
		return loadFiles(targetFolder, DONE_EXT);
	}
	
	public static File[] loadFiles(final File targetFolder, final String ext) {
		return targetFolder.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(ext);
			}
		});
		
	}
	
	public static void renameDoneFile(File file) {
		if (!file.exists()) {
			return;
		}
		File dest = new File(file.getParent(), file.getName() + DONE_EXT);
		boolean success = file.renameTo(dest);
		if (success) {
			System.out.println("[INFO] Rename Session LOG " + dest);
		} else {
			System.out.println("[WARNING] Failed rename session LOG to " + dest);
		}
	}
}