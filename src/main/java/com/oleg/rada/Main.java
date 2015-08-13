package com.oleg.rada;

import com.oleg.rada.persistance.*;
import com.oleg.rada.persistance.rep.*;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.IOException;
import java.util.*;


public class Main {
    static RadaHtmlParser parser = new RadaHtmlParser();

    static ApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
    static LawRepository lawRepository  = context.getBean(LawRepository.class);
    static MPRepository mpRepository = context.getBean(MPRepository.class);
    static RadaSessionDayRepository  radaSessionDayRepository = context.getBean(RadaSessionDayRepository.class);
    static VoteRepository  voteRepository = context.getBean(VoteRepository.class);
    static VoteResultPerository  voteResultPerository = context.getBean(VoteResultPerository.class);
    static RadaSessionRepository radaSessionRepository = context.getBean(RadaSessionRepository.class);
    static PageRepository pageRepository = context.getBean(PageRepository.class);

    public static void main(String[] args) throws Exception {

        parser.setPageRepository(pageRepository);

        List<RadaSession> sessions = parser.getSessions();
        sessions.size();
        radaSessionRepository.save(sessions);
        for (RadaSession session : sessions){
            List<RadaSessionDay> days = parser.getSessionDays(session);
            radaSessionDayRepository.save(days);
            session.setDays(days);
            radaSessionRepository.save(session);
        }

        /*Iterable<RadaSession> sessions = radaSessionRepository.findAll();
        Iterable<RadaSessionDay> days = radaSessionDayRepository.findWithUrl();

        for (RadaSessionDay day : days){
            RadaSession s = day.getRadaSession();
            List<Law> laws = parser.loadLawsForDay(day);
            day.setLaws(laws);
            System.out.println(s.getSklikanya() + "\t" + s.getsName() + "\t" + day + laws.size());
            lawRepository.save(laws);
            radaSessionDayRepository.save(day);
        }*/

        //parseSessionsAndSaveToDB();
        //parseLawsAndSaveToDB();
        //String dayUrl = "http://iportal.rada.gov.ua" + "/meeting/awt/show/4109.html";
        //parselawsForDayAndSaveToDB(dayUrl);






        /*List<String> list = parser.getDayLinksBySessionUrl("http://iportal.rada.gov.ua/meeting/awt/56");
        for (String dayLink : list){
            List<Law> laws = parser.getLawsByDayLink(dayLink);
            System.out.println(laws.size() + "\t" + (laws.size()>0?laws.get(0).getDate():"") + "\t" + dayLink);
            lawRepo.save(laws);
        }*/

        /*
        Iterable<Law> laws = lawRepo.findAll();
        int i = 0;
        for(Law law : laws){
            i++;
            if (law.getVoteResultLink() != null && !law.getVoteResultLink().isEmpty())
                continue;
            try{
                System.out.print(i + " " + law.getLink() + "\t");
                String s = parser.getVoteResultLink(law.getLink().replace("static", "w1.c1"));
                System.out.println(s);
                law.setVoteResultLink(s);
                lawRepo.save(law);
            }catch (Exception ex){
                System.out.println("ERR: " + ex.getMessage());
            }
        }
          */


        //loadVotes();
        //loadMP();
        //loadVotesResults();

        //ApplicationContext context = new FileSystemXmlApplicationContext("C:\\src\\rada\\src\\main\\resources\\spring.xml");

        //LawRepository r = context.getBean(LawRepository.class);
        //Law law = r.findOne(32);

    }

    private static void parselawsForDayAndSaveToDB(String dayUrl) {

    }

    private static void parseLawsAndSaveToDB() throws Exception {
        //Iterable<RadaSession> sessions = readSessionsFromDB();
        Iterable<RadaSessionDay> days = radaSessionDayRepository.findWithUrl();
        for (RadaSessionDay day : days){
            System.out.print(day.getRadaSession().getSklikanya()
                    +"\t" + day.getRadaSession().getsName()
                    + "\t" + day.getMonth()
                    + "\t" + day.getDay()
                    + "\t" + day.getUrl()
            );
            List<Law> laws = parser.loadLawsForDay(day);
            System.out.println("\t" + laws.size());
            if (laws.isEmpty())
                continue;
            try {
                lawRepository.save(laws);
            }catch (Exception ex){
                int f  =5;
            }
            //radaSessionDayRepository.save(day);
        }
    }

    private static void saveDayToDB(RadaSessionDay day) {

    }

    private static void saveLawsToDB(List<Law> laws) {

    }

    private static List<RadaSessionDay> readDaysForSession(RadaSession session) {
        return null;
    }

    private static Iterable<RadaSession> readSessionsFromDB() {
        //return radaSessionRepository.findAll();
        return radaSessionRepository.findAll();
    }

    private static void parseSessionsAndSaveToDB() throws Exception {
        List<RadaSession> sessions = parser.getSessions();

        for (RadaSession session : sessions){
            List<RadaSessionDay> days = session.getDays();
            session.setDays(Collections.EMPTY_LIST);
            radaSessionRepository.save(session);
            radaSessionDayRepository.save(days);
            session.setDays(days);
            radaSessionRepository.save(session);
        }
    }

    private static void loadVotesResults() throws Exception {

        Integer[] voteIds = {3771};
        Iterable<Vote> votes = voteRepository.findAll(Arrays.asList(voteIds));

        Iterable<MP> mpList = mpRepository.findAll();
        Map<String, MP>  mpMap = new HashMap<String, MP>();
        for (MP mp : mpList){
            mpMap.put(mp.getFio(), mp);
        }

        List<VoteResult> results = parser.getVoteResults(votes, mpMap);
        System.out.println((445*4) + " results.size() = " + results.size());
    }

    private static void loadMP() throws Exception {
        String voteResUrl = "http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_golos_print?g_id=6385&vid=1";
        List<MP> listMP = parser.getMP(voteResUrl);
        System.out.println(listMP.size());


        mpRepository.save(listMP);
    }

    private static void loadVotes() {

        int i = 0;
        List<Vote> votes;
        for (Law law : lawRepository.findAll()){
            i++;
            votes = voteRepository.findByLaw(law);
            if (votes.size() > 0)
                continue;
            System.out.print(i + " " + law.getVoteResultLink() + "\t");
            try {
                votes = parser.getVotesByLaw(law);
                voteRepository.save(votes);
                System.out.println(votes.size());
                Thread.sleep(1000);
            }catch (Exception ex){
                System.out.println("ERR: " + ex.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
