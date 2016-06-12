/** 
 * DwCaReader.java 
 * 
 * Copyright 2015 President and Fellows of Harvard College
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kurator.validation.actors.io;

import akka.actor.PoisonPill;
import akka.routing.Broadcast;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.filteredpush.kuration.util.SpecimenRecord;
import org.gbif.dwc.record.StarRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.ArchiveFactory;
import org.gbif.dwc.text.UnsupportedArchiveException;import org.kurator.akka.actors.OneShot;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Actor to read occurrence data from DarwinCore Archive files.
 * 
 * @author mole
 *
 */
public class DwCaReader extends OneShot {
	
	private static final Log logger = LogFactory.getLog(DwCaReader.class);

    public String filePath;
    public Archive dwcArchive = null;

    Iterator<StarRecord> iterator;

	@Override
	public void fireOnce() throws Exception {
		File file =  new File(filePath);
		if (!file.exists()) {
			// Error
			logger.error(filePath + " not found.");
		}
		if (!file.canRead()) {
			// error
			logger.error("Unable to read " + filePath);
		}
		if (file.isDirectory()) {
			// check if it is an unzipped dwc archive.
			dwcArchive = openArchive(file);
		}
		if (file.isFile()) {
			// unzip it
			File outputDirectory = new File(file.getName().replace(".", "_") + "_content");
			if (!outputDirectory.exists()) {
				outputDirectory.mkdir();
				try {
					byte[] buffer = new byte[1024];
					ZipInputStream inzip = new ZipInputStream(new FileInputStream(file));
					ZipEntry entry =  inzip.getNextEntry();
					while (entry!=null) {
						String fileName = entry.getName();
						File expandedFile = new File(outputDirectory.getPath() + File.separator + fileName);
						new File(expandedFile.getParent()).mkdirs();
						FileOutputStream expandedfileOutputStream = new FileOutputStream(expandedFile);
						int len;
						while ((len = inzip.read(buffer)) > 0) {
							expandedfileOutputStream.write(buffer, 0, len);
						}

						expandedfileOutputStream.close();
						entry = inzip.getNextEntry();
					}
					inzip.closeEntry();
					inzip.close();
					System.out.println("Unzipped archive into " + outputDirectory.getPath());
				} catch (FileNotFoundException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				} catch (IOException e) {
					logger.error(e.getMessage());
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			// look into the unzipped directory
			dwcArchive = openArchive(outputDirectory);

			iterator = dwcArchive.iterator();
			while (iterator.hasNext()) {
				// read initial set of rows, pass downstream
				StarRecord dwcrecord = iterator.next();
				SpecimenRecord record = new SpecimenRecord(dwcrecord);
				broadcast(record);
			}
		}
		if (dwcArchive!=null) {
			if (checkArchive()) {
				// good to go
			}
		} else {
			System.out.println("Problem opening archive.");
		}
	}

    /**
     * Attempt to open a DarwinCore archive directory and return it as an Archive object.  
     * If an UnsupportedArchiveException is thrown, trys again harder by looking for an archive
     * directory inside the provided directory.
     * 
     * @param outputDirectory directory that should represent an unzipped DarwinCore archive.
     * @return an Archive object repsesenting the content of the directory or null if unable
     * to open an archive object.
     */
    protected Archive openArchive(File outputDirectory) { 
    	Archive result = null;
    	try {
    		result = ArchiveFactory.openArchive(outputDirectory);
    	} catch (UnsupportedArchiveException e) {
    		logger.error(e.getMessage());
    		File[] containedFiles = outputDirectory.listFiles();
    		boolean foundContained = false;
    		for (int i = 0; i<containedFiles.length; i++) { 
    			if (containedFiles[i].isDirectory()) {
    				try {
    					// Try harder, some pathological archives contain a extra level of subdirectory
    					result = ArchiveFactory.openArchive(containedFiles[i]);
    					foundContained = true;
    				} catch (Exception e1) { 
    					logger.error(e.getMessage());
    					System.out.println("Unable to open archive directory " + e.getMessage());
    					System.out.println("Unable to open directory contained within archive directory " + e1.getMessage());
    				}
    			}
    		}
    		if (!foundContained) { 
    			System.out.println("Unable to open archive directory " + e.getMessage());
    		}					
    	} catch (IOException e) {
    		logger.error(e.getMessage());
    		System.out.println("Unable to open archive directory " + e.getMessage());
    	}
    	return result;
    }
    
    protected boolean checkArchive() {
    	boolean result = false;
    	if (dwcArchive==null) { 
    		return result;
    	}
	    if (dwcArchive.getCore() == null) {
		      System.out.println("Cannot locate the core datafile in " + dwcArchive.getLocation().getPath());
		      return result;
		}
		System.out.println("Core file found: " + dwcArchive.getCore().getLocations());
		System.out.println("Core row type: " + dwcArchive.getCore().getRowType());
		if (dwcArchive.getCore().getRowType().equals(DwcTerm.Occurrence) ) {
			
			// check expectations 
		    List<DwcTerm> expectedTerms = new ArrayList<DwcTerm>();
		    expectedTerms.add(DwcTerm.scientificName);
		    expectedTerms.add(DwcTerm.scientificNameAuthorship);
		    expectedTerms.add(DwcTerm.eventDate);
		    expectedTerms.add(DwcTerm.recordedBy);
		    expectedTerms.add(DwcTerm.decimalLatitude);
		    expectedTerms.add(DwcTerm.decimalLongitude);
		    expectedTerms.add(DwcTerm.locality);
		    expectedTerms.add(DwcTerm.basisOfRecord);
		    
		    for (DwcTerm term : expectedTerms) {
		      if (!dwcArchive.getCore().hasTerm(term)) {
		        System.out.println("Cannot find " + term + " in core of input dataset.");
		      }
		    } 		
		    
		    result = true;
		} else { 
			// currently can only process occurrence core
		}

        return result;
    }

}
