package com.oleg.rada;

import com.oleg.rada.persistance.*;
import com.oleg.rada.persistance.rep.PageRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: okostyuk
 * Date: 9/4/2014
 */
public class RadaHtmlParser {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0";
    private PageRepository pageRepository;

    public List<String> getLawLinksByDayUrl(String url) throws Exception {
        List<String> val = new ArrayList<String>();
        Document doc = getDocument("http://iportal.rada.gov.ua" + url);

        Elements es = doc.getElementsByTag("a");
        for (Element e : es){
            if (e.attr("style").equals("color: purple;")){
                val.add(e.attr("href")+"#ui-tabs-2");
            }
        }
        return val;
    }

    public List<String> getDayLinksBySessionUrl(String sessionUrl) throws Exception {
        List<String> val = new ArrayList<String>();
        Document doc = getDocument(sessionUrl);

        //Elements es = doc.getElementsByClass("b_yellow");
        Elements es = doc.getElementsByClass("calendar_list");
        es = es.get(0).getElementsByTag("a");
        for (Element e : es){
            val.add(e.attr("href"));
        }
        return val;
    }

    private String getDateByDayDoc(Document doc) throws IOException {
        return doc.getElementsByClass("date").get(0).ownText();
    }

    public static Document doc;

    public List<Law> loadLawsForDayTest(String url) throws Exception{
        List<Law> laws = new ArrayList<Law>();
        doc = getDocument(url);
        Elements tables = doc.getElementsByClass("MsoNormalTable");
        if (tables.size() == 0)
            tables = doc.getElementsByTag("table");
        Elements lawLinks = tables.first().getElementsByTag("a");
        Law law;
        for (Element lawLink : lawLinks){
            if (lawLink.attr("href").isEmpty())
                continue;
            law = new Law();
            laws.add(law);
            law.setNum(lawLink.text());
            try{
                law.setId(Integer.parseInt(law.getNum()));
            }catch (Exception ex){
                law.setId(law.getNum().hashCode());
            }
            law.setLink(lawLink.attr("href"));

            Element tr = lawLink.parent();
            while(true){
                if (tr.tag().getName().equalsIgnoreCase("tr")){
                    law.setName(tr.getElementsByTag("td").get(2).text());
                    break;
                }
                if (tr.tag().getName().equalsIgnoreCase("table")){
                    break;
                }
                tr = tr.parent();
            }
        }


        return laws;
    }

    public List<Law> loadLawsForDay(RadaSessionDay day) throws Exception {
        if (day.getUrl() == null || day.getUrl().isEmpty())
            return Collections.emptyList();
        return loadLawsForDayTest("http://iportal.rada.gov.ua" + day.getUrl());
    }

    public String getVoteResultLink(String lawLink) throws Exception {
        Document doc = getDocument(lawLink);
        Elements tabs = doc.getElementsByClass("tabs_block");
        Element e = tabs.get(0).getElementsByTag("li").last();
        return e.getElementsByTag("a").first().attr("href");
    }

    static SimpleDateFormat voteDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public List<Vote> getVotesByLaw(Law law) throws Exception {
        List<Vote> votes = new ArrayList<Vote>();
        Document doc = getDocument("http://w1.c1.rada.gov.ua" + law.getVoteResultLink());
        Element table = doc.getElementById("gol_v");
        Elements rows = table.getElementsByTag("li");
        for (Element row : rows){
            Elements checkboxes = row.getElementsByTag("input");
            if (checkboxes.isEmpty())
                continue;
            Vote vote = new Vote();
            vote.setLaw(law);
            Integer id = Integer.parseInt(checkboxes.first().attr("value"));
            vote.setId(id);
            try {
                Date date = voteDateFormat.parse(row.getElementsByClass("fr_data").first().text());
                vote.setDate(date);
            }catch (Exception ex){ System.err.println("Err parse date");}

            String name = row.getElementsByClass("fr_nazva").first().getElementsByTag("a").first().text();
            vote.setName(name);
            String text = row.getElementsByClass("fr_nazva").first().getElementsByTag("center").first().text();
            vote.setResText(text);
            votes.add(vote);
        }
        return votes;
    }

    public List<MP> getMP(String voteResUrl) throws Exception {
        List<MP> res = new ArrayList<MP>();
        Document doc = getDocument(voteResUrl);
        Element table = doc.getElementsByTag("table").get(1);
        Elements rows = table.getElementsByTag("tr");
        String fraction = "";
        String fio;
        MP mp;
        for (Element row : rows){
            Elements columns = row.getElementsByTag("td");
            if (columns.size() == 1){
                try {
                    Element e1 = columns.get(0);
                    Elements all = e1.getAllElements().first().getAllElements();
                    Element e2 = all.get(0);
                    Elements e3s = e2.getElementsByTag("b");
                    Element e3 = e3s.get(0);
                    fraction = e3.text();
                }catch (Exception ex){
                    continue;
                }
            }else{
                mp = new MP();
                mp.setFraction(fraction);
                mp.setFio(columns.get(0).text());
                res.add(mp);

                mp = new MP();
                mp.setFraction(fraction);
                mp.setFio(columns.get(2).text());
                if (mp.getFio().length() > 5){
                    res.add(mp);
                }
            }
        }
        return res;
    }

