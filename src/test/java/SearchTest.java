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
    }
}
