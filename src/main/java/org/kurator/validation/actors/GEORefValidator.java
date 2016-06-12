package org.kurator.validation.actors;

import akka.actor.ActorRef;
import org.filteredpush.kuration.interfaces.IGeoRefValidationService;
import org.filteredpush.kuration.services.GeoLocate3;
import org.filteredpush.kuration.util.*;
import org.kurator.akka.KuratorActor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * This code is an adaptation of org.filteredpush.akka.actors.GEORefValidator
 * in the FP-Akka package
 */
public class GEORefValidator extends KuratorActor {
    private boolean useCache;
    private String serviceClassQN = "org.filteredpush.kuration.services.GeoLocate3";
    private IGeoRefValidationService geoRefValidationService;
    private double certainty; 	//the unit is km

    @Override
    protected void onInitialize() throws Exception {
        //resolve service
        try {
            geoRefValidationService = (IGeoRefValidationService)Class.forName(serviceClassQN).newInstance();
            geoRefValidationService.setUseCache(useCache);
        } catch (InstantiationException e) {
            geoRefValidationService = new GeoLocate3();
            geoRefValidationService.setUseCache(useCache);
            e.printStackTrace();
            System.exit(-1);
        } catch (IllegalAccessException e) {
            geoRefValidationService = new GeoLocate3();
            geoRefValidationService.setUseCache(useCache);
            e.printStackTrace();
            System.exit(-1);
        } catch (ClassNotFoundException e) {
            geoRefValidationService = new GeoLocate3();
            geoRefValidationService.setUseCache(useCache);
            e.printStackTrace();
            System.exit(-1);
        }
    }

    @Override
    protected void onData(Object value) throws Exception {
        if (value instanceof SpecimenRecord) {
            SpecimenRecord record = (SpecimenRecord) value;
            //System.err.println("georefstart#"+record.get("oaiid").toString() + "#" + System.currentTimeMillis());
            Map<String,String> fields = new HashMap<String,String>();
            for (String key : record.keySet()) {
                fields.put(key,record.get(key));
            }

            // TODO: We need an actor to examine country names and country codes
            // able to fill in one when the other is missing, and able to identify conflicts.

            //if missing, let it run, handle the error in service
            String country = record.get("country");
            String countryCode = record.get("countryCode");

            // Special case handling for GBIF Benin data set which largely lacks the country name.
            if (country==null || country.trim().length()==0) {
                if (countryCode!=null) {
                    country = CountryLookup.lookupCountry(countryCode);
                }
            }

            String stateProvince = record.get("stateProvince");
            String county = record.get("county");
            String locality = record.get("locality");
            String waterBody = record.get("waterBody");
            String verbatimDepth = record.get("verbatimDepth");
                    /*
                    //get the needed information from the input SpecimenRecord
                    String country = record.get("country");
                    if(country == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"country is missing in the input",getName());
                        constructOutput(fields,curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String stateProvince = record.get("stateProvince");
                    if(stateProvince == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"stateProvinceLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String county = record.get("county");
                    if (county == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"countyLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }

                    String locality = record.get("locality");
                    if(locality == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"localityLabel is missing in the input",getName());
                        constructOutput(fields, curationComment);
                        Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
                        return;
                    }
                     */
            int isCoordinateMissing = 0;
            String latitudeToken = record.get("decimalLatitude");
            double latitude = -1;

            if (latitudeToken != null && !latitudeToken.isEmpty()){
                //if(!(latitudeToken instanceof ScalarToken)){
                //    CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"latitudeLabel of the input is not of scalar type.",getName());
                //    constructOutput(fields, curationComment);
                //    return;
                //}
                try{
                    latitude = Double.valueOf(latitudeToken);
                }catch (Exception e){
                    System.out.println("latitude token has issue: |" + latitudeToken + "|");
                    System.exit(-1);
                }
            }else{
                isCoordinateMissing++;
            }

            String longitudeToken = record.get("decimalLongitude");
            double longitude = -1;
            if (longitudeToken != null && !longitudeToken.isEmpty()) {
                //if(!(longitudeToken instanceof ScalarToken)){
                //CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,"longitudeLabel of the input is not of scalar type.",getName());
                //constructOutput(fields, curationComment);
                //return;
                //}
                try{
                    longitude = Double.valueOf(longitudeToken);
                }catch (Exception e){
                    System.out.println("longitude token has issue: |" + latitudeToken + "|");
                    System.exit(-1);
                }
            } else {
                isCoordinateMissing++;
            }

            //invoke the service to parse the locality and return the coordinates
            //isCoordinateMissing == 2 means both longitude and latitude are missing
            if(isCoordinateMissing == 2){
                //geoRefValidationService.validateGeoRef(country, stateProvince, county, locality,null,null,certainty);
                CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, "Both longitude and latitude are missing in the incoming SpecimenRecord", null);
                updateAndSendRecord(new SpecimenRecord(fields),curationComment);
                return;
            }else{
                geoRefValidationService.validateGeoRef(country, stateProvince, county, waterBody, verbatimDepth, locality,String.valueOf(latitude),String.valueOf(longitude),certainty);
            }

            CurationStatus curationStatus = geoRefValidationService.getCurationStatus();
            if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN){
                String originalLat = fields.get(SpecimenRecord.dwc_decimalLatitude);
                String originalLng = fields.get(SpecimenRecord.dwc_decimalLongitude);
                String newLat = String.valueOf(geoRefValidationService.getCorrectedLatitude());
                String newLng = String.valueOf(geoRefValidationService.getCorrectedLongitude());

                if(originalLat != null && originalLat.length() != 0 &&  !originalLat.equals(newLat)){
                    fields.put(SpecimenRecord.Original_Latitude_Label, originalLat);
                    fields.put(SpecimenRecord.dwc_decimalLatitude, newLat);
                }
                if(originalLng != null && originalLng.length() != 0 && !originalLng.equals(newLng)){
                    fields.put(SpecimenRecord.Original_Longitude_Label, originalLng);
                    fields.put(SpecimenRecord.dwc_decimalLongitude, newLng);
                }
            }
            //output
            //System.out.println("curationStatus = " + curationStatus.toString());
            //System.out.println("curationComment = " + curationComment.toString());
            CurationCommentType curationComment = CurationComment.construct(curationStatus,geoRefValidationService.getComment(),geoRefValidationService.getServiceName());
            updateAndSendRecord(new SpecimenRecord(fields), curationComment);
            for (List l : geoRefValidationService.getLog()) {
                //Prov.log().printf("service\t%s\t%d\t%s\t%d\t%d\t%s\t%s\n", this.getClass().getSimpleName(), invoc, (String)l.get(0), (Long)l.get(1), (Long)l.get(2),l.get(3),curationStatus.toString());
            }
        }
        //Prov.log().printf("invocation\t%s\t%d\t%d\t%d\n", this.getClass().getSimpleName(), invoc, start, System.currentTimeMillis());
    }

    private void updateAndSendRecord(SpecimenRecord result, CurationCommentType comment) {
        if (comment != null) {
            result.put(SpecimenRecord.geoRef_Status_Label,comment.getStatus());
        } else {
            result.put(SpecimenRecord.geoRef_Status_Label,CurationComment.CORRECT.toString());
        }
        result.put(SpecimenRecord.geoRef_Comment_Label,comment.getDetails());
        result.put(SpecimenRecord.geoRef_Source_Label,comment.getSource());
        SpecimenRecord r = new SpecimenRecord(result);

        broadcast(result);
    }
}
