import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.dao.GenericRawResults;
import com.j256.ormlite.dao.RawRowMapper;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import model.Employer;
import model.Job;
import spark.ModelAndView;
import spark.Spark;
import spark.template.velocity.VelocityTemplateEngine;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Server {

    private static Dao getEmployerORMLiteDao() throws SQLException {
        final String URI = "jdbc:sqlite:./JBApp.db";
        ConnectionSource connectionSource = new JdbcConnectionSource(URI);
        TableUtils.createTableIfNotExists(connectionSource, Employer.class);
        return DaoManager.createDao(connectionSource, Employer.class);
    }

    private static Dao getJobORMLiteDao() throws SQLException {
        final String URI = "jdbc:sqlite:./JBApp.db";
        ConnectionSource connectionSource = new JdbcConnectionSource(URI);
        TableUtils.createTableIfNotExists(connectionSource, Job.class);
        return DaoManager.createDao(connectionSource, Job.class);
    }

    public static void main(String[] args) {

        final int PORT_NUM = 7000;
        Spark.port(PORT_NUM);

        Spark.get("/", (req, res) -> {
            Map<String, Object> model = new HashMap<>();
            if (req.cookie("username") != null)
                model.put("username", req.cookie("username"));
            return new ModelAndView(model, "public/index.vm");
        }, new VelocityTemplateEngine());

        Spark.post("/", (req, res) -> {
            String username = req.queryParams("username");
            String color = req.queryParams("color");
            res.cookie("username", username);
            res.redirect("/");
            return null;
        });

        Spark.get("/employers", (req, res) -> {
            List<Employer> ls = getEmployerORMLiteDao().queryForAll();
            Map<String, Object> model = new HashMap<String, Object>();
            model.put("employers", ls);
            return new ModelAndView(model, "public/employers.vm");
        }, new VelocityTemplateEngine());

        Spark.post("/employers", (req, res) -> {
            String name = req.queryParams("name");
            String sector = req.queryParams("sector");
            String summary = req.queryParams("summary");
            Employer em = new Employer(name, sector, summary);
            getEmployerORMLiteDao().create(em);
            res.status(201);
            res.type("application/json");
            return new Gson().toJson(em.toString());
        });

        Spark.get("/addemployers", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            return new ModelAndView(model, "public/addemployers.vm");
        }, new VelocityTemplateEngine());

        Spark.get("/search", (req, res) -> {
            Map<String, Object> model = new HashMap<String, Object>();
            return new ModelAndView(model, "public/search.vm");
        }, new VelocityTemplateEngine());

        Spark.post("/search", (req, res) -> {
            String term = "%" + req.queryParams("job-search-term") + "%";   // post parameter
            QueryBuilder<Employer, Integer> employerQB = getEmployerORMLiteDao().queryBuilder();
            QueryBuilder<Job, Integer> jobQB = getJobORMLiteDao().queryBuilder();
            List<Job> returnJobs = new ArrayList<>();

            // sql query to get jobs with term in job title and domain
            returnJobs = jobQB.where()
                    .like(Job.JOB_TITLE, term)
                    .or()
                    .like(Job.JOB_DOMAIN, term).query();
            // get employers with term in employer's name
            List<Employer> employersSearchName = employerQB.where().like(Employer.EMPLOYER_NAME, term).query();
            List<Integer> employersId = new ArrayList<>();  // store returned employers' ids
            for (Employer e: employersSearchName) {
                employersId.add(e.getId());
            }
            // traverse through all jobs and find jobs with early returned employers' ids
            List<Job> allJobs = new ArrayList<>();
            allJobs = getJobORMLiteDao().queryForAll();
            System.out.println(allJobs);
            for (Job j: allJobs) {
                if (employersId.contains(j.getEmployer().getId())) {
                    if (!returnJobs.contains(j)){
                        returnJobs.add(j);  // add into results
                    }
                }
            }
            res.status(201);
            res.type("application/json");
            return new Gson().toJson(returnJobs);
        });

    }
}
