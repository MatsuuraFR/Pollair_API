package controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.bind.annotation.CrossOrigin;

import Tools.FileInfo;
import Tools.JsonResponse;
import Tools.SQLRequests;
import Tools.TrajetsFileTemplate;
import model.Personne;
import model.PersonneRowMapper;
import service.FilesStorageService;
import service.FilesStorageServiceImpl;

//import com.bezkoder.spring.files.upload.model.FileInfo;
//import com.bezkoder.spring.files.upload.model.ResponseMessage;
//import com.bezkoder.spring.files.upload.service.FilesStorageService;

@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
@RestController
@RequestMapping(path = "/fileshandler")
//@CrossOrigin("http://localhost:8080")
public class UploadFileHandler {

	@Autowired
	FilesStorageService storageService;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@PostMapping("/uploadone")
	public ResponseEntity<Object> uploadOneFile(@RequestParam("file") MultipartFile file,@RequestParam("id") String idLogin) {
		//String message = "";
		
		//1- vérification de l'extension du fichier
		String[] fileNameSplit = file.getOriginalFilename().split("\\.");
		if(fileNameSplit.length >= 2) {
			if(fileNameSplit[fileNameSplit.length - 1].equals("timeline")) {
				//return JsonResponse.generateResponse("Extension de fichiers valide", 200, null);
				
				//2 - on vérifie que la personne existe 
				try {
					Personne personne = jdbcTemplate.queryForObject(SQLRequests.SelectPersonById, new PersonneRowMapper(), idLogin);
					
					//3 - ajout du fichier
					try {
						storageService.save(file,idLogin,FilesStorageServiceImpl.TimelineFolderName);
						
						// 4 - lancement du traitement (gérer en async)
						
						return JsonResponse.generateResponse("Extension de fichiers valide", 200, null);
					}catch(Exception e){
						return JsonResponse.generateResponse("Erreur lors de l'ajout du fichier", -1, null);
					}
					
				}catch(EmptyResultDataAccessException e) {
					
					return JsonResponse.generateResponse("ID inconnu", 0, null);
				}
			}else {
				return JsonResponse.generateResponse("Extension de fichiers invalide: ", 1, null);
			}
		}else {
			return JsonResponse.generateResponse("Erreur extension fichier: ", -1, null);
		}
	}
	
