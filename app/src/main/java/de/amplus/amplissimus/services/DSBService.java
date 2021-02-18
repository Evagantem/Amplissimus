package de.amplus.amplissimus.services;

import android.util.Log;
import android.util.Pair;

import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import de.sematre.dsbmobile.DSBMobile;

public class DSBService {
    public DSBService(@NotNull String username, @NotNull String password) {
        dsbMobile = new DSBMobile(username, password);
    }

    public DSBService() throws Exception {
        if(dsbMobile == null) throw new Exception("DSBMobile not initialized!");
    }

    private static DSBMobile dsbMobile;
    private static List<Plan> plans;
    private static String filterBy;

    public static void setFilter(String filter) { filterBy = filter; }

    public static String getFilter() { return filterBy; }

    public static List<Plan> getPlans() {
        return plans;
    }

    public static void setPlans(List<Plan> plans) { DSBService.plans = plans; }

    public boolean credentialsOK() {
        Log.d("DSBService", "Checking credentials...");
        try {
            JsonObject jsonObject = dsbMobile.pullData();
            return jsonObject.get("Resultcode").getAsInt() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static List<Plan> getFilteredPlans() {
        if(filterBy == null) return plans;
        List<Plan> resultPlans = new ArrayList<>();
        for (Plan plan : plans) {
            List<Substitution> planSubs = new ArrayList<>();
            for (ClassSubs classSubs : plan.substitutions) {
                if (classSubs.className.contains(filterBy))
                    planSubs.addAll(classSubs.subs);
            }
            resultPlans.add(new Plan(planSubs, plan.title, plan.url));
        }
        return resultPlans;
    }

    public String htmlDecode(final String input) {
        String s = input;
        s = s.replace("&nbsp;", " ");
        s = s.replace("&quot;", "\"");
        s = s.replace("&apos;", "'");
        s = s.replace("&#39;", "'");
        s = s.replace("&lt;", "<");
        s = s.replace("&gt;", ">");
        s = s.replace("&amp;", "&");
        return s;
    }

    public List<Plan> parseTimetables() throws Exception {
        List<Pair<Document, String>> documents = new ArrayList<>();
        try {
            for(DSBMobile.TimeTable timeTable : dsbMobile.getTimeTables()) {
                documents.add(new Pair<>(
                        Jsoup.connect(timeTable.getDetail()).get(), timeTable.getDetail()
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while fetching timetables");
        }
        List<Plan> plans = new ArrayList<>();
        try {
            for(Pair<Document, String> docPair : documents) {
                String title = docPair.first.getElementsByClass("mon_title").first().html();
                List<Element> elements = docPair.first.getElementsByClass("mon_list")
                        .first().child(0).children();
                List<Substitution> subs = new ArrayList<>();
                for(Element element : elements) {
                    if(elements.indexOf(element) != 0) {
                        List<Element> children = element.children();
                        Substitution sub;
                        if(children.size() == 6) {
                            if(children.get(0).html().contains("<span style=\"color: ")) {
                                sub = new Substitution(
                                        htmlDecode(children.get(0).child(0).html()),
                                        htmlDecode(children.get(1).child(0).html()),
                                        htmlDecode(children.get(2).child(0).html()),
                                        htmlDecode(children.get(3).child(0).html()),
                                        htmlDecode(children.get(4).child(0).html()),
                                        htmlDecode(children.get(5).child(0).html())
                                );
                            } else {
                                sub = new Substitution(
                                        htmlDecode(children.get(0).html()),
                                        htmlDecode(children.get(1).html()),
                                        htmlDecode(children.get(2).html()),
                                        htmlDecode(children.get(3).html()),
                                        htmlDecode(children.get(4).html()),
                                        htmlDecode(children.get(5).html())
                                );
                            }
                        } else if(children.size() == 5) {
                            if(children.get(0).html().contains("<span style=\"color: ")) {
                                sub = new Substitution(
                                        htmlDecode(children.get(0).child(0).html()),
                                        htmlDecode(children.get(1).child(0).html()),
                                        htmlDecode(children.get(2).child(0).html()),
                                        htmlDecode(children.get(3).child(0).html()),
                                        null,
                                        htmlDecode(children.get(4).child(0).html())
                                );
                            } else {
                                sub = new Substitution(
                                        htmlDecode(children.get(0).html()),
                                        htmlDecode(children.get(1).html()),
                                        htmlDecode(children.get(2).html()),
                                        htmlDecode(children.get(3).html()),
                                        null,
                                        htmlDecode(children.get(4).html())
                                );
                            }
                        } else break;
                        subs.add(sub);
                    }
                }
                plans.add(new Plan(subs, title, docPair.second));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("Error while parsing timetables");
        }
        DSBService.plans = plans;
        return plans;
    }

    public static class Plan {
        public Plan(@NotNull final List<Substitution> substitutions, @NotNull String title, @NotNull String url) {
            this.substitutions = subsToClassSubs(substitutions);
            this.title = title;
            this.url = url;
        }

        private List<ClassSubs> subsToClassSubs(@NotNull final List<Substitution> substitutions) {
            List<ClassSubs> result = new ArrayList<>();
            while (substitutions.size() != 0) {
                List<Substitution> filteredSubs = new ArrayList<>();
                for (Substitution s : substitutions) {
                    if (s.getAffectedClass().equals(substitutions.get(0).getAffectedClass()))
                        filteredSubs.add(s);
                }
                if (filteredSubs.size() > 0)
                    substitutions.subList(0, filteredSubs.size()).clear();
                result.add(new ClassSubs(filteredSubs.get(0).getAffectedClass(), filteredSubs));
            }
            return result;
        }

        private final String url;
        private final String title;
        private final List<ClassSubs> substitutions;

        public String getUrl() { return url; }

        public String getDay() {
            return title.split(" ")[1];
        }

        public String getTitle() {
            String[] parts = title.split(" ");
            return String.format("%s, %s", parts[1], parts[0]);
        }

        public List<ClassSubs> getSubstitutions() {
            return substitutions;
        }

        @NotNull
        @Override
        public String toString() {
            return "Plan { " +
                    "title=" + title +
                    ", substitutions=" + substitutions.toString() +
                    " }";
        }
    }

    public static class ClassSubs {
        public ClassSubs(@NotNull String className, @NotNull List<Substitution> subs) {
            this.className = className;
            this.subs = subs;
        }

        private final String className;
        private final List<Substitution> subs;

        public String getClassName() {
            return className;
        }

        public List<Substitution> getSubs() {
            return subs;
        }

        @NotNull
        @Override
        public String toString() {
            return "ClassSubs { " +
                    "className='" + className + '\'' +
                    ", subs=" + subs +
                    " }";
        }
    }

    public static class Substitution {
        public Substitution(
                @NotNull String affectedClass,
                @NotNull String hours,
                @NotNull String newTeacher,
                @NotNull String subject,
                String origTeacher,
                String notes
        ) {
            this.affectedClass = affectedClass;
            this.hours = hours;
            this.newTeacher = newTeacher;
            this.subject = subject;
            this.origTeacher = origTeacher;
            this.notes = notes;
        }

        private final String affectedClass;
        private final String origTeacher;
        private final String newTeacher;
        private final String subject;
        private final String notes;
        private final String hours;


        public String getAffectedClass() {
            return affectedClass;
        }

        public String getOrigTeacher() {
            return origTeacher;
        }

        public String getNewTeacher() {
            return newTeacher;
        }

        public String getSubject() {
            return subject;
        }

        public String getViewableNotes() {
            return (hasNotes() ? String.format("\n(%s)", notes) : "");
        }

        public String getNotes() {
            return notes;
        }

        public String getHours() {
            return hours;
        }

        public boolean isFree() {
            return newTeacher.trim().equals("---") || newTeacher.isEmpty();
        }

        public boolean hasNotes() { return notes != null && !notes.trim().isEmpty(); }

        @NotNull
        @Override
        public String toString() {
            return "Substitution { " +
                    "affectedClass='" + affectedClass + '\'' +
                    ", origTeacher='" + origTeacher + '\'' +
                    ", newTeacher='" + newTeacher + '\'' +
                    ", subject='" + subject + '\'' +
                    ", notes='" + notes + '\'' +
                    ", hours='" + hours + '\'' +
                    " }";
        }
    }

}

