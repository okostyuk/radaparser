package com.oleg.rada;

import com.oleg.rada.persistance.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: okostyuk
 * Date: 9/4/2014
 */
public class RadaHtmlParser {

    public List<String> getLawLinksByDayUrl(String url) throws IOException {
        List<String> val = new ArrayList<String>();
        Document doc = Jsoup.connect("http://iportal.rada.gov.ua" + url).get();

        Elements es = doc.getElementsByTag("a");
        for (Element e : es){
            if (e.attr("style").equals("color: purple;")){
                val.add(e.attr("href")+"#ui-tabs-2");
            }
        }
        return val;
    }

    public List<String> getDayLinksBySessionUrl(String sessionUrl) throws IOException {
        List<String> val = new ArrayList<String>();
        Document doc = Jsoup.connect(sessionUrl).get();

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

    public List<Law> loadLawsForDayTest(String url) throws IOException{
        List<Law> laws = new ArrayList<Law>();
        Connection c = Jsoup.connect(url).timeout(10000);
        doc = c.get();
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

    public List<Law> loadLawsForDay(RadaSessionDay day) throws IOException {
        if (day.getUrl() == null || day.getUrl().isEmpty())
            return Collections.emptyList();
        return loadLawsForDayTest("http://iportal.rada.gov.ua" + day.getUrl());
    }

    public String getVoteResultLink(String lawLink) throws IOException {
        Document doc = Jsoup.connect(lawLink).get();
        Elements tabs = doc.getElementsByClass("tabs_block");
        Element e = tabs.get(0).getElementsByTag("li").last();
        return e.getElementsByTag("a").first().attr("href");
    }

    static SimpleDateFormat voteDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    public List<Vote> getVotesByLaw(Law law) throws IOException {
        List<Vote> votes = new ArrayList<Vote>();
        Document doc = Jsoup.connect("http://w1.c1.rada.gov.ua" + law.getVoteResultLink()).get();
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

    public List<MP> getMP(String voteResUrl) throws IOException {
        List<MP> res = new ArrayList<MP>();
        Document doc = Jsoup.connect(voteResUrl).get();
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

    public List<VoteResult> getVoteResults(Iterable<Vote> voteList, Map<String, MP> mpMap) throws IOException {
        List<VoteResult> res = new ArrayList<VoteResult>();
        StringBuilder sb = new StringBuilder();
        sb.append("http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_zakon_gol_dep_list?zn=");
        for (Vote vote : voteList){
            sb.append("$").append(vote.getId());
        }
        Document doc = Jsoup.connect(sb.toString()).get();
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

    public List<RadaSession> getSessions() throws IOException {
        List<RadaSession> sessions = new ArrayList<RadaSession>();
        Document doc = Jsoup.connect("http://iportal.rada.gov.ua/meeting/awt/53")
                .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:32.0) Gecko/20100101 Firefox/32.0")
                .timeout(10000).get();
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
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
            parseSessionData(s);
        }
        return sessions;
    }

    private void parseSessionData(RadaSession s) throws IOException {
        System.out.println(s.getId());
        Document doc = Jsoup.connect("http://iportal.rada.gov.ua" + s.getUrl()).get();
        Element calendar = doc.getElementsByClass("b_calendar").first();
        String name = calendar.getElementsByTag("h3").first().text();
        s.setName(name);

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
                s.addDay(radaDay);
            }
        }
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
}
