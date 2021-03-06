package controller;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
//import java.time.LocalDate;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
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
		
		//1- v??rification de l'extension du fichier
		String[] fileNameSplit = file.getOriginalFilename().split("\\.");
		if(fileNameSplit.length >= 2) {
			if(fileNameSplit[fileNameSplit.length - 1].equals("timeline")) {
				//return JsonResponse.generateResponse("Extension de fichiers valide", 200, null);
				
				//2 - on v??rifie que la personne existe 
				try {
					Personne personne = jdbcTemplate.queryForObject(SQLRequests.SelectPersonById, new PersonneRowMapper(), idLogin);
					
					//3 - ajout du fichier
					try {
						storageService.save(file,idLogin,FilesStorageServiceImpl.TimelineFolderName);
						
						//initialisation de l'??tat dans la bdd
						
						int codeRetourInit = jdbcTemplate.update(SQLRequests.InsertTaskInitialize, file.getOriginalFilename(), personne.getIdPersonne());
						
						// 4 - lancement du traitement (g??rer en async)
						//TODO r??cup de l'os soit dans la route (=> demande ?? l'user), soit dans le fichier (+ logique si l'info est dedans)
						
						TaskExecutor theExecutor = new SimpleAsyncTaskExecutor();
						
						theExecutor.execute(new Runnable() {
				            @Override
				            public void run () {
				            	processFileToJson(file.getOriginalFilename(),personne.getIdPersonne() , idLogin, "ios");
				            }
				        });
						
						//processFileToJson(file.getOriginalFilename(),personne.getIdPersonne() , idLogin, "ios");
						
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
	
	
	//@Async
	//public void launchprocessFileToJsonAsync()
	
	/*
	 * TODO appel en async
	 */
	private int processFileToJson(String filename, int IdPersonne , String idLogin, String typeOS) {
		
		//objet qui permettra la sauvegarde du fichier trajets.json
		JSONObject trajetsJson = null;
		
		HashMap<String, JSONObject> indicesfeaturesJson = new HashMap<String, JSONObject>();
		
		//map permettant de trier par date les trajets. Pas besoin d'utiliser une object date, le comparator de string suffit pour ordonner les donn??es
		TreeMap<String,JSONObject> trajetsSortedByDate = new TreeMap<String,JSONObject>() ;
		
		//Resource jsonFile = storageService.load(filename,idLogin);
		
		
		
		//2 - chargement du trajets.json de la personne
		/* 
		 * On charge le fichier trajets.json si il existe d??j?? pour la personne.
		 * Sinon on chargera un template dans la Partie 3
		 *  Si le fichier n'existe pas le storageService renvoie un Runtime exception. Ceci est une behaviour attendu
		 */
		try {
			Resource jsonFile = storageService.loadTrajetJson(idLogin);
			
			try (Reader reader = new InputStreamReader(jsonFile.getInputStream())){
				String jsonString = FileCopyUtils.copyToString(reader);
				trajetsJson = new JSONObject(jsonString);
				
				//r??cup??ration des trajets dans la map de tri
				JSONArray trajetsInKeyTempo = trajetsJson.getJSONArray("trajets");
				for(int i = 0; i < trajetsInKeyTempo.length();i++) {
					JSONObject tr =  trajetsInKeyTempo.getJSONObject(i);
					String datetime = tr.getJSONObject("cleaned_section").getJSONObject("data").getString("start_fmt_time");
					trajetsSortedByDate.put(datetime, tr);
				}
				
				//debug
				/*
				for (Map.Entry<String, JSONObject> entry : trajetsSortedByDate.entrySet()) {
				    String key = entry.getKey();
				    System.out.println("cl??: "+key);//debug
				    JSONObject value = entry.getValue();
				    //System.out.println("value: "+value);//debug
				}
				*/
				    
				//nettoyage des trajets dans le json charg??
				trajetsJson.put("trajets", new JSONArray());
				
				//clear
				//trajetsInKeyTempo = null;
				//System.gc();
				
			}catch (IOException e) {
				//System.err.println("IOException1: "+e);
			}
		}catch(Exception e) {
			//le fichier n'existe pas
		}
		
		//3 - chargement du template si aucun fichier dans la session n'existe
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
					jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
					return -1;
				}
				
			}catch(Exception e) {
				System.err.println(e);
				jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
				return -1;
			}
		}
		
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
				
				//r??cup??ration des blocs
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
				
				//v??rification si la section existe d??j?? dans trajets.json
				for (Map.Entry<String, JSONArray> entry : locationByIdSection.entrySet()) {
				    String key = entry.getKey();
				    System.out.println("locationByIdSection: "+key);//debug
				    JSONArray value = entry.getValue();
				    //System.out.println( jsonArrayTrajetExist(trajetsJson.getJSONArray("trajets"), key) );
				    
				    // 5 - ajout des indices
				    //TODO key by date
				    if(!indicesfeaturesJson.containsKey("1")) {
				    	//chargement de la resource
				    	JSONObject tempoIndices = null;
				    	try {
				    		JSONObject dateJson = sectionClean.get(key).getJSONObject("data").getJSONObject("start_local_dt");
				    		String dateString = dateJson.getInt("year") + "-" + String.format("%02d", dateJson.getInt("month")) + "-" +String.format("%02d", dateJson.getInt("day"));
				    		
				    		
				    		//tentative chargement en cache du fichier wfs
				    		//JSONObject wfsJson = null;
				    		try {
					    		Resource jsonWfs = storageService.load(dateString+".json", idLogin, FilesStorageServiceImpl.WfsFolderName);
								try (Reader readerWfs = new InputStreamReader(jsonWfs.getInputStream())){
									String jsonString = FileCopyUtils.copyToString(readerWfs);
									
									tempoIndices = new JSONObject(jsonString);
									
								}catch (IOException e) {
									//le fichier n'existe pas
									//System.out.println("file not found: "+e.getMessage());
									//jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
									//return -1;
								}
				    		}catch(Exception e) {
				    			
				    		}
				    		
				    		if(tempoIndices == null) {
			
					    		//InputStream indicesJsonFile = new URL(SQLRequests.GetIndicesByDate).openStream();
					    		String wfsRequest = SQLRequests.GetIndicesByDate1 + dateString + SQLRequests.GetIndicesByDate2;
					    		
					    		InputStream indicesJsonFile = new URL(wfsRequest).openStream();
					    		
					    		try (Reader readerObject = new InputStreamReader(indicesJsonFile)){
					    			String jsonString = FileCopyUtils.copyToString(readerObject);
					    			
					    			//sauvegarde du fichier
					    			
					    			Path filePath = FilesStorageServiceImpl.rootWfs;
					    			//filePath = filePath.resolve(FilesStorageServiceImpl.rootWfs);
					    			System.out.println(filePath);
				    				try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toString()+"/" + dateString +".json"))){
			    						writer.write(jsonString);
			    						
			    						//Files.copy(file.getInputStream(), folderSession.resolve(file.getOriginalFilename()),  StandardCopyOption.REPLACE_EXISTING);
			    					}catch (IOException e) {
			    						System.err.println("IOException: " + e);
			    					}
					    			
					    			//r??cup??ration des indices
					    			tempoIndices = new JSONObject(jsonString);
					    			
					    		}catch (IOException e) {
					    			System.err.println("erreur lors de la r??cup??ration des indices, seront manquant pour se trajet");
					    			System.err.println(e);
					    		}
				    		}
				    		
				    	}catch(Exception e) {
				    		System.err.println("erreur lors de la r??cup??ration des indices, seront manquant pour se trajet");
				    		System.err.println(e);
				    	}
				    	
				    	//add to hashmap
				    	if(tempoIndices != null) {
					    	//TODO add by date
				    		indicesfeaturesJson.put("1", tempoIndices);
				    		tempoIndices = null;
					    }
				    }
				    
				    //ajout des indices au trajet
				    if(indicesfeaturesJson.containsKey("1")) {
				    	
				    	//value
				    	for(int i = 0; i < value.length();i++) {
				    		JSONObject location = value.getJSONObject(i);
				    		JSONArray coords = location.getJSONObject("data").getJSONObject("loc").getJSONArray("coordinates");
				    		int indexFeature = getIndexMinimalDistance(coords.getDouble(0),coords.getDouble(1),indicesfeaturesJson.get("1").getJSONArray("features"));
				    		
				    		if(indexFeature >= 0) {
				    			JSONObject IndicesJson = indicesfeaturesJson.get("1").getJSONArray("features").getJSONObject(indexFeature).getJSONObject("properties");
				    			
				    			location.getJSONObject("data").put("code_no2", IndicesJson.getInt("code_no2"));
				    			location.getJSONObject("data").put("code_so2", IndicesJson.getInt("code_so2"));
				    			location.getJSONObject("data").put("code_o3", IndicesJson.getInt("code_o3"));
				    			location.getJSONObject("data").put("code_pm10", IndicesJson.getInt("code_pm10"));
				    			location.getJSONObject("data").put("code_pm25", IndicesJson.getInt("code_pm25"));
				    		}
				    		
				    	}
				    }
				    
				    
				    if(!jsonArrayTrajetExist(trajetsJson.getJSONArray("trajets"), key)) {
				    	
				    	//5 - ajout du trajet
				    	//TODO voir si le template n'est pas inutile : la cr??ation du trajet utilse put qui ??crase les valeurs
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
									jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
									return -1;
								}
								//trajetsJson.getJSONArray("trajets").put(trajetObjectTemplate);
								trajetsSortedByDate.put(sectionClean.get(key).getJSONObject("data").getString("start_fmt_time"), trajetObjectTemplate);
								
							}catch (IOException e) {
								//le fichier n'existe pas
								System.out.println("file template object not found: "+e.getMessage());
								jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
								return -1;
							}
						}catch(Exception e) {
							System.err.println(e);
							jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
							return -1;
						}
				    }
				}
				
				//ajout des trajets dans le json
				for (Map.Entry<String, JSONObject> entry : trajetsSortedByDate.entrySet()) {
				    String key = entry.getKey();
				    System.out.println("cl??: "+key);//debug
				    //JSONObject value = entry.getValue();
				    //System.out.println("value: "+value);//debug
				    trajetsJson.getJSONArray("trajets").put(entry.getValue());
				}
				
			}catch (IOException e) {
				//le fichier n'existe pas
				System.out.println("file not found: "+e.getMessage());
			}
			
		}catch(Exception e) {
			System.err.println(e);
			jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("error"),filename,IdPersonne);
			return -1;
		}
		
		
		//6 - sauvegarde du fichier
		Path filePath = FilesStorageServiceImpl.rootPersonne;
		filePath = filePath.resolve(idLogin + "/" + FilesStorageServiceImpl.TrajetFolderName);
		try {
			//si le dossier trajet n'existe pas, on le cr??e
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
		
		jdbcTemplate.update(SQLRequests.UpdateEtatTask,SQLRequests.etats.get("ok"),filename,IdPersonne);
		return 1;
		//System.out.println(trajetsFileTemplate.toString(1));
	}
	
	private double getDistanceBetweenPoints(double x1,double y1, double x2, double y2) {
		return Math.sqrt(((x1 - x2) * (x1 - x2)) + ((y1 - y2) * (y1 - y2)));
	}
	
	private int getIndexMinimalDistance(double x, double y, JSONArray features) {
		
		double minDistance = Float.MAX_VALUE;
		int minIndex = -1;
		
		for(int i = 0;i < features.length(); i++) {
			//JSONArray coordinates = features.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
			double xF = features.getJSONObject(i).getJSONObject("properties").getDouble("x_wgs84");
			double yF = features.getJSONObject(i).getJSONObject("properties").getDouble("y_wgs84");
			//double distance = getDistanceBetweenPoints(x, y, coordinates.getDouble(0), coordinates.getDouble(1));
			double distance = getDistanceBetweenPoints(x, y, xF, yF);
			if(distance < minDistance) {
				minDistance = distance;
				minIndex = i;
			}
		}
		
		return minIndex;
	}
	
	/*
	@GetMapping("/testprocesstimeline/{typeOS}/{idLogin}/{filename}")
	@ResponseBody
	public ResponseEntity<Object> readJson(@PathVariable String filename,@PathVariable("idLogin") String idLogin, @PathVariable("typeOS") String typeOS) {
		
		processFileToJson(filename, 1 ,idLogin, "ios");
		
		return null; //debug
	}
	*/
	
	@GetMapping("/files/{idLogin}")
	public /*ResponseEntity<List<FileInfo>>*/ ResponseEntity<Object> getListFiles(@PathVariable("idLogin") String idLogin) {
		
		// on v??rifie que la session existe
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
