package org.purl.jh.feat.profile.creg;

import org.purl.jh.feat.profiles.ErrorTagset;
import org.purl.jh.feat.profiles.ErrorTag;
import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.List;


// todo replace by two tagsets

/**
 *
 * @author Jirka dot Hana at gmail dot com
 */
enum ErrorSpecs {
    INSTANCE;

    public final ErrorTagset aTagset = new ErrorTagset("http://utkl.cuni.cz/czesl/L1/2", "L1 Error Tags V2");
    public final ErrorTagset bTagset = new ErrorTagset("http://utkl.cuni.cz/czesl/L2/2", "L2 Error Tags V2");;
    private final static int lineLen = 13;

    private ErrorSpecs() {
        readData();
    }

    private void readData() {
        List<String> data = data();

        Preconditions.checkArgument(data.size() % lineLen == 0, "Wrong format len=%d\n%s", data.size(), data);
        for (int i = 0; i < data.size(); i+=lineLen) {
            List<String> oneTag = data.subList(i, i+lineLen);
            process(oneTag);
        }

//        // todo incorproate into the file
//        aTagset.old2new.put("incorStem", "incorBase");
//        aTagset.old2new.put("incor:incorInfl", "incorInfl");
//        aTagset.old2new.put("incor:incorStem", "incorBase");
//        aTagset.old2new.put("incor:incorBase", "incorBase");
//        aTagset.old2new.put("fw:fwFab", "fwFab");
//        aTagset.old2new.put("fw:fwNc", "fwNc");
//        aTagset.old2new.put("wbd:wbdComp", "wbdComp");
//        aTagset.old2new.put("wbd:wbdPre", "wbdPre");
//        aTagset.old2new.put("wbd:wbdOther", "wbdOther");
//        aTagset.old2new.put("styl:stylColl", "stylColl");
//        bTagset.old2new.put("styl:stylColl", "stylColl");
//        aTagset.old2new.put("styl:stylMark", "stylMark");
//        bTagset.old2new.put("styl:stylMark", "stylMark");
//        aTagset.old2new.put("styl:stylOther", "stylOther");
//        bTagset.old2new.put("styl:stylOther", "stylOther");
//
//        //?
//        aTagset.old2new.put("odd:oddObj", "oddObj");        //?
//        aTagset.old2new.put("miss:missObj", "missObj");
//        aTagset.old2new.put("miss:missPred", "missPred");
                
    }


    private String tagId(String aTag, String aSubTag) {
            if (aTag.isEmpty()) {
                return aSubTag;
            }
            else if (aSubTag.isEmpty()) {
                return aTag;
            }
            else {
                return aTag+ ":" + aSubTag;
            }
    }

    private void process(List<String> strs) {
        try {
            String tagId = tagId(strs.get(0), strs.get(1));

            final ErrorTag tag = new ErrorTag(aTagset,
                tagId,
                strs.get(5),    // comment
                Boolean.parseBoolean(strs.get(2)),      // deprecated
                "A".equalsIgnoreCase(strs.get(3)),      // auto
                strs.get(4),    // menu
                Integer.parseInt(strs.get(6)),  // edges up min
                Integer.parseInt(strs.get(7)),
                Integer.parseInt(strs.get(8)),  // edges down min
                Integer.parseInt(strs.get(9)),
                Integer.parseInt(strs.get(10)), // error links min
                Integer.parseInt(strs.get(11))
                );
            
            switch (strs.get(12)) {
                case "R1,R2":
                    aTagset.add( tag );
                    bTagset.add( tag );
                    break;
                case "R1":
                    aTagset.add( tag );
                    break;
                case "R2":
                    bTagset.add( tag );
                    break;
                default:
                    Preconditions.checkArgument(false, "Unknown layer: %s", strs);
                    break;
            }
        }
        catch(Exception ex) {
            Preconditions.checkArgument(false, "Wrong format strs.len=%d, %s\n%s", strs.size(), strs, ex.toString());
            return;
        }
    }

