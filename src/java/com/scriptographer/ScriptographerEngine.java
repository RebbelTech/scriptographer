/*
 * Scriptographer
 *
 * This file is part of Scriptographer, a Plugin for Adobe Illustrator.
 *
 * Copyright (c) 2002-2007 Juerg Lehni, http://www.scratchdisk.com.
 * All rights reserved.
 *
 * Please visit http://scriptographer.com/ for updates and contact.
 *
 * -- GPL LICENSE NOTICE --
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -- GPL LICENSE NOTICE --
 *
 * File created on 04.12.2004.
 *
 * $Id$
 */

package com.scriptographer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.prefs.Preferences;

import com.scriptographer.adm.Dialog;
import com.scriptographer.adm.MenuItem;
import com.scriptographer.ai.Annotator;
import com.scriptographer.ai.Document;
import com.scriptographer.ai.LiveEffect;
import com.scriptographer.ai.Timer;
import com.scratchdisk.script.Script;
import com.scratchdisk.script.ScriptCanceledException;
import com.scratchdisk.script.ScriptEngine;
import com.scratchdisk.script.ScriptException;
import com.scratchdisk.script.Callable;
import com.scratchdisk.script.Scope;

/**
 * @author lehni
 */
public class ScriptographerEngine {
	private static File scriptDir = null;
	private static File pluginDir = null;
	private static PrintStream logger = null;

	/**
     * Don't let anyone instantiate this class.
     */
    private ScriptographerEngine() {
	}

	public static void init(String javaPath) throws Exception {
		// Redirect system streams to the console.
		ConsoleOutputStream.enableRedirection(true);

		logger = new PrintStream(new FileOutputStream(new File(javaPath,
			"error.log")), true);
		
		pluginDir = new File(javaPath).getParentFile();

		// This is needed on mac, where there is more than one thread and the
		// Loader is initiated on startup
		// in the second thread. The ScriptographerEngine get loaded through the
		// Loader, so getting the ClassLoader from there is save:
		Thread.currentThread().setContextClassLoader(
				ScriptographerEngine.class.getClassLoader());
		// get the baseDir setting, if it's not set, ask the user
		String dir = getPreferences(false).get(
			"scriptDir", null);
		// If nothing is defined, try the default place for Scripts: In the
		// plugin's folder
		scriptDir = dir != null ? new File(dir)
			: new File(pluginDir, "scripts");
		// If the specified folder does not exist, ask the user
		if (!scriptDir.exists() || !scriptDir.isDirectory())
			chooseScriptDirectory();

		// Execute all __init__ scripts in startup folder:
		if (scriptDir != null)
			callInitScripts(scriptDir);

		// Explicitly initialize all dialogs on startup, as otherwise
		// funny things will happen on CS3 -> see comment in initializeAll
		Dialog.initializeAll();
	}

