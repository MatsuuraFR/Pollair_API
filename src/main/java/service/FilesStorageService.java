package service;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {
	  public void init();

	  public void save(MultipartFile file, String idLogin);

	  public Resource load(String filename, String idLogin);

	  //public void deleteAll();
	  
	  public int delete(String filename, String idLogin);

	  public Stream<Path> loadAll(String idLogin);
}