    private List<String> data() {
        return Arrays.asList(
                
//"Tag","subtag","deprecated","manual/auto","menu","description","description","min lower leg","max upper leg","min upper leg","max upper leg","min links","max links","layers",
"Selection","Verb","","M","Selection - Verb","Selection - Verb","0","-1","0","-1","1","-1","R2",
"Selection","Prep","","M","Selection - Prep","Selection - Prep","0","-1","0","-1","0","-1","R2",
"Selection","Sentence","","M","Selection - Sentence","Selection - Sentence","0","-1","0","-1","0","-1","R2",
"Selection","Other","","M","Selection - Other","Selection - Other","0","-1","0","-1","0","-1","R2",
"Agreement","Subj-Verb","","M","Agreement - Subj-Verb","Agreement - Subj-Verb","0","-1","0","-1","0","-1","R2",
"Agreement","NP","","M","Agreement - NP","Agreement - NP","0","-1","0","-1","0","-1","R2",
"Agreement","Other","","M","Agreement - Other","Agreement - Other","0","-1","0","-1","0","-1","R2",
"Order","Finite Verb","","M","Order - Finite Verb","Order - Finite Verb","0","-1","0","-1","0","-1","R2",
"Order","Non-Finite Verb","","M","Order - Non-Finite Verb","Order - Non-Finite Verb","0","-1","0","-1","0","-1","R2",
"Order","Other","","M","Order - Other","Order - Other","0","-1","0","-1","0","-1","R2",
"Negation","","","M","Negation","Negation","0","-1","0","-1","0","-1","R2",
"Punctuation","Comma","","M","Punctuation - Comma","Punctuation - Comma","0","-1","0","-1","0","-1","R2",
"Punctuation","Sentence-Final","","M","Punctuation - Sentence-Final","Punctuation - Sentence-Final","0","-1","0","-1","0","-1","R2",
"Punctuation","Other","","M","Punctuation - Other","Punctuation - Other","0","-1","0","-1","0","-1","R2",
"Word","Capitalization","","M","Word - Capitalization","Word - Capitalization","0","-1","0","-1","0","-1","R1",
"Word","Stem/Inflection","","M","Word - Stem/Inflection","Word - Stem/Inflection","0","-1","0","-1","0","-1","R1",
"Word","Boundary","","M","Word - Boundary","Word - Boundary","0","-1","0","-1","0","-1","R1",
"Word","Non-Word","","M","Word - Non-Word","Word - Non-Word","0","-1","0","-1","0","-1","R1",
"Word","Other","","M","Word - Other","Word - Other","0","-1","0","-1","0","-1","R1",
"Lexicon/Style","","","M","Lexicon/Style","Lexicon/Style","0","-1","0","-1","0","-1","R1,R2",
"Question","","","M","Question","Question","0","-1","0","-1","0","-1","R1,R2",
"Reading Text","","","M","Reading Text","Reading Text","0","-1","0","-1","0","-1","R1,R2",
"Copied","Problematic","","M","Copied - Problematic","Copied - Problematic","0","-1","0","-1","0","-1","R2",
"Secondary","","","M","Secondary","","0","-1","0","-1","0","-1","R2",
"Problem","","","M","Problem/Other","Problem/Other","0","-1","0","-1","0","-1","R1,R2"
);
    }
    
////"Tag","subtag","deprecated","manual/auto","menu","description","min lower leg","max upper leg","min upper leg","max upper leg","min links","max links","layers"
//"S1a.1","","","M","S1a.1"," Verb - Complement - NP complement - incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S1a.2","","","M","S1a.2"," Verb - Complement - PP complement - incorrect preposition","0","-1","0","-1","0","-1","R1,R2",
//"S1a.3","","","M","S1a.3"," Verb - Complement - PP complement - incorrect case with correct preposition","0","-1","0","-1","0","-1","R1,R2",
//"S1a.4","","","M","S1a.4"," Verb - Complement - VP complement - haben/sein error","0","-1","0","-1","0","-1","R1,R2",
//"S1a.5","","","M","S1a.5"," Verb - Complement - VP complement - incorrect non-finite verb form","0","-1","0","-1","0","-1","R1,R2",
//"S1a.6","","","M","S1a.6"," Verb - Complement - Clausal complement - incorrect complementizer","0","-1","0","-1","0","-1","R1,R2",
//"S1a.7","","","M","S1a.7"," Verb - Complement - Incorrect correlative (not sure what I intended here)","0","-1","0","-1","0","-1","R1,R2",
//"S1a.8","","","M","S1a.8"," Verb - Complement - Incorrect complement type","0","-1","0","-1","0","-1","R1,R2",
//"S1a.8a","","","M","S1a.8a"," Verb - Complement - Incorrect complement type - With copula (i.e. attributive adjective)","0","-1","0","-1","0","-1","R1,R2",
//"S1a.9","","","M","S1a.9"," Verb - Complement - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S1a.10","","","M","S1a.10"," Verb - Complement - VP complement zu infinitive","0","-1","0","-1","0","-1","R1,R2",
//"S1a.10a","","","M","S1a.10a"," Verb - Complement - VP complement - zu missing","0","-1","0","-1","0","-1","R1,R2",
//"S1a.11","","","M","S1a.11"," Verb - Complement - Extra","0","-1","0","-1","0","-1","R1,R2",
//"S1a.12","","","M","S1a.12"," Verb - Complement - Complement should be adverbial (move to extra?)","0","-1","0","-1","0","-1","R1,R2",
//"S1a.13","","","M","S1a.13"," Verb - Complement - Incorrect PP or case in PP with verb of state/location","0","-1","0","-1","0","-1","R1,R2",
//"S1a.14","","","M","S1a.14"," Verb - Complement - Incorrect PP or case in PP with verb of motion","0","-1","0","-1","0","-1","R1,R2",
//"S1a.14a","","","M","S1a.14a"," Verb - Complement - Incorrect PP or case in PP with verb of motion - NP-dependent (nach Deutschland reisen in die Schweiz reisen)","0","-1","0","-1","0","-1","R1,R2",
//"S1a.15","","","M","S1a.15"," Verb - Complement - Da-preposition with clause - preposition without da-","0","-1","0","-1","0","-1","R1,R2",
//"S1a.16","","","M","S1a.16"," Verb - Complement - Clausal complement - missing complementizer","0","-1","0","-1","0","-1","R1,R2",
//"S1b","","","M","S1b"," Verb - Separable prefix - Impossible form","0","-1","0","-1","0","-1","R1,R2",
//"S1c.1","","","M","S1c.1"," Verb - Reflexive - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S1c.2","","","M","S1c.2"," Verb - Reflexive - Unnecessary/extra","0","-1","0","-1","0","-1","R1,R2",
//"S1c.3","","","M","S1c.3"," Verb - Reflexive - Incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S2a.1","","","M","S2a.1"," Preposition - Complement - Incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S2a.2","","","M","S2a.2"," Preposition - Complement - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S2b","","","M","S2b"," Preposition - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S2b.1","","","M","S2b.1"," Preposition - Missing in paired construction (von/bin)","0","-1","0","-1","0","-1","R1,R2",
//"S3a.1","","","M","S3a.1"," Noun - Determiner - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S3a.2","","","M","S3a.2"," Noun - Determiner - Extra (two determiners ``die die'')","0","-1","0","-1","0","-1","R1,R2",
//"S3a.3","","","M","S3a.3"," (not used)","0","-1","0","-1","0","-1","R1,R2",
//"S3a.4","","","M","S3a.4"," Noun - Partial (was für missing was)","0","-1","0","-1","0","-1","R1,R2",
//"S3a.5","","","M","S3a.5"," Noun - Pronoun instead of determiner (alles wem)","0","-1","0","-1","0","-1","R1,R2",
//"S3a.6","","","M","S3a.6"," Noun - Extra with job/description or with als","0","-1","0","-1","0","-1","R1,R2",
//"S3a.7","","","M","S3a.7"," Noun - Pronominalized determiner (eins) instead of determiner","0","-1","0","-1","0","-1","R1,R2",
//"S3b.1","","","M","S3b.1"," Noun - Complement - NP Complement - incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S3b.2","","","M","S3b.2"," Noun - Complement - PP complement - incorrect preposition","0","-1","0","-1","0","-1","R1,R2",
//"S3b.3","","","M","S3b.3"," Noun - Complement - PP complement - incorrect case with correct preposition","0","-1","0","-1","0","-1","R1,R2",
//"S3c.1","","","M","S3c.1"," Noun - Modifier - Genitive NP modifier - incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S4a.1","","","M","S4a.1"," Adjective - Complement - NP complement - incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S4a.2","","","M","S4a.2"," Adjective - Complement - PP complement - incorrect preposition","0","-1","0","-1","0","-1","R1,R2",
//"S4a.3","","","M","S4a.3"," Adjective - Complement - PP complement - incorrect case with correct preposition","0","-1","0","-1","0","-1","R1,R2",
//"S4a.4","","","M","S4a.4"," Adjective - Complement - Incorrect complement type","0","-1","0","-1","0","-1","R1,R2",
//"S4b.1","","","M","S4b.1"," Adjective - Comparative clause - Incorrect type (wie/als)","0","-1","0","-1","0","-1","R1,R2",
//"S4b.2a","","","M","S4b.2a"," Adjective - Comparative clause - Non-parallel after wie/als - Different phrase type","0","-1","0","-1","0","-1","R1,R2",
//"S4b.2b","","","M","S4b.2b"," Adjective - Comparative clause - Non-parallel after wie/als - Extra argument","0","-1","0","-1","0","-1","R1,R2",
//"S4b.2c","","","M","S4b.2c"," Adjective - Comparative clause - Non-parallel after wie/als - Incorrect case","0","-1","0","-1","0","-1","R1,R2",
//"S5a.1","","","M","S5a.1"," Sentence - Main clause - Missing","0","-1","0","-1","0","-1","R1,R2",
//"S5a.2","","","M","S5a.2"," Sentence - Main clause - Doubled coordinating conjunction","0","-1","0","-1","0","-1","R1,R2",
//"S5b.1","","","M","S5b.1"," Sentence - Finite verb - Missing in main clause","0","-1","0","-1","0","-1","R1,R2",
//"S5b.2","","","M","S5b.2"," Sentence - Finite verb - Missing in subordinate clause","0","-1","0","-1","0","-1","R1,R2",
//"S5b.3","","","M","S5b.3"," Sentence - Finite verb - Extra in main clause","0","-1","0","-1","0","-1","R1,R2",
//"S5b.4","","","M","S5b.4"," Sentence - Finite verb - Imperative form with subject corrected to indicative","0","-1","0","-1","0","-1","R1,R2",
//"S5b.5","","","M","S5b.5"," Sentence - Finite verb - Infinitive with subject (only with sein is it not 1st/3rd plural)","0","-1","0","-1","0","-1","R1,R2",
//"S5c.1","","","M","S5c.1"," Sentence - Target unclear - Main clause","0","-1","0","-1","0","-1","R1,R2",
//"S5c.2","","","M","S5c.2"," Sentence - Target unclear - Subordinate clause","0","-1","0","-1","0","-1","R1,R2",
//"S6a.1","","","M","S6a.1"," Conjunction - Coordinating - Missing conjunction","0","-1","0","-1","0","-1","R1,R2",
//"S6a.2","","","M","S6a.2"," Conjunction - Coordinating - Mismatched determiner or adjective","0","-1","0","-1","0","-1","R1,R2",
//"S6a.3","","","M","S6a.3"," Conjunction - Coordinating - Missing phrase in coordination","0","-1","0","-1","0","-1","R1,R2",
//"S6a.4","","","M","S6a.4"," Conjunction - Coordinating - Phrases not of the same type","0","-1","0","-1","0","-1","R1,R2",
//"S6b.1","","","M","S6b.1"," Conjunction - Subordinating - Incorrect tense","0","-1","0","-1","0","-1","R1,R2",
//"S7a","","","M","S7a"," Determiner/Inflected Adjective - Missing head noun","0","-1","0","-1","0","-1","R1,R2",
//"S7a.1","","","M","S7a.1"," Determiner/Inflected Adjective - Missing head noun - Adjective should be adverb","0","-1","0","-1","0","-1","R1,R2",
//"S7b","","","M","S7b"," Determiner/Inflected Adjective - Relative pronoun - No antecedent","0","-1","0","-1","0","-1","R1,R2",
//"S8a","","","M","S8a"," Titles - Missing last name","0","-1","0","-1","0","-1","R1,R2",
//"A1a","","","M","A1a"," Subject-Verb - Person","0","-1","0","-1","0","-1","R1,R2",
//"A1b","","","M","A1b"," Subject-Verb - Number","0","-1","0","-1","0","-1","R1,R2",
//"A2a","","","M","A2a"," Determiner-Adjective-Noun - Gender","0","-1","0","-1","0","-1","R1,R2",
//"A2a.1","","","M","A2a.1"," Determiner-Adjective-Noun - Gender - Of gendered noun","0","-1","0","-1","0","-1","R1,R2",
//"A2b","","","M","A2b"," Determiner-Adjective-Noun - Number","0","-1","0","-1","0","-1","R1,R2",
//"A2b.1","","","M","A2b.1"," Determiner-Adjective-Noun - Number - Singular count noun with plural modifier","0","-1","0","-1","0","-1","R1,R2",
//"A2b.2","","","M","A2b.2"," Determiner-Adjective-Noun - Number - Mass noun determiner with singular count noun (both in error)","0","-1","0","-1","0","-1","R1,R2",
//"A2b.3","","","M","A2b.3"," Determiner-Adjective-Noun - Number - Plural count noun determiner with singular mass noun (det in error)","0","-1","0","-1","0","-1","R1,R2",
//"A2b.4","","","M","A2b.4"," Determiner-Adjective-Noun - Number - Mass noun determiner with plural count coun (det in error)","0","-1","0","-1","0","-1","R1,R2",
//"A2b.5","","","M","A2b.5"," Determiner-Adjective-Noun - Number - ein with plural","0","-1","0","-1","0","-1","R1,R2",
//"A2c","","","M","A2c"," Determiner-Adjective-Noun - Case","0","-1","0","-1","0","-1","R1,R2",
//"A2d","","","M","A2d"," Determiner-Adjective-Noun - Definiteness","0","-1","0","-1","0","-1","R1,R2",
//"A2e","","","M","A2e"," Determiner-Adjective-Noun - Attributeness (predicative adjective in attribute position)","0","-1","0","-1","0","-1","R1,R2",
//"A3a","","","M","A3a"," Relative Pronoun-Antecedent - Gender","0","-1","0","-1","0","-1","R1,R2",
//"A3b","","","M","A3b"," Relative Pronoun-Antecedent - Number","0","-1","0","-1","0","-1","R1,R2",
//"A3c","","","M","A3c"," Relative Pronoun-Antecedent - Case (not dessen/deren used as determiner) (this doesn't make sense should be somewhere else)","0","-1","0","-1","0","-1","R1,R2",
//"A4a","","","M","A4a"," Subject-Predicate with Copula - Number","0","-1","0","-1","0","-1","R1,R2",
//"A5a","","","M","A5a"," Negatives - Double negative","0","-1","0","-1","0","-1","R1,R2",
//"A5b","","","M","A5b"," Negatives - Incompatible (schon + noch nicht)","0","-1","0","-1","0","-1","R1,R2",
//"A6","","","M","A6"," Reflextive","0","-1","0","-1","0","-1","R1,R2",
//"A7a","","","M","A7a"," Appositives - Gender","0","-1","0","-1","0","-1","R1,R2",
//"A7b","","","M","A7b"," Appositives - Number","0","-1","0","-1","0","-1","R1,R2",
//"A7c","","","M","A7c"," Appositives - Case","0","-1","0","-1","0","-1","R1,R2",
//"O1a","","","M","O1a"," Finite Verb - In a main clause","0","-1","0","-1","0","-1","R1,R2",
//"O1b","","","M","O1b"," Finite Verb - In a subordinate clause","0","-1","0","-1","0","-1","R1,R2",
//"O2","","","M","O2"," Non-finite verb","0","-1","0","-1","0","-1","R1,R2",
//"O2a","","","M","O2a"," Non-finite verb - Reordered outside clause","0","-1","0","-1","0","-1","R1,R2",
//"O3a","","","M","O3a"," Separable Prefix - Non-separable prefix treated as separable prefix (should be WF error?)","0","-1","0","-1","0","-1","R1,R2",
//"O4a","","","M","O4a"," Mittelfeld - Arguments (reflexive counts as argument)","0","-1","0","-1","0","-1","R1,R2",
//"O4b","","","M","O4b"," Mittelfeld - Adverbials","0","-1","0","-1","0","-1","R1,R2",
//"O5","","","M","O5"," Prepositional Phrase","0","-1","0","-1","0","-1","R1,R2",
//"O5a","","","M","O5a"," Prepositional Phrase - Was für","0","-1","0","-1","0","-1","R1,R2",
//"O6","","","M","O6"," Noun Phrase","0","-1","0","-1","0","-1","R1,R2",
//"O6a","","","M","O6a"," Noun Phrase - Kein ... mehr","0","-1","0","-1","0","-1","R1,R2",
//"O7","","","M","O7"," Adverb Phrase","0","-1","0","-1","0","-1","R1,R2",
//"O7a","","","M","O7a"," Adverb Phrase - Noch nicht","0","-1","0","-1","0","-1","R1,R2",
//"O8a","","","M","O8a"," Vorfeld - Contains interrogative pronoun in question","0","-1","0","-1","0","-1","R1,R2",
//"P1a.1","","","M","P1a.1"," Comma - Missing - Between coordinated elements","0","-1","0","-1","0","-1","R1,R2",
//"P1a.2","","","M","P1a.2"," Comma - Missing - Between main and subordinate clauses or other clauses where a comma is required (not with und and oder)","0","-1","0","-1","0","-1","R1,R2",
//"P1a.3","","","M","P1a.3"," Comma - Missing - Other","0","-1","0","-1","0","-1","R1,R2",
//"P1b.1","","","M","P1b.1"," Comma - Extra - Between coordinated elements","0","-1","0","-1","0","-1","R1,R2",
//"P1b.2","","","M","P1b.2"," Comma - Extra - Other","0","-1","0","-1","0","-1","R1,R2",
//"P2a","","","M","P2a"," Other Punctuation - Missing","0","-1","0","-1","0","-1","R1,R2",
//"P2b","","","M","P2b"," Other Punctuation - Extra","0","-1","0","-1","0","-1","R1,R2",
//"P3a","","","M","P3a"," Sentence Final Punctuation - Incorrect (go by word order/question words)","0","-1","0","-1","0","-1","R1,R2",
//"P3b","","","M","P3b"," Sentence Final Punctuation - Missing (target chosen by word order)","0","-1","0","-1","0","-1","R1,R2",
//"P3c","","","M","P3c"," Sentence Final Punctuation - Extra","0","-1","0","-1","0","-1","R1,R2",
//"W1","","","M","W1","Capitalization","0","-1","0","-1","0","-1","R1,R2",
//"W2","","","M","W2","Inflection","0","-1","0","-1","0","-1","R1,R2",
//"W3","","","M","W3","Other","0","-1","0","-1","0","-1","R1,R2",
//"W4","","","M","W4","Single-letter spelling error with different POS","0","-1","0","-1","0","-1","R1,R2",
//"W5","","","M","W5","Collocation","0","-1","0","-1","0","-1","R1,R2",
//"W6","","","M","W6","Dictionary errors","0","-1","0","-1","0","-1","R1,R2",
//"W7","","","M","W7","Incomplete phrases","0","-1","0","-1","0","-1","R2"
//                );
    
