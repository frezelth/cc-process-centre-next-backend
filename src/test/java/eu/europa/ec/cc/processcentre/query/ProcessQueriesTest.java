package eu.europa.ec.cc.processcentre.query;

import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ProcessQueriesTest extends ProcessCentreNextApplicationTests {

    @Autowired
    private ProcessQueries processQueries;

    @Test
    @Sql(
            statements = {
                    "insert into t_process (PROCESS_INSTANCE_ID) values ('P1')",
                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('P1T1', 'P1')",
                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('P1T2', 'P1')",
                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
                    "values ('PROCESS', 'P1', 'title', 'en', 'test', true)",
                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
                            "values ('PROCESS', 'P1', 'title', 'fr', 'test fr', false)",
                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
                            "values ('TASK', 'P1T1', 'title', 'en', 'test task title', true)",
                    "insert into t_process (PROCESS_INSTANCE_ID) values ('2')",
                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('T2', '2')",
                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
                            "values ('PROCESS', '2', 'title', 'en', 'process 2', true)",
                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
                            "values ('TASK', 'T2', 'title', 'en', 'task 2', true)"

            })
    void testQuickSearch(){
        List<SearchProcessQueryResponse> searchProcessQueryResponses = processQueries.searchProcesses(
                new SearchProcessRequestDto(
                        null,
                        null,
                        null,
                        "test",
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
                10,
                Locale.FRENCH,
                "frezeth"
        );
        Assertions.assertEquals(1, searchProcessQueryResponses.size());
    }

}
