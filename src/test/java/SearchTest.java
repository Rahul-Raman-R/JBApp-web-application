import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import model.Job;
import okhttp3.*;
import org.junit.jupiter.api.*;
import spark.utils.Assert;

import javax.print.attribute.standard.JobName;
import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SearchTest {

    private final String URI = "jdbc:sqlite:./JBApp.db";

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SearchORMLiteDaoTest {

        private ConnectionSource connectionSource;
        private Dao<Job, Integer> dao;

        // create a new connection to JBApp database, create "employers" table, and create a
        // new dao to be used by test cases
        @BeforeAll
        public void setUpAll() throws SQLException {
            connectionSource = new JdbcConnectionSource(URI);
            TableUtils.createTableIfNotExists(connectionSource, Job.class);
            dao = DaoManager.createDao(connectionSource, Job.class);
        }

        // delete all rows in the employers table before each test case
        @BeforeEach
        public void setUpEach() throws SQLException {
            TableUtils.clearTable(connectionSource, Job.class);
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class SearchAPITest {

        final String BASE_URL = "http://localhost:7000";
        private OkHttpClient client;

        @BeforeAll
        public void setUpAll() {
            client = new OkHttpClient();
        }

        @Test
        public void testHTTPGetSearchEndpointCompanyNameExactly() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Eyemed")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test
            assertEquals(2, searchResults.size());
            assertEquals(4, searchResults.get(0).getId());
            assertEquals(5, searchResults.get(1).getId());
        }
        @Test
        public void testCompanyNameExists() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "eyemed")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test

            assertEquals(2, searchResults.size());
            for (int i=0;i<searchResults.size();i++)
            {
                assertEquals("eyemed", searchResults.get(i).getEmployer().getName().toLowerCase());
            }
        }
        @Test
        public void testDomainNameExists() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Management")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test


            for (int i=0;i<searchResults.size();i++)
            {
                assertEquals("management".toLowerCase(), searchResults.get(i).getDomain().toLowerCase());
            }
        }
        @Test
        public void testJobTitleExists() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Software Developer")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test


            for (int i=0;i<searchResults.size();i++)
            {
                assertEquals("Software Developer".toLowerCase(), searchResults.get(i).getTitle().toLowerCase());
            }
        }
        @Test
        public void testEmptyInput() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test
            int i;
            if(searchResults.size()>1)
            {
                i=1;
            }
            else
            {
               i=0;
            }

            assertEquals(1,i);
        }

        @Test
        public void testCompanyNameSubstring() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Coca")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test
           for(int i=0;i<searchResults.size();i++)
           {
               assertEquals(true,searchResults.get(i).getEmployer().getName().toLowerCase().contains("Coca".toLowerCase()));


           }
        }
        @Test
        public void testTitleSubstring() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Scientist")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test
            for(int i=0;i<searchResults.size();i++)
            {
                assertEquals(true,searchResults.get(i).getTitle().toLowerCase().contains("Scientist".toLowerCase()));


            }
        }
        @Test
        public void testDomainSubstring() throws IOException {
            String endpoint = BASE_URL + "/search";
            List<Job> searchResults = new ArrayList<>();
            Type listType = new TypeToken<List<Job>>() {}.getType();
            String jsonStr = "";

            // construct post form with parameters
            RequestBody formBody = new FormBody.Builder()
                    .add("job-search-term", "Software")
                    .build();
            // post request
            Request request = new Request.Builder()
                    .url(endpoint)
                    .post(formBody)
                    .build();
            // receive json response and parse to string
            Response response = client.newCall(request).execute();

            assertEquals(201, response.code());

            jsonStr = response.body().string();
            // parse json into job list
            searchResults = new Gson().fromJson(jsonStr, listType);
            System.out.print(searchResults);
            Collections.sort(searchResults);    // sort results based on ascending job id
            // test
            for(int i=0;i<searchResults.size();i++)
            {
                assertEquals(true,searchResults.get(i).getDomain().toLowerCase().contains("Software".toLowerCase()));


            }
        }
    }
}