	public static void destroy() {
		// We're shuting down, so do not display console stuff any more
		ConsoleOutputStream.enableRedirection(false);
		stopAll();
		Dialog.destroyAll();
		LiveEffect.removeAll();
		MenuItem.removeAll();
		Timer.disposeAll();
		Annotator.disposeAll();
		try {
			// This is needed on some versions on Mac CS (CFM?)
			// as the JVM seems to not shoot down properly,
			//and the prefs would then not be flushed to file otherwise.
			getPreferences(false).flush();
		} catch (java.util.prefs.BackingStoreException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean chooseScriptDirectory() {
		scriptDir = Dialog.chooseDirectory(
			"Please choose the Scriptographer Script directory:", scriptDir);
		if (scriptDir != null && scriptDir.isDirectory()) {
			getPreferences(false).put("scriptDir",
				scriptDir.getPath());
			return true;
		}
		return false;
	}

	public static File getPluginDirectory() {
		return pluginDir;
	}

	public static File getScriptDirectory() {
		return scriptDir;
	}
	
	public static Preferences getPreferences(boolean fromScript) {
		if (fromScript && currentFile != null)
			return getPreferences(currentFile);
		// the base prefs for Scriptographer are:
		// com.scriptographer.preferences on mac, three nodes seem to be
		// necessary, otherwise things get mixed up...
		return Preferences.userNodeForPackage(
				ScriptographerEngine.class).node("preferences");
	}

	public static Preferences getPreferences(File file) {
		// determine preferences for the current executing script
		// by walking up the file path to the script directory and using each
		// folder as a preference node.
		Preferences prefs = getPreferences(false).node("scripts");
		ArrayList parts = new ArrayList();
		File root = getScriptDirectory();
		// collect the directory parts up to root
		do {
			parts.add(file.getName());
			file = file.getParentFile();
		} while (file != null && !file.equals(root));

		for (int i = parts.size() - 1; i >= 0; i--) {
			prefs = prefs.node((String) parts.get(i));
		}
		return prefs;
	}
	
	public static void reportError(Throwable t) {
		String error = t.getMessage();
		if (error != null) {
			logger.print(error);
			logger.print("Stacktrace: ");
		}
		t.printStackTrace(logger);
		logger.println();
		if (error != null) {
			System.err.print(error);
			char last = error.charAt(error.length() - 1);
			if (last != '\n' && last != '\r')
				System.err.println();
		} else {
			System.err.println(t);
		}
	}

	static int reloadCount = 0;

	public static int getReloadCount() {
		return reloadCount;
	}

	public static String reload() {
		stopAll();
		reloadCount++;
		return nativeReload();
	}

	public static native String nativeReload();

	static ScriptographerCallback callback;

	public static void setCallback(ScriptographerCallback cback) {
		callback = cback;
		ConsoleOutputStream.setCallback(cback);
	}

	public static void onAbout() {
		callback.onAbout();
	}
	
	private static boolean executing = false;
	private static File currentFile = null;
	private static ArrayList stopScripts = new ArrayList();
	private static boolean allowScriptCancelation = true;

	/**
	 * To be called before AI functions are executed
	 */
	private static boolean beginExecution(File file, Scope scope) {
		// Since the interface is done in scripts too, we need to cheat
		// a bit here. When file is set, we ignore the current state
		// of "executing", as we're about to to execute a new script...
		if (!executing || file != null) {
			if (!executing)
				Document.beginExecution();
			// Disable output to the console while the script is executed as it
			// won't get updated anyway
			// ConsoleOutputStream.enableOutput(false);
			executing = true;
			showProgress(file != null ? "Executing " + file.getName() + "..." : "Executing...");
			if (file != null) {
				currentFile = file;
				// Put a script object in the scope to offer the user
				// access to information about it.
				if (scope.get("script") == null)
					scope.put("script", new com.scriptographer.sg.Script(file), true);
			}
			return true;
		}
		return false;
	}

	/**
	 * To be called after AI functions were executed
	 */
	private static void endExecution() {
		if (executing) {
			try {
				CommitManager.commit();
			} catch(Throwable t) {
				ScriptographerEngine.reportError(t);
			}
			Document.endExecution();
			closeProgress();
			currentFile = null;
			executing = false;
		}
	}

	/**
	 * Invokes the method on the object, passing the arguments to it and calling
	 * beginExecution before and endExecution after it, which commits all
	 * changes after execution.
	 * 
	 * @param onDraw
	 * @param annotator
	 * @param objects
	 * @throws ScriptException 
	 */
	public static Object invoke(Callable callable, Object obj, Object[] args) {
		boolean started = beginExecution(null, null);
		// Retrieve wrapper object for the native java object, and call the
		// function on it.
		Throwable throwable = null;
		try {
			return callable.call(obj, args);
		} catch (Throwable t) {
			throwable = t;
		} finally {
			// commit all changed objects after a scripting function has been
			// called!
			if (started)
				endExecution();
		}
		// Do not allow script cancelation during error reporting,
		// as this is now handled by scripts too
		allowScriptCancelation = false;
		if (throwable instanceof ScriptException) {
			ScriptographerEngine.reportError(throwable);
		} else if (throwable instanceof ScriptCanceledException) {
			System.out.println("Execution canceled");
		}
		allowScriptCancelation = true;
		return null;
	}

	public static Object invoke(Callable callable, Object obj)
			throws ScriptException {
		return invoke(callable, obj, new Object[0]);
	}

	/**
	 * executes the specified script file.
	 *
	 * @param file
	 * @return
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static Object execute(File file, Scope scope)
			throws ScriptException, IOException {
		ScriptEngine engine = ScriptEngine.getEngineByFile(file);
		if (engine == null)
			throw new ScriptException("Unable to find script engine for " + file);
		Script script = engine.compile(file);
		if (script == null)
			throw new ScriptException("Unable to compile script " + file);
		boolean started = false;
		Object ret = null;
		try {
			if (scope == null)
				scope = script.getEngine().createScope();
			started = beginExecution(file, scope);
			ret = script.execute(scope);
			if (started) {
				// handle onStart / onStop
				com.scriptographer.sg.Script scriptObj =
					(com.scriptographer.sg.Script) scope.get("script");
				Callable onStart = scriptObj.getOnStart();
				if (onStart != null)
					onStart.call(scriptObj);
				if (scriptObj.getOnStop() != null) {
					// add this scope to the scopes that want onStop to be called
					// when the stop button is hit by the user
					stopScripts.add(scriptObj);
				}
			}
		} catch (ScriptException e) {
			reportError(e);
		} catch (ScriptCanceledException e) {
			System.out.println(file != null ? file.getName() + " canceled" :
				"Execution canceled");
		} finally {
			// commit all the changes, even when script has crashed (to synch
			// with
			// direct changes such as creation of paths, etc
			if (started) {
				endExecution();
				// now reenable the console, this also writes out all the things
				// that were printed in the meantime:
				// ConsoleOutputStream.enableOutput(true);
			}
		}
		return ret;
	}

	/**
	 * Executes all scripts named __init__.* in the given folder
	 *
	 * @param dir
	 * @throws IOException 
	 * @throws ScriptException 
	 */
	public static void callInitScripts(File dir) throws ScriptException, IOException {
		File []files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				File file = files[i];
				String name = file.getName();
				if (file.isDirectory() && !name.startsWith(".")
						&& !name.equals("CVS")) {
					callInitScripts(file);
				} else if (name.startsWith("__init__")) {
					execute(file, null);
				}
			}
		}
	}

