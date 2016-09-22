package org.kurator.validation.actors;

/*
 * This code is an adaptation of akka.fp.NewScientificNameValidator
 * in the FP-Akka package as of 28Oct2014.
 */


import org.filteredpush.kuration.services.sciname.*;
import org.filteredpush.kuration.util.*;
import org.gbif.api.model.checklistbank.ParsedName;
import org.gbif.nameparser.NameParser;
import org.gbif.nameparser.UnparsableException;
import org.kurator.akka.KuratorActor;
import org.filteredpush.kuration.interfaces.INewScientificNameValidationService;

import java.io.IOException;

public class ScientificNameValidator extends KuratorActor {

    public String serviceClassQN = "org.filteredpush.kuration.services.sciname.COLService";
    public boolean insertLSID = true;
    public boolean insertGUID = true;

    public String authorityName;
    public boolean taxonomicMode = false;

    private String scientificNameLabel;
    private String authorLabel;
    private String LSIDLabel;

    private INewScientificNameValidationService scientificNameService;

    @Override
	public void onInitialize() {
        //initialize required label
        SpecimenRecordTypeConf specimenRecordTypeConf = SpecimenRecordTypeConf.getInstance();

        try {
            scientificNameLabel = specimenRecordTypeConf.getLabel("ScientificName");
            if(scientificNameLabel == null){
                scientificNameLabel = SpecimenRecord.dwc_scientificName;
                //throw new CurrationException(" failed since the ScientificName label of the SpecimenRecordType is not set.");
            }

            authorLabel = specimenRecordTypeConf.getLabel("ScientificNameAuthorship");
            if(authorLabel == null){
                authorLabel = SpecimenRecord.dwc_scientificNameAuthorship;
                //throw new CurrationException("failed since the ScientificNameAuthorship label of the SpecimenRecordType is not set.");
            }

            if(insertGUID){
                LSIDLabel = specimenRecordTypeConf.getLabel("TaxonID");
                if(LSIDLabel == null){
                    LSIDLabel = "IdentificationTaxon";
                    //throw new CurrationException(" failed since the IdentificationTaxon label of the SpecimenRecordType is not set.");
                }
            }

            //scientificNameService = (INewScientificNameValidationService)Class.forName(serviceClassQN).newInstance();
            //use the authority argument to select which service to use
            if (authorityName==null) { authorityName = "GBIF"; }
            switch(authorityName.toUpperCase()) {
                case "IF":
                case "INDEXFUNGORUM":
                    scientificNameService = new IndexFungorumService();
                    break;
                case "WORMS":
                    scientificNameService = new WoRMSService();
                    break;
                case "COL":
                    scientificNameService = new COLService();
                    break;
                case "IPNI":
                    scientificNameService = new IPNIService();
                    break;
                case "GBIF":
                default:
                    if (!authorityName.toUpperCase().equals("GBIF")) {
                        System.err.println("Unrecognized service (" + authorityName + ") or service not specified, using GBIF.");
                    }
                    scientificNameService = new GBIFService();
            }

            //set validation mode
            if(!taxonomicMode) scientificNameService.setValidationMode(INewScientificNameValidationService.MODE_NOMENCLATURAL);
            else scientificNameService.setValidationMode(INewScientificNameValidationService.MODE_TAXONOMIC);

            //} catch (CurationException e) {
             //   e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

    }

    @Override
	public void onData(Object value) throws Exception {
        if (value instanceof SpecimenRecord) {
            SpecimenRecord inputSpecimenRecord = (SpecimenRecord) value;
            String scientificName = inputSpecimenRecord.get(scientificNameLabel);
            //System.out.println("scientificName = " + scientificName);
            if(scientificName == null){
                CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, scientificNameLabel + " is missing in the incoming SpecimenRecord", "ScientificNameValidator");
                updateAndSendRecord(new SpecimenRecord(inputSpecimenRecord),curationComment);
                return;
            }

            String author = inputSpecimenRecord.get(authorLabel);
            //System.out.println("author = " + author);
                    /*if(author == null){
                        CurationCommentType curationComment = CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY,authorLabel+" is missing in the incoming SpecimenRecord","ScientificNameValidator");
                        constructOutput(new SpecimenRecord(inputSpecimenRecord),curationComment);
                        return;
                    }  */
            // TODO: Need an actor to extract scientific name authorships from scientific name when authorship is not provided separately
            if(author == null){
                NameParser parser = new NameParser();
                try {
                    ParsedName parse = parser.parse(scientificName);
                    author = parse.getAuthorship();
                } catch (UnparsableException e) {
                    CurationComment.construct(CurationComment.UNABLE_DETERMINE_VALIDITY, e.getMessage(), "ScientificNameValidator");
                }
            }

            String genus = inputSpecimenRecord.get("genus");
            String subgenus = inputSpecimenRecord.get("subgenus");
            String specificEpithet = inputSpecimenRecord.get("specificEpithet");
            String verbatimTaxonRank = inputSpecimenRecord.get("verbatimTaxonRank");
            String infraspecificEpithet = inputSpecimenRecord.get("infraspecificEpithet");
            String taxonRank = inputSpecimenRecord.get("taxonRank");
            String kingdom = inputSpecimenRecord.get("kingdom");
            String phylum = inputSpecimenRecord.get("phylum");
            String tclass = inputSpecimenRecord.get("tclass");
            String order = inputSpecimenRecord.get("order");
            String family = inputSpecimenRecord.get("family");
            String genericEpithet = "";


                    /*
                    System.out.println("taxonRank = " + taxonRank);
                    System.out.println("infraspecificEpithet = " + infraspecificEpithet);
                    System.out.println("verbatimTaxonRank = " + verbatimTaxonRank);
                    System.out.println("specificEpithet = " + specificEpithet);
                    System.out.println("subgenus = " + subgenus);
                    System.out.println("genus = " + genus);
                     */
            try {
                scientificNameService.validateScientificName( scientificName, author, genus, subgenus,specificEpithet, verbatimTaxonRank, infraspecificEpithet, taxonRank, kingdom, phylum, tclass, order, family, genericEpithet);
            } catch (Exception e) {
                e.printStackTrace();
            }

            CurationStatus curationStatus = scientificNameService.getCurationStatus();

            if (curationStatus == CurationComment.CORRECT) {
                // If a blank has been filled in from atomic fields and is asserted as correct, put it inot the output.
                if (inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName).trim().length()==0) {
                    String originalSciName = "";
                    String newSciName = scientificNameService.getCorrectedScientificName();
                    inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, originalSciName);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                }
            }

