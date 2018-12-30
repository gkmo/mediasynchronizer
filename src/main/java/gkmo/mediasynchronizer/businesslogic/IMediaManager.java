package gkmo.mediasynchronizer.businesslogic;

import gkmo.mediasynchronizer.model.Album;
import gkmo.mediasynchronizer.model.MediaItem;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface IMediaManager {
	
    Map<String, Album> getAlbums();
    
    Map<String, MediaItem> getMediaItems(Album album);
    
    Album createAlbum(String name);

	void uploadMediaItem(File file, MediaItem item, String destinationAlbumId);

	void uploadMediaItems(Map<MediaItem, File> files, String destinationAlbumId);
	
	File downloadMediaItem(MediaItem item);

	Map<MediaItem, File> downloadMediaItems(List<MediaItem> mediaItems);
	
	Album getAlbumByName(String name);

	Album getAlbumById(String id);
	
	void shutdown();
}