	public static void stopAll() {
		Timer.stopAll();
		// Walk through all the stop scopes and call onStop on them:
		for (Iterator it = stopScripts.iterator(); it.hasNext();) {
			com.scriptographer.sg.Script script =
				(com.scriptographer.sg.Script) it.next();
			Callable onStop = script.getOnStop();
			if (onStop != null) {
				try {
					onStop.call(script);
				} catch (ScriptException e) {
					reportError(e);
				}
			}
		}
		stopScripts.clear();
	}


	/**
	 * Launches the filename with the default associated editor.
	 * 
	 * @param filename
	 */
	public static native boolean launch(String filename);

	public static boolean launch(File file) {
		return launch(file.getPath());
	}

	/**
	 * Returns the current system time in nano seconds.
	 * This is very useful for high resolution time measurements.
	 * @return the current system time.
	 */
	public static native long getNanoTime();

	private static long progressCurrent;
	private static long progressMax;
	private static boolean progressAutomatic;

	private static native void nativeSetProgressText(String text);

	public static void showProgress(String text) {
		progressAutomatic = true;
		progressCurrent = 0;
		progressMax = 1 << 8;
		nativeUpdateProgress(progressCurrent, progressMax);
		nativeSetProgressText(text);
	}
	
	private static native boolean nativeUpdateProgress(long current, long max);

	public static  boolean updateProgress(long current, long max) {
		progressCurrent = current;
		progressMax = max;
		progressAutomatic = false;
		boolean ret = nativeUpdateProgress(current, max);
		return !allowScriptCancelation || ret;
	}

	public static boolean updateProgress() {
		boolean ret = nativeUpdateProgress(progressCurrent, progressMax);
		if (progressAutomatic) {
			progressCurrent++;
			progressMax++;
		}
		return !allowScriptCancelation || ret;
	}

	private static native void nativeCloseProgress();

	public static void closeProgress() {
		nativeCloseProgress();
		// BUGFIX: After display of progress dialog, the next modal 
		// dialog seems to be become active, even when it is invisible
		// The workaround is to walk through all dialogs and deactivate
		// the modal ones.
		Dialog.updateModalDialogs();
	}

	/**
	 * @jshide
	 */
	public static native void dispatchNextEvent();

	private static final boolean isWindows, isMacintosh;

	static {
		String os = System.getProperty("os.name").toLowerCase();
		isWindows = (os.indexOf("windows") != -1);
		isMacintosh = (os.indexOf("mac os x") != -1);
	}

	public static boolean isWindows() {
		return isWindows;
	}

	public static boolean isMacintosh() {
		return isMacintosh;
	}

	public static native String getApplicationVersion();

	public static native int getApplicationRevision();
}