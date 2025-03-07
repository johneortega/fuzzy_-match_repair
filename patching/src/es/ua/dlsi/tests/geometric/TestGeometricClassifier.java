/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package es.ua.dlsi.tests.geometric;

import es.ua.dlsi.recommendation.GeometricRecommender;
import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.translationmemory.TranslationMemory;
import es.ua.dlsi.translationmemory.TranslationUnit;
import es.ua.dlsi.utils.CmdLineParser;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author miquel
 */
public class TestGeometricClassifier {
    public static void main(String[] args) {
        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option otestsource = parser.addStringOption("test-source");
        CmdLineParser.Option otesttarget = parser.addStringOption("test-target");
        CmdLineParser.Option ooutput = parser.addStringOption('o',"output");
        CmdLineParser.Option othreshold = parser.addDoubleOption('t',"threshold");
        CmdLineParser.Option odecisionthreshold = parser.addDoubleOption("decision-threshold");
        CmdLineParser.Option oclassification = parser.addStringOption('c',"classification");
        CmdLineParser.Option oqualitythresholdfms = parser.addDoubleOption("quality-threshold-fms");
        CmdLineParser.Option oqualitythresholdeditdist = parser.addDoubleOption("quality-threshold-editdist");
        CmdLineParser.Option otmpath = parser.addStringOption("tm-path");
        CmdLineParser.Option odebug = parser.addStringOption('d',"debug");
        CmdLineParser.Option omaxseglen = parser.addIntegerOption('m',"max-segment-len");

        try{
            parser.parse(args);
        }
        catch(CmdLineParser.IllegalOptionValueException e){
            System.err.println(e);
            System.exit(-1);
        }
        catch(CmdLineParser.UnknownOptionException e){
            System.err.println(e);
            System.exit(-1);
        }

        Double qualitythresholdfms=(Double)parser.getOptionValue(oqualitythresholdfms,null);
        Double qualitythresholdeditdist=(Double)parser.getOptionValue(oqualitythresholdeditdist,null);
        String testsource=(String)parser.getOptionValue(otestsource,null);
        String testtarget=(String)parser.getOptionValue(otesttarget,null);
        Double threshold=(Double)parser.getOptionValue(othreshold,null);
        double decisionthreshold=(Double)parser.getOptionValue(odecisionthreshold,0.5);
        String output=(String)parser.getOptionValue(ooutput,null);
        String tmpath=(String)parser.getOptionValue(otmpath,null);
        String debugpath=(String)parser.getOptionValue(odebug,null);
        String classification=(String)parser.getOptionValue(oclassification,null);
        boolean debug=(debugpath!=null);
        int maxseglen=(Integer)parser.getOptionValue(omaxseglen,-1);

        if(threshold==null){
            System.err.println("Error: It is necessary to set a maximum lenght higher than 0 (use parameter --max-segment-len).");
            System.exit(-1);
        }
        if(tmpath==null){
            System.err.println("Error: It is necessary to define the path of the file containing the translation memory java object(use parameter --tm-path).");
            System.exit(-1);
        }
        if(output==null){
            System.err.println("Error: It is necessary to define the path of the file where the features will be writen (use parameter --feature-output).");
            System.exit(-1);
        }
        if(testsource==null){
            System.err.println("Error: It is needed to define the path to the source language segments of the test set (use parameter --test-source).");
            System.exit(-1);
        }
        if(testtarget==null){
            System.err.println("Error: It is needed to define the path to the target language segments of the test set (use parameter --test-source).");
            System.exit(-1);
        }

        TranslationMemory trans_memory=new TranslationMemory();
        trans_memory.LoadTMFromObject(tmpath);

        List<Segment> stestsegs=new LinkedList<Segment>();
        try {
            stestsegs=TranslationMemory.ReadSegmentsFile(testsource);
        } catch (FileNotFoundException ex) {
            System.err.print("Error: Source language test segments file '");
            System.err.print(testsource);
            System.err.println("' could not be found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.print("Error while reading source language test segments from file '");
            System.err.print(testsource);
            System.err.println("'");
            System.exit(-1);
        }

        List<Segment> ttestsegs=new LinkedList<Segment>();
        try {
            ttestsegs=TranslationMemory.ReadSegmentsFile(testtarget);
        } catch (FileNotFoundException ex) {
            System.err.print("Error: Target language test segments file '");
            System.err.print(testsource);
            System.err.println("' could not be found.");
            System.exit(-1);
        } catch (IOException ex) {
            System.err.print("Error while reading target language test segments from file '");
            System.err.print(testsource);
            System.err.println("'");
            System.exit(-1);
        }

        /*double coverage;
        double accuracy;
        double keepprecission;
        double changeprecission;
        double keeprecall;
        double changerecall;*/
        int keepcorrect, changecorrect, keepwrong, changewrong, keepunrecommended, changeunrecommended;
        keepcorrect=changecorrect=keepwrong=changewrong=keepunrecommended=changeunrecommended=0;

        PrintWriter classpw=null;
        if(classification!=null){
            try{
                //classpw=new PrintWriter(classification);
                classpw=new PrintWriter(new GZIPOutputStream(new FileOutputStream(classification)));
            } catch(FileNotFoundException ex){
                System.err.println("Warning: Output file "+classification+" could not be found: the results will be printed in the default output.");
            } catch(IOException ex){
                System.err.println("Warning: Error while writting file "+classification+": the results will be printed in the default output.");
            }
        }

        for(int i=0;i<stestsegs.size();i++){
            System.err.print("Testing segment ");
            System.err.print(i);
            System.err.print("\r");
            for(TranslationUnit tu: trans_memory.GetTUs()){
                double score=TranslationMemory.GetScore(stestsegs.get(i),
                        tu.getSource(), threshold, null);
                if(score>=threshold){
                    if(debug){
                        System.out.println("SENTENCES MATCHING:\n\ts': "
                                +stestsegs.get(i)+"\n\tt': "+ttestsegs.get(i)
                                +"\n\ts_i: "+tu.getSource()+"\n\tt_i: "+tu.getTarget());
                    }
                    boolean[] talignment=new boolean[tu.getTarget().size()];
                    double editdist=Segment.EditDistance(ttestsegs.get(i).getSentenceCodes(),
                            tu.getTarget().getSentenceCodes(), talignment, debug);
                    boolean acceptable_quality=true;
                    if(qualitythresholdeditdist!=null){
                        double seditdist=(((1-score)*Math.max(stestsegs.get(i).size(),
                                tu.getSource().size())));
                        double ratio_editions;
                        if(seditdist==0){
                            if(editdist==0) {
                                ratio_editions=1;
                            }
                            else {
                                ratio_editions=Double.POSITIVE_INFINITY;
                            }
                        }
                        else{
                            ratio_editions=editdist/seditdist;
                        }
                        if(ratio_editions<(1/qualitythresholdeditdist) ||
                                ratio_editions>qualitythresholdeditdist){
                            acceptable_quality=false;
                        }
                    }
                    if(acceptable_quality && qualitythresholdfms!=null){
                        double tscore=1-(editdist/Math.max(ttestsegs.get(i).size(),
                                tu.getTarget().getSentenceCodes().size()));
                        double diff_fms=Math.abs(tscore-score);
                        if(diff_fms>=qualitythresholdfms){
                            acceptable_quality=false;
                        }
                    }
                    if(acceptable_quality){
                        int[] recommendations=GeometricRecommender.MakeRecommendation(stestsegs.get(i),
                                tu, maxseglen,debug,null,talignment, decisionthreshold);
                        for(int r=0;r<recommendations.length;r++){
                            if(recommendations[r]==0){
                                if(talignment[r]) {
                                    keepunrecommended++;
                                }
                                else {
                                    changeunrecommended++;
                                }
                            }
                            else{
                                if(recommendations[r]==1){
                                    if(talignment[r]) {
                                        keepcorrect++;
                                        if(classpw!=null){
                                            classpw.println("1 1");
                                        }
                                    }
                                    else {
                                        keepwrong++;
                                        if(classpw!=null){
                                            classpw.println("1 0");
                                        }
                                    }
                                }
                                else{
                                    if(talignment[r]) {
                                        changewrong++;
                                        if(classpw!=null){
                                            classpw.println("0 0");
                                        }
                                    }
                                    else {
                                        changecorrect++;
                                        if(classpw!=null){
                                            classpw.println("0 1");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        System.err.println();

        if(classpw!=null)
            classpw.close();
        //Printing results
        PrintWriter pw;
        try{
            pw=new PrintWriter(output);
        } catch(FileNotFoundException ex){
            System.err.println("Warning: Output file "+output+" could not be found: the results will be printed in the default output.");
            pw=new PrintWriter(System.out);
        }
        int totalwords=keepunrecommended+changeunrecommended+changecorrect+changewrong+keepcorrect+keepwrong;
        int totalclassified=changecorrect+changewrong+keepcorrect+keepwrong;
        int totalunclassified=keepunrecommended+changeunrecommended;
        int totalcorrect=keepcorrect+changecorrect;
        //int totalwrong=keepwrong+changewrong;
        double keepprecision=(double)keepcorrect/(keepcorrect+keepwrong);
        double changeprecision=(double)changecorrect/(changecorrect+changewrong);
        double keeprecall=(double)keepcorrect/(keepcorrect+changewrong);
        double changerecall=(double)changecorrect/(changecorrect+keepwrong);
        double keepfmeasure=2.0*keepprecision*keeprecall/(keepprecision+keeprecall);
        double changefmeasure=2.0*changeprecision*changerecall/(changeprecision+changerecall);
        double accuracy=(double)(totalcorrect)/(totalclassified);
        double unrecomendedpercent=(double)(totalunclassified)/totalwords;

        double keepprecisionconfinterval=1.96*Math.sqrt(keepprecision*(1-keepprecision)/totalclassified);
        double changeprecisionconfinterval=1.96*Math.sqrt(changeprecision*(1-changeprecision)/totalclassified);
        
        double keeprecallconfinterval=1.96*Math.sqrt(keeprecall*(1-keeprecall)/totalclassified);
        double changerecallconfinterval=1.96*Math.sqrt(changerecall*(1-changerecall)/totalclassified);
        
        double keepfmeasureconfinterval=(2*Math.pow(keepprecision,2)/
                (keepprecision+keeprecall))*keeprecallconfinterval+
                (2*Math.pow(keeprecall,2)/(keepprecision+keeprecall))*keepprecisionconfinterval;

        double changefmeasureconfinterval=(2*Math.pow(changeprecision,2)/
                (changeprecision+changerecall))*changerecallconfinterval+
                (2*Math.pow(changerecall,2)/(changeprecision+changerecall))*changeprecisionconfinterval;

        double accuracyconfinterval=1.96*Math.sqrt(accuracy*(1-accuracy)/totalclassified);

        double unrecomendedpercentconfinterval=1.96*Math.sqrt(unrecomendedpercent*(1-unrecomendedpercent)/totalwords);

        DecimalFormat df = new DecimalFormat("###.###");
        pw.println("TEST RESULTS:");
        pw.print("Correctly classified as keep: ");
        pw.print(keepcorrect);
        pw.print(" of ");
        pw.println(totalwords);
        pw.print("Correctly classified as change: ");
        pw.print(changecorrect);
        pw.print(" of ");
        pw.println(totalwords);
        pw.print("Precission in keep recommendations: ");
        pw.print(df.format(keepprecision*100.0));
        pw.print("±");
        pw.print(df.format(keepprecisionconfinterval*100.0));
        pw.println("%");
        pw.print("Precission in change recommendations: ");
        pw.print(df.format(changeprecision*100.0));
        pw.print("±");
        pw.print(df.format(changeprecisionconfinterval*100.0));
        pw.println("%");
        pw.print("Recall in keep recommendations: ");
        pw.print(df.format(keeprecall*100.0));
        pw.print("±");
        pw.print(df.format(keeprecallconfinterval*100.0));
        pw.println("%");
        pw.print("Recall in change recommendations: ");
        pw.print(df.format(changerecall*100.0));
        pw.print("±");
        pw.print(df.format(changerecallconfinterval*100.0));
        pw.println("%");
        pw.print("F-measure in keep recommendations: ");
        pw.print(df.format(keepfmeasure*100.0));
        pw.print("±");
        pw.print(df.format(keepfmeasureconfinterval*100.0));
        pw.println("%");
        pw.print("F-measure in change recommendations: ");
        pw.print(df.format(changefmeasure*100.0));
        pw.print("±");
        pw.print(df.format(changefmeasureconfinterval*100.0));
        pw.println("%");
        pw.print("Accuracy: ");
        pw.print(df.format(accuracy*100.0));
        pw.print("±");
        pw.print(df.format(accuracyconfinterval*100.0));
        pw.println("%");
        pw.print("Percentage of words without any recommendation: ");
        pw.print(df.format(unrecomendedpercent*100.0));
        pw.print("±");
        pw.print(df.format(unrecomendedpercentconfinterval*100.0));
        pw.println("%");
        pw.close();
    }
}