            if(curationStatus == CurationComment.CURATED || curationStatus == CurationComment.FILLED_IN){
                //put in original value first
                String originalSciName =  inputSpecimenRecord.get(SpecimenRecord.dwc_scientificName);
                String originalAuthor = inputSpecimenRecord.get(SpecimenRecord.dwc_scientificNameAuthorship);
                String newSciName = scientificNameService.getCorrectedScientificName();
                String newAuthor = scientificNameService.getCorrectedAuthor();

                if(originalSciName != null && originalSciName.length() != 0 &&  !originalSciName.equals(newSciName)){
                    inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, originalSciName);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                } else if (originalSciName==null || originalSciName.trim().length()==0) {
                    inputSpecimenRecord.put(SpecimenRecord.Original_SciName_Label, "");
                    inputSpecimenRecord.put(SpecimenRecord.dwc_scientificName, newSciName);
                }
                if(originalAuthor != null && !originalAuthor.equals(newAuthor)){
                    inputSpecimenRecord.put(SpecimenRecord.Original_Authorship_Label, originalAuthor);
                    inputSpecimenRecord.put(SpecimenRecord.dwc_scientificNameAuthorship, newAuthor);
                }
            }
            // add a GUID one was returned
            if(!scientificNameService.getGUID().equals("")) {
                // TODO: We should be able to handle scientificNameID and acceptedNameUsageID
                inputSpecimenRecord.put("taxonID", scientificNameService.getGUID());

            }

            StringBuffer higherFillInComment = new StringBuffer();
            // if higher taxonomy is absent, fill it in
            if (inputSpecimenRecord.get(SpecimenRecord.dwc_kingdom)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_kingdom).trim().length()==0) {
                if (scientificNameService.getCorrectedKingdom()!=null && scientificNameService.getCorrectedKingdom().trim().length() >0) {
                    inputSpecimenRecord.put(SpecimenRecord.dwc_kingdom, scientificNameService.getCorrectedKingdom());
                    higherFillInComment.append(" | Filled In Kingdom ");
                }
            }
            if (inputSpecimenRecord.get(SpecimenRecord.dwc_phylum)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_phylum).trim().length()==0) {
                if (scientificNameService.getCorrectedPhylum()!=null && scientificNameService.getCorrectedPhylum().trim().length() >0) {
                    inputSpecimenRecord.put(SpecimenRecord.dwc_phylum, scientificNameService.getCorrectedPhylum());
                    higherFillInComment.append(" | Filled In Phylum ");
                }
            }
            if (inputSpecimenRecord.get(SpecimenRecord.dwc_class)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_class).trim().length()==0) {
                if (scientificNameService.getCorrectedClass()!=null && scientificNameService.getCorrectedClass().trim().length() >0) {
                    inputSpecimenRecord.put(SpecimenRecord.dwc_class, scientificNameService.getCorrectedClass());
                    higherFillInComment.append(" | Filled In Class ");
                }
            }
            if (inputSpecimenRecord.get(SpecimenRecord.dwc_order)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_order).trim().length()==0) {
                if (scientificNameService.getCorrectedOrder()!=null && scientificNameService.getCorrectedOrder().trim().length() >0) {
                    inputSpecimenRecord.put(SpecimenRecord.dwc_order, scientificNameService.getCorrectedOrder());
                    higherFillInComment.append(" | Filled In Order ");
                }
            }
            if (inputSpecimenRecord.get(SpecimenRecord.dwc_family)==null || inputSpecimenRecord.get(SpecimenRecord.dwc_family).trim().length()==0) {
                if (scientificNameService.getCorrectedFamily()!=null && scientificNameService.getCorrectedFamily().trim().length() >0) {
                    inputSpecimenRecord.put(SpecimenRecord.dwc_family, scientificNameService.getCorrectedFamily());
                    higherFillInComment.append(" | Filled In Family ");
                }
            }

            //output
            CurationCommentType curationComment = CurationComment.construct(curationStatus,scientificNameService.getComment().concat(higherFillInComment.toString()),scientificNameService.getServiceName());

            updateAndSendRecord(inputSpecimenRecord, curationComment);
        }
    }


    private void updateAndSendRecord(SpecimenRecord result, CurationCommentType comment) {

        if(comment!=null){
            result.put(SpecimenRecord.SciName_Comment_Label, comment.getDetails());
            result.put(SpecimenRecord.SciName_Status_Label, comment.getStatus());
            result.put(SpecimenRecord.SciName_Source_Label, comment.getSource());
        }

        broadcast(result);
    }


}