    public List<VoteResult> getVoteResults(Iterable<Vote> voteList, Map<String, MP> mpMap) throws Exception {
        List<VoteResult> res = new ArrayList<VoteResult>();
        StringBuilder sb = new StringBuilder();
        sb.append("http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_zakon_gol_dep_list?zn=");
        for (Vote vote : voteList){
            sb.append("$").append(vote.getId());
        }
        Document doc = getDocument(sb.toString());
        Elements tables = doc.getElementsByClass("tab_gol");
        Element table = tables.get(1);
        Elements rows = table.getElementsByTag("tr");
        Element cell;
        boolean skippedFirst = false;
        int k = 0;
        for (Element row : rows){
            if (!skippedFirst){
                skippedFirst = true;
                continue;
            }
            Elements columns = row.getElementsByTag("td");
            System.out.println(columns.first().text()+"\t"+columns.get(1).text());
            String fio = columns.get(1).text();
            if (fio.trim().isEmpty())
                continue;
            int i = 2;
            for (Vote vote : voteList){
                cell = columns.get(i);
                String result = cell.attr("style") + cell.text();
                VoteResult voteResult = new VoteResult();
                voteResult.setRes(result);
                voteResult.setVote(vote);
                MP mp = mpMap.get(fio);
                if (mp == null){
                    mp = new MP();
                    mp.setFio(fio);
                }
                voteResult.setMp(mp);
                res.add(voteResult);
            }
            k++;
        }
        VoteResult r = res.get(res.size()-1);
        return res;

    }

    public List<RadaSession> getSessions() throws Exception {
        List<RadaSession> sessions = new ArrayList<RadaSession>();
        Document doc = getDocument("http://iportal.rada.gov.ua/meeting/awt/53");

        RadaSession session = new RadaSession();
        session.setUrl("/meeting/awt/53");
        session.setSklikanya("VII скликання");
        session.setsName("1-ша сесія");
        session.generateId();
        Element calendar = doc.getElementsByClass("b_calendar").first();
        String name = calendar.getElementsByTag("h3").first().text();
        session.setName(name);
        sessions.add(session);

        parseSessionLinks(sessions, calendar);
        for (RadaSession s : sessions){

            parseSessionData(s);
        }
        return sessions;
    }

    private void parseSessionData(RadaSession s) throws Exception {
        System.out.println(s.getId());
        Document doc = getDocument("http://iportal.rada.gov.ua" + s.getUrl());
        Element calendar = doc.getElementsByClass("b_calendar").first();
        String name = calendar.getElementsByTag("h3").first().text();
        s.setName(name);

        //List<RadaSessionDay> days = getSessionDays(s);
    }

    public List<RadaSessionDay> getSessionDays(RadaSession s) throws Exception{
        List<RadaSessionDay> daysList = new ArrayList<RadaSessionDay>();

        Document doc = getDocument("http://iportal.rada.gov.ua" + s.getUrl());
        Element calendar = doc.getElementsByClass("b_calendar").first();

        Element calendarList = calendar.getElementsByClass("calendar_list").first();
        String monthName = null;
        for (Element month : calendarList.children()){
            monthName = month.children().first().text();
            Elements days = month.getElementsByTag("td");
            for (Element day : days){
                int dayValue;
                try{
                    dayValue = Integer.parseInt(day.text());
                }catch (Exception ex){
                    continue;
                }

                RadaSessionDay radaDay =  new RadaSessionDay();
                radaDay.setMonth(monthName);
                radaDay.setType(day.attr("class"));
                radaDay.setDay(dayValue);
                radaDay.setRadaSession(s);
                radaDay.generateId();
                Element url = day.getElementsByTag("a").first();
                if (url != null)
                    radaDay.setUrl(url.attr("href"));
                daysList.add(radaDay);
            }
        }

        return daysList;
    }

    private void parseSessionLinks(List<RadaSession> sessions, Element calendar) {
        Elements es = calendar.getElementsByClass("block_text");
        Element element = es.last();
        String sklikanya = null;
        for (Element e : element.children()){
            if (e.tag().getName().equals("div")){
                sklikanya = e.text();
            }else if (e.tag().getName().equals("a")){
                RadaSession session = new RadaSession();
                session.setUrl(e.attr("href"));
                session.setSklikanya(sklikanya);
                session.setsName(e.text());
                session.generateId();
                sessions.add(session);
            }
        }
    }

    public static String sanitizeFilename(String name) {
        return name.replaceAll("[:\\\\/*?|<>]", "_");
    }

    public Document getDocument(String url) throws Exception{
        return getDocument(url, 10000);
    }

    private Document getDocument(String url, int timeout) throws Exception{
        Document doc = null;
        try{
            doc = Jsoup.parse(pageRepository.findOne(url).getFile(), "UTF-8");
        }catch (Exception ex){
            ex.printStackTrace();
        }
        if (doc == null){
            doc = Jsoup.connect(url).userAgent(USER_AGENT).timeout(timeout).get();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            File file = saveDocToFile(doc, url);
            if (pageRepository != null){
                Page page = pageRepository.findOne(url);
                if (page == null){
                    page = new Page();
                    page.setUrl(url);
                }
                page.setFile(file);
                page.setFilePath(file.getPath());
                page.setFileContent(doc.outerHtml());
                pageRepository.save(page);
            }
        }
        return doc;
    }

    private File saveDocToFile(Document doc, String url) throws Exception{
        String filename = sanitizeFilename(url+".html");
        File file = new File("htmlCache/"+filename);
        BufferedWriter htmlWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        htmlWriter.write(doc.toString());
        htmlWriter.flush();
        htmlWriter.close();
        return file;
    }

    public void setPageRepository(PageRepository pageRepository) {
        this.pageRepository = pageRepository;
    }

    public PageRepository getPageRepository() {
        return pageRepository;
    }
}
