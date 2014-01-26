package com.saibi.ereader.pojos;

import java.util.Date;

import com.dropbox.sync.android.DbxFileInfo;

@SuppressWarnings("unused")
public class EPubInfo {

	private DbxFileInfo mFileInfo;
	private String mTitle;
	private Date mModifiedTime;
	
	public EPubInfo(DbxFileInfo fileInfo) {
		mFileInfo = fileInfo;
	}
	
	public DbxFileInfo getFileInfo()
	{
		return mFileInfo;
	}
	
	public String getTitle()
	{
		return mFileInfo.path.getName();
	}
	
	public Date getModifiedTime()
	{
		return mFileInfo.modifiedTime;
	}
	
}
