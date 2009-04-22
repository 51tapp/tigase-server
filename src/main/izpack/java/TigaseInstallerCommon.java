package com.izforge.izpack.panels;

import java.io.InputStream;

import com.izforge.izpack.LocaleDatabase;
import com.izforge.izpack.installer.ResourceManager;
import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.util.Debug;

class TigaseInstallerCommon {

	public static AutomatedInstallData init(AutomatedInstallData iData) {
		insertResource(iData.langpack);
		return iData;
	}	

	public static InstallData init(InstallData iData) {
		insertResource(iData.langpack);
		return iData;
	}

	private final static String LANG_FILE_NAME = "panelsLangPack.xml";
	private static void insertResource(LocaleDatabase langpack) {
		try {
			InputStream inputStream = 
				ResourceManager.getInstance().getInputStream(LANG_FILE_NAME);
			langpack.add(inputStream);
		}
		catch (Exception exc) {
			Debug.trace(exc);
		}
	}
}
