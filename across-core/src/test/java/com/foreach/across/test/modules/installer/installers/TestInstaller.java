package com.foreach.across.test.modules.installer.installers;

import com.foreach.across.core.annotations.InstallerMethod;

import java.util.LinkedList;
import java.util.List;

public abstract class TestInstaller
{
	public static List<Class> EXECUTED = new LinkedList<>();

	@InstallerMethod
	public void run() {
		EXECUTED.add( getClass() );
	}

	public static Class[] executed() {
		return EXECUTED.toArray( new Class[EXECUTED.size()] );
	}

	public static void reset() {
		EXECUTED.clear();
	}
}