	/*
	 * TODO appel en async
	 */
	private int processFileToJson(String filename, String idLogin, String typeOS) {
		
		//objet qui permettra la sauvegarde du fichier trajets.json
		JSONObject trajetsJson = null;
		
		//Resource jsonFile = storageService.load(filename,idLogin);
		
		//1 - création d'une task
		//TODO
		
		//2 - chargement du trajets.json de la personne
		/* 
		 * On charge le fichier trajets.json si il existe déjà pour la personne.
		 * Sinon on chargera un template dans la Partie 3
		 *  Si le fichier n'existe pas le storageService renvoie un Runtime exception. Ceci est une behaviour attendu
		 */
		try {
			Resource jsonFile = storageService.loadTrajetJson(idLogin);
			
			try (Reader reader = new InputStreamReader(jsonFile.getInputStream())){
				String jsonString = FileCopyUtils.copyToString(reader);
				
				trajetsJson = new JSONObject(jsonString);
				
				//System.out.println(existingtrajetsJson.toString(1));
				
			}catch (IOException e) {
				//System.err.println("IOException1: "+e);
				
			}
		}catch(Exception e) {
			//le fichier n'existe pas
		}
		
		//3 - 
		//TrajetsFileTemplate trajetsFileTemplate = new TrajetsFileTemplate();
		//chargement du template si aucun fichier dans la session n'existe
		if(trajetsJson == null) {
			//trajetsFileTemplate.setCleaned_place(existingtrajetsJson.getJSONArray("cleaned_place"));
			try {
				Resource jsonFileTemplate = storageService.load("trajets_file_template.json", idLogin, "template");
				try (Reader reader = new InputStreamReader(jsonFileTemplate.getInputStream())){
					String jsonString = FileCopyUtils.copyToString(reader);
					
					trajetsJson = new JSONObject(jsonString);
					
				}catch (IOException e) {
					//le fichier n'existe pas
					System.out.println("file not found: "+e.getMessage());
					return -1;
				}
				
			}catch(Exception e) {
				System.err.println(e);
				return -1;
			}
		}
		
		//4 - 
		//TODO ajout (avec vérif si existe déjà) des trajets
		//4 - chargement du fichier timeline 
		
		//TODO raw_place cleaned_place ?
		
		JSONArray timelineFileJson = null;
		
		JSONArray tripRaw = new JSONArray();
		
		//JSONArray tripsClean = new JSONArray();
		HashMap<String,JSONObject> tripsClean = new HashMap<String,JSONObject>();
		
		//JSONArray sectionClean = new JSONArray();
		HashMap<String,JSONObject> sectionClean = new HashMap<String,JSONObject>();
		
		//rassemble les points par l'id de leur section
		HashMap<String,JSONArray> locationByIdSection = new HashMap<String,JSONArray>();
		
		try {
			Resource timelineFile = storageService.load(filename,idLogin,"timeline");
			try (Reader reader = new InputStreamReader(timelineFile.getInputStream())){
				timelineFileJson = new JSONArray(FileCopyUtils.copyToString(reader));
				//récupération des blocs
				for(int i=0; i < timelineFileJson.length();i++) {
					
					//segmentation
					switch(getJsonMetadataKey(timelineFileJson.getJSONObject(i))) {
						case "segmentation/raw_trip": {
							//tripRaw.put(timelineFileJson.getJSONObject(i));
						}
						break;
						case "segmentation/raw_place" : {
							//placesRaw.put(timelineFileJson.getJSONObject(i).getJSONObject("_id").getString("$oid"),timelineFileJson.getJSONObject(i));
						}
						break;
						case "analysis/cleaned_trip" : {
							//tripsClean.put(timelineFileJson.getJSONObject(i));
							tripsClean.put(getIdJSONObject(timelineFileJson.getJSONObject(i)), timelineFileJson.getJSONObject(i));
						}
						break;
						case "analysis/cleaned_place" : {
							//placesClean.put(timelineFileJson.getJSONObject(i).getJSONObject("_id").getString("$oid"),timelineFileJson.getJSONObject(i));
						}
						break;
						case "analysis/recreated_location" : {
							String idSection = timelineFileJson.getJSONObject(i).getJSONObject("data").getJSONObject("section").getString("$oid");
							if(!locationByIdSection.containsKey(idSection)) {
								locationByIdSection.put(idSection, new JSONArray());
							}
							locationByIdSection.get(idSection).put(timelineFileJson.getJSONObject(i));
							
						}
						break;
						case "analysis/cleaned_section" : {
							//sectionClean.put(timelineFileJson.getJSONObject(i));
							sectionClean.put(getIdJSONObject(timelineFileJson.getJSONObject(i)),timelineFileJson.getJSONObject(i));
						}
					}
				}
				System.out.println(tripsClean.size());
				
				//vérification si la section existe déjà dans trajets.json
				for (Map.Entry<String, JSONArray> entry : locationByIdSection.entrySet()) {
				    String key = entry.getKey();
				    System.out.println("locationByIdSection: "+key);//debug
				    JSONArray value = entry.getValue();
				    //System.out.println( jsonArrayTrajetExist(trajetsJson.getJSONArray("trajets"), key) );
				    
				    if(!jsonArrayTrajetExist(trajetsJson.getJSONArray("trajets"), key)) {
				    	
				    	//5 - ajout du trajet
				    	//TODO voir si le template n'est pas inutile : la création du trajet utilse put qui écrase les valeurs
				    	JSONObject trajetObjectTemplate = null;
				    	try {
							Resource jsonFileObjectTemplate = storageService.load("trajet_object_template.json", idLogin, "template");
							try (Reader readerObject = new InputStreamReader(jsonFileObjectTemplate.getInputStream())){
								String jsonString = FileCopyUtils.copyToString(readerObject);

								//trajetsJson = new JSONObject(jsonString);
								trajetObjectTemplate = new JSONObject(jsonString);
								if(trajetObjectTemplate != null) {
									trajetObjectTemplate.put("from",filename);
									trajetObjectTemplate.put("os", typeOS);
									trajetObjectTemplate.put("cleaned_section", sectionClean.get(key));
									
									
									trajetObjectTemplate.put("cleaned_trip", tripsClean.get( sectionClean.get(key).getJSONObject("data").getJSONObject("trip_id").getString("$oid") ));
									trajetObjectTemplate.put("locations", locationByIdSection.get(key) );
								}else {
									return -1;
								}
								trajetsJson.getJSONArray("trajets").put(trajetObjectTemplate);
							}catch (IOException e) {
								//le fichier n'existe pas
								System.out.println("file template object not found: "+e.getMessage());
								return -1;
							}
						}catch(Exception e) {
							System.err.println(e);
							return -1;
						}
				    }
				}
				
			}catch (IOException e) {
				//le fichier n'existe pas
				System.out.println("file not found: "+e.getMessage());
			}
			
		}catch(Exception e) {
			System.err.println(e);
			return -1;
		}
		
		
		//5 - sauvegarde du fichier
		Path filePath = FilesStorageServiceImpl.rootPersonne;
		filePath = filePath.resolve(idLogin + "/" + FilesStorageServiceImpl.TrajetFolderName);
		try {
			//si le dossier trajet n'existe pas, on le crée
			if(!Files.exists(filePath)) {
				Files.createDirectories(filePath);
			}
			if(trajetsJson != null) {
				try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()+"/trajets.json"))){
					writer.write(trajetsJson.toString(1));
					
					//Files.copy(file.getInputStream(), folderSession.resolve(file.getOriginalFilename()),  StandardCopyOption.REPLACE_EXISTING);
				}catch (IOException e) {
					System.err.println("IOException2: " + e);
				}
			}
		
		}catch (IOException e) {
			System.err.println(e);
		}
		
		return 1;
		//System.out.println(trajetsFileTemplate.toString(1));
	}
	
	@GetMapping("/testprocesstimeline/{typeOS}/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> readJson(@PathVariable String filename,@PathVariable("idLogin") String idLogin, @PathVariable("typeOS") String typeOS) {
		
		processFileToJson(filename,idLogin, "ios");
		
		return null; //debug
	}
	
	@GetMapping("/files/{idLogin}")
	public /*ResponseEntity<List<FileInfo>>*/ ResponseEntity<Object> getListFiles(@PathVariable("idLogin") String idLogin) {
		
		// on vérifie que la session existe
		try {
			Personne personne = jdbcTemplate.queryForObject(SQLRequests.SelectPersonById, new PersonneRowMapper(), idLogin);
			
			List<FileInfo> fileInfos = storageService.loadAll(idLogin).map(path -> {
				String filename = path.getFileName().toString();
				String url = MvcUriComponentsBuilder
						.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString(),idLogin).build()
						.toString();

				return new FileInfo(filename, url);
			}).collect(Collectors.toList());

			//return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
			return JsonResponse.generateResponse("ok", 200, fileInfos);
		}catch(EmptyResultDataAccessException e) {
			
			return JsonResponse.generateResponse("ID inconnu", 0, null);
		}
		
		/*
		List<FileInfo> fileInfos = storageService.loadAll(idLogin).map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString(),idLogin).build()
					.toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
		*/
		
	}
	
	@GetMapping("/files/{idLogin}/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename,@PathVariable("idLogin") String idLogin) {
		Resource file = storageService.load(filename,idLogin,"timeline");
		//System.out.println("file.getFilename():"+file.getFilename());//debug
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	
	
	@GetMapping("/delete/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> deleteFile(@PathVariable String filename,@PathVariable("idLogin") String idLogin){
		
		try {
			int success = storageService.delete(filename, idLogin);
			
			if(success == 200) {
				return JsonResponse.generateResponse("ok", 200, null);
			}else {
				return JsonResponse.generateResponse("Le fichier n'existe pas", 0, null);
			}
			
		}catch(Exception e){
			return JsonResponse.generateResponse("Erreur ", -1, null);
		}
	}
	
	
	private String getJsonMetadataKey(JSONObject jso) {
		return jso.getJSONObject("metadata").getString("key");
	}
	
	private String getIdJSONObject(JSONObject jsonO) {
		
		return jsonO.getJSONObject("_id").getString("$oid");
	}
	
	private boolean jsonArrayTrajetExist(JSONArray trajets, String idSection) {
		boolean retour = false;
		int compt = 0;
		
		while(compt < trajets.length() && !retour) {
			try {
				//System.out.println(trajets.getJSONObject(compt).getJSONObject("cleaned_section").getJSONObject("_id").getString("$oid"));
				//System.out.println(idSection);
				if(trajets.getJSONObject(compt).getJSONObject("cleaned_section").getJSONObject("_id").getString("$oid").equals(idSection)) {
					retour = true;
				}
			}catch(Exception e) {
				System.err.println("key not found");
			}
			
			
			compt++;
		}
		
		return retour;
	}
	
	/*
	@PostMapping("/upload")
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile file) {
		String message = "";
		try {
			storageService.save(file);

			message = "Uploaded the file successfully: " + file.getOriginalFilename();
			return ResponseEntity.status(HttpStatus.OK).body(message);
		} catch (Exception e) {
			message = "Could not upload the file: " + file.getOriginalFilename() + "!";
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
		}
	}

	@GetMapping("/files")
	public ResponseEntity<List<FileInfo>> getListFiles() {
		List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
			String filename = path.getFileName().toString();
			String url = MvcUriComponentsBuilder
					.fromMethodName(UploadFileHandler.class, "getFile", path.getFileName().toString()).build()
					.toString();

			return new FileInfo(filename, url);
		}).collect(Collectors.toList());

		return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
	}

	@GetMapping("/files/{filename:.+}")
	@ResponseBody
	public ResponseEntity<Resource> getFile(@PathVariable String filename) {
		Resource file = storageService.load(filename);
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
				.body(file);
	}
	*/

}
