package com.saibi.ereader.domain;

import nl.siegmann.epublib.domain.Book;

import com.dropbox.client2.DropboxAPI.Entry;

public class DbxFileInfo {

	private Book book;
	private Entry entry;

	public DbxFileInfo(Book book, Entry entry) {
		this.book = book;
		this.entry = entry;
	}

	public Book getBook() {
		return book;
	}

	public Entry getEntry() {
		return entry;
	}

}
