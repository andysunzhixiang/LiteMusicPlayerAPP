package org.loader.liteplayer.pojo;

import java.io.Serializable;

/**
 * 2015年8月15日 15:51:26
 * 博文地址：http://blog.csdn.net/u010156024
 */
public class SearchResult implements Serializable {
	private static final long serialVersionUID = 0X00000001l;
	private String musicName;
	private String url;
	private String artist;
	private String album;

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getMusicName() {
		return musicName;
	}

	public void setMusicName(String musicName) {
		this.musicName = musicName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}
}
