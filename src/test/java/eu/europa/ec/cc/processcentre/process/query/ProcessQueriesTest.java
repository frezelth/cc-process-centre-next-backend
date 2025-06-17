package eu.europa.ec.cc.processcentre.process.query;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.process.query.repository.model.SearchProcessQueryResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.beans.factory.annotation.Autowired;

public class ProcessQueriesTest extends ProcessCentreNextApplicationTests {

    @Autowired
    private ProcessQueries processQueries;

    @Autowired
    private DataSource dataSource;

    @Test
    @SneakyThrows
    void testQuickSearch(){

        try (Connection conn = dataSource.getConnection()) {
            CopyManager copyManager = new CopyManager((BaseConnection) conn.unwrap(BaseConnection.class));

            insertProcesses(copyManager);
            insertProcessesLabels(copyManager);

            // Run ANALYZE
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("ANALYZE T_PROCESS");
                stmt.execute("ANALYZE T_STATIC_TRANSLATION");
                stmt.execute("REINDEX TABLE T_PROCESS");
                stmt.execute("REINDEX TABLE T_STATIC_TRANSLATION");
                // Optional: also run REINDEX if needed
                // stmt.execute("REINDEX TABLE your_table");



              System.out.println("push finished");

              long before = System.currentTimeMillis();
              List<SearchProcessQueryResponse> searchProcessQueryResponses = processQueries.searchProcesses(
                  new SearchProcessRequestDto(
                      null,
                      null,
                      null,
                      "Proc:*",
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      Collections.emptyList(),
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null,
                      null,
                      null,
                      null,
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null
                  ),
                  0,
                  20,
                  Locale.ENGLISH,
                  "frezeth"
              );
              System.out.println("results:"+searchProcessQueryResponses);
              System.out.println(System.currentTimeMillis() - before);

              before = System.currentTimeMillis();
              searchProcessQueryResponses = processQueries.searchProcesses(
                  new SearchProcessRequestDto(
                      null,
                      null,
                      null,
                      "titl:*",
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      null,
                      Collections.emptyList(),
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null,
                      null,
                      null,
                      null,
                      Collections.emptyList(),
                      Collections.emptyList(),
                      null
                  ),
                  0,
                  20,
                  Locale.ENGLISH,
                  "frezeth"
              );
              System.out.println(System.currentTimeMillis() - before);
              System.out.println("number of results:"+searchProcessQueryResponses);

              stmt.execute("DELETE FROM T_USER_TASK");
              stmt.execute("DELETE FROM T_PROCESS");
              stmt.execute("DELETE FROM T_PROCESS_VARIABLE");
              stmt.execute("DELETE FROM T_STATIC_TRANSLATION");

            }
        }
    }

    private static void insertProcesses(CopyManager copyManager) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        StringBuilder taskBuilder = new StringBuilder();
        StringBuilder taskTitleBuilder = new StringBuilder();
        for (int i=0;i<500;i++) {
            sb.append(i)
                    .append(',')
                    .append("Flex")
                    .append(',')
                    .append("PM_AGRI")
                    .append(',')
                    .append("processType1")
                    .append('\n');

            for (int j=0;j<30;j++) {

                if (j > 10){
                    taskBuilder.append(i).append("_").append(j)
                            .append(',')
                            .append(i)
                            .append(',')
                            .append("typeKey")
                            .append(',')
                            .append("COMPLETED")
                            .append('\n');
                } else {
                    taskBuilder.append(i).append("_").append(j)
                            .append(',')
                            .append(i)
                            .append(',')
                            .append("typeKey")
                            .append(',')
                            .append("CREATED")
                            .append('\n');
                }

                taskTitleBuilder.append(i).append("_").append(j)
                        .append(',')
                        .append("TASK")
                        .append(',')
                        .append("TASK_TITLE")
                        .append(',').append("en")
                        .append(',').append("task title title ").append(i).append("_").append(j)
                        .append('\n');
            }
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(data);
        copyManager.copyIn("COPY T_PROCESS (PROCESS_INSTANCE_ID, PROVIDER_ID, DOMAIN_KEY, PROCESS_TYPE_KEY) FROM STDIN WITH (FORMAT csv)", is);


        byte[] dataTask = taskBuilder.toString().getBytes(StandardCharsets.UTF_8);
        InputStream isTask = new ByteArrayInputStream(dataTask);
        copyManager.copyIn("COPY T_USER_TASK (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID, TASK_TYPE_KEY, STATUS) FROM STDIN WITH (FORMAT csv)", isTask);

        byte[] dataTranslation = taskTitleBuilder.toString().getBytes(StandardCharsets.UTF_8);
        InputStream isTranslation = new ByteArrayInputStream(dataTranslation);
        copyManager.copyIn("COPY T_STATIC_TRANSLATION (OBJECT_ID, OBJECT_TYPE, ATTRIBUTE_NAME, LANGUAGE_CODE, TRANSLATED_TEXT) FROM STDIN WITH (FORMAT csv)", isTranslation);
    }

    private static void insertProcessesLabels(CopyManager copyManager) throws SQLException, IOException {
        StringBuilder sb = new StringBuilder();
        for (int i=0;i<100;i++) {
            sb.append(i)
                    .append(',')
                    .append("PROCESS")
                    .append(',')
                    .append("PROCESS_TITLE")
                    .append(',').append("en")
                    .append(',').append("process title ").append(i)
                    .append('\n');
        }

        byte[] data = sb.toString().getBytes(StandardCharsets.UTF_8);
        InputStream is = new ByteArrayInputStream(data);
        copyManager.copyIn("COPY T_STATIC_TRANSLATION (OBJECT_ID, OBJECT_TYPE, ATTRIBUTE_NAME, LANGUAGE_CODE, TRANSLATED_TEXT) FROM STDIN WITH (FORMAT csv)", is);
    }

}