    private List<String> dataCzesl() {
        return Arrays.asList(
//"Tag",	"subtag",	"deprecated",	"manual/auto",	"menu",	"description",	"min lower leg",	"max upper leg",	"min upper leg", 	"max upper leg",	"min links",	"max links",	"layers",
"incor",	"",	"true",	"M",	"incor",	"nesprávný tvar (obecná kategorie pro neanotované případy oprav)",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"incorInfl",	"",	"M",	"incor&Infl",	"nesprávná flexe",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"incorStem",	"true",	"M",	"incorStem",	"nesprávný kmen",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"incorBase",	"",	"M",	"incor&Base",	"nesprávný kmen",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"incorOther",	"",	"A",	"incorOther",	"ostatní nesprávné tvary",	"1",	"-1",	"1",	"-1",	"0",	"0",	"R1",
"fw",	"",	"true",	"M",	"fw",	"neemendovatelné slovo",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"fwFab",	"",	"M",	"fwFab",	"neemendovatelné slovo",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"",	"fwNc",	"",	"M",	"fwNc",	"slovo z jiného jazyka",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"flex",	"",	"",	"M",	"flex",	"flexe u výrazu fw",	"1",	"1",	"1",	"1",	"0",	"0",	"R1",
"wbd",	"",	"true",	"M",	"wbd",	"chybná hranice slov ",	"1",	"-1",	"1",	"-1",	"0",	"0",	"R1",
"",	"wbdPre",	"",	"M",	"wbdPre",	"prefix oddělený mezerou a předložka bez mezery ",	"1",	"2",	"1",	"2",	"0",	"0",	"R1",
"",	"wbdComp",	"",	"M",	"wbdComp",	"neoprávněně rozdělená kompozita",	"2",	"-1",	"1",	"1",	"0",	"0",	"R1",
"",	"wbdOther",	"",	"M",	"wbdOther",	"jiná chyba týkající se hranice slova",	"1",	"-1",	"1",	"-1",	"0",	"0",	"R1",
"agr",	"",	"",	"M",	"agr",	"narušení shody",	"1",	"1",	"1",	"1",	"0",	"-1",	"R2",
"dep",	"",	"",	"M",	"dep",	"chyba ve vyjádření syntaktické závislosti",	"0",	"1",	"0",	"1",	"0",	"-1",	"R2",
"ref",	"",	"",	"M",	"ref",	"chyba v zájmenném odkazu",	"1",	"1",	"1",	"1",	"0",	"1",	"R2",
"vbx",	"",	"",	"M",	"vbx",	"chyba v analytickém slovesném tvaru a složeném přísudku",	"0",	"-1",	"0",	"-1",	"0",	"1",	"R2",
"",	"cvf",	"",	"A",	"cvf",	"chyba v analytickém slovesném tvaru",	"1",	"-1",	"1",	"-1",	"0",	"1",	"R2",
"",	"mod",	"",	"A",	"mod",	"chyba v konstrukci s modálním nebo fázovým slovesem",	"1",	"-1",	"1",	"-1",	"0",	"1",	"R2",
"",	"vnp",	"",	"A",	"vnp",	"chyba ve sponově-jmenném přísudku (vč pas a rez)",	"1",	"-1",	"1",	"-1",	"0",	"1",	"R2",
"rflx",	"",	"",	"M",	"rflx",	"chyba v reflexivním výrazu",	"0",	"-1",	"0",	"-1",	"0",	"-1",	"R2",
"neg",	"",	"",	"M",	"neg",	"chyba v negaci",	"1",	"-1",	"1",	"-1",	"0",	"1",	"R2",
"odd",	"",	"",	"A",	"odd",	"nadbytečné slovo",	"1",	"1",	"0",	"0",	"0",	"0",	"R2",
"miss",	"",	"",	"A",	"miss",	"chybějící slovo",	"0",	"0",	"1",	"1",	"0",	"0",	"R2",
"wo",	"",	"",	"A",	"wo",	"chybný slovosled",	"1",	"-1",	"1",	"-1",	"0",	"0",	"R2",
"lex",	"",	"",	"M",	"lex",	"chyba v lexiku a frazeologii",	"0",	"-1",	"0",	"-1",	"0",	"1",	"R2",
"use",	"",	"",	"M",	"use",	"chyba v užití gramatické kategorie",	"1",	"-1",	"1",	"-1",	"0",	"1",	"R2",
"sec",	"",	"",	"M",	"sec",	"sekundární, \"zavlečená\" chyba",	"1",	"-1",	"1",	"-1",	"0",	"-1",	"R2",
"styl",	"",	"true",	"M",	"styl",	"obecněčeský,	knižní,	nářeční tvar",	"0",	"-1",	"0",	"-1",	"0",	"-1",	"R1,R2",
"",	"stylColl",	"",	"M",	"stylColl",	"potenciální obecněčeský tvar",	"0",	"-1",	"0",	"-1",	"0",	"-1",	"R1,R2",
"",	"stylMark",	"",	"M",	"stylMark",	"výplňkové slovo",	"1",	"-1",	"0",	"0",	"0",	"0",	"R2",
"",	"stylOther",	"",	"M",	"stylOther",	"knižní,	nářeční,	slangový ap tvar/výraz",	"0",	"-1",	"0",	"-1",	"0",	"-1",	"R1,R2",
"disr",	"",	"",	"M",	"disr",	"rozvrácená konstrukce",	"-1",	"-1",	"0",	"-1",	"0",	"0",	"R2",
"problem",	"",	"",	"M",	"problem",	"problémová chyba",	"0",	"-1",	"0",	"-1",	"0",	"-1",	"R1,R2"
                );
    }

}
