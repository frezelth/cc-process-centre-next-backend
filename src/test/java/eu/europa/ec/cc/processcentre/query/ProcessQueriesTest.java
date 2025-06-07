package eu.europa.ec.cc.processcentre.query;

import eu.europa.ec.cc.babel.proto.BabelText;
import eu.europa.ec.cc.babel.proto.BabelText.Literal;
import eu.europa.ec.cc.babel.proto.BabelText.Literal.LiteralValue;
import eu.europa.ec.cc.babel.proto.ISO6391LanguageCode;
import eu.europa.ec.cc.processcentre.ProcessCentreNextApplicationTests;
import eu.europa.ec.cc.processcentre.dto.SearchProcessRequestDto;
import eu.europa.ec.cc.processcentre.dto.SearchProcessResponseDto;
import eu.europa.ec.cc.processcentre.proto.UpdateProcessContext;
import eu.europa.ec.cc.processcentre.repository.ProcessMapper;
import eu.europa.ec.cc.processcentre.repository.ProcessVariableMapper;
import eu.europa.ec.cc.processcentre.repository.TaskMapper;
import eu.europa.ec.cc.processcentre.repository.model.CompleteTaskQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.CreateProcessQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.CreateTaskQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.InsertOrUpdateProcessVariableQueryParam;
import eu.europa.ec.cc.processcentre.repository.model.SearchProcessQueryResponse;
import eu.europa.ec.cc.processcentre.service.IngestionService;
import eu.europa.ec.cc.processcentre.service.ProcessService;
import eu.europa.ec.cc.processcentre.translation.TranslationAttribute;
import eu.europa.ec.cc.processcentre.translation.TranslationObjectType;
import eu.europa.ec.cc.processcentre.translation.repository.InsertOrUpdateTranslationsParam;
import eu.europa.ec.cc.processcentre.translation.repository.TranslationMapper;
import eu.europa.ec.cc.processcentre.util.ProtoUtils;
import eu.europa.ec.cc.provider.proto.ProcessCreated;
import eu.europa.ec.cc.provider.task.event.proto.TaskCreated;
import eu.europa.ec.cc.variables.proto.VariableValue;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Set;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
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
    @Autowired
    private IngestionService ingestionService;
  @Autowired
  private ProcessService processService;

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Test
//    @Sql(
//            statements = {
//                    "insert into t_process (PROCESS_INSTANCE_ID) values ('P1')",
//                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('P1T1', 'P1')",
//                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('P1T2', 'P1')",
//                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                    "values ('PROCESS', 'P1', 'title', 'en', 'test', true)",
//                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                            "values ('PROCESS', 'P1', 'title', 'fr', 'test fr', false)",
//                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                            "values ('TASK', 'P1T1', 'title', 'en', 'test task title', true)",
//                "insert into t_process (PROCESS_INSTANCE_ID, PARENT_ID) values ('P1Child', 'P1')",
//                "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('T1P1Child', 'P1Child')",
//                "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                    "values ('PROCESS', 'P1Child', 'title', 'en', 'subprocess en', true)",
//                "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                    "values ('PROCESS', 'P1Child', 'title', 'fr', 'subprocess fr', false)",
//                "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                    "values ('TASK', 'T1P1Child', 'title', 'en', 'task subprocess', true)",
//                    "insert into t_process (PROCESS_INSTANCE_ID) values ('2')",
//                    "insert into t_user_task (TASK_INSTANCE_ID, PROCESS_INSTANCE_ID) values ('T2', '2')",
//                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                            "values ('PROCESS', '2', 'title', 'en', 'process 2', true)",
//                    "insert into t_static_translation (object_type, object_id, attribute_name, language_code, translated_text, is_default) " +
//                            "values ('TASK', 'T2', 'title', 'en', 'task 2', true)"
//
//            })
    void testQuickSearch(){

        // insert thousands of rows
        List<CreateProcessQueryParam> processCreated = new ArrayList<>();
        List<InsertOrUpdateProcessVariableQueryParam> insertVariables = new ArrayList<>();
        List<CreateTaskQueryParam> taskCreated = new ArrayList<>();
        List<CompleteTaskQueryParam> completeTasks = new ArrayList<>();
        List<InsertOrUpdateTranslationsParam> insertOrUpdateTranslationsParams = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            processCreated.add(
                new CreateProcessQueryParam(String.valueOf(i), Instant.now(), "Flex", "processType1", null, null, null, "PM_AGRI", "processType1", null, "frezeth", null, null)
            );

            insertVariables.add(
                new InsertOrUpdateProcessVariableQueryParam(String.valueOf(i), "YEAR", null, String.valueOf(i), null, null, null, null, null, null, null)
            );

            insertOrUpdateTranslationsParams.add(
                new InsertOrUpdateTranslationsParam(TranslationObjectType.PROCESS, String.valueOf(i), TranslationAttribute.PROCESS_TITLE_TEMPLATE, "en", "Process title "+i, true)
            );

            for (int j = 0; j < 100; j++) {
                taskCreated.add(
                    new CreateTaskQueryParam(String.valueOf(i), i + "_" + j, "typeKey", Instant.now())
                );

                insertOrUpdateTranslationsParams.add(
                    new InsertOrUpdateTranslationsParam(TranslationObjectType.TASK, i + "_" + j, TranslationAttribute.TASK_TITLE, "en", "Task title "+(i+"_"+j), true)
                );

                if (j > 10){
                    completeTasks.add(new CompleteTaskQueryParam(String.valueOf(i), Instant.now()));
                }
            }
        }

        try (SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH, false)) {
            ProcessMapper mapper = session.getMapper(ProcessMapper.class);
            ProcessVariableMapper variableMapper = session.getMapper(ProcessVariableMapper.class);
            TranslationMapper translationMapper = session.getMapper(TranslationMapper.class);
            TaskMapper taskMapper = session.getMapper(TaskMapper.class);

            int i = 0;
            for (CreateProcessQueryParam p : processCreated){
                i++;
                mapper.insertOrUpdateProcess(p);
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }

            for (InsertOrUpdateProcessVariableQueryParam p : insertVariables){
                i++;
                variableMapper.insertOrUpdateProcessVariables(Set.of(p));
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }

            for (CreateTaskQueryParam p : taskCreated){
                i++;
                taskMapper.insertOrUpdateTask(p);
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }

            for (CompleteTaskQueryParam p : completeTasks){
                i++;
                taskMapper.completeTask(p);
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }

            for (InsertOrUpdateTranslationsParam p : insertOrUpdateTranslationsParams){
                i++;
                translationMapper.insertOrUpdateTranslations(Collections.singleton(p));
                if (i % 1000 == 0) {
                    session.flushStatements();
                }
            }

            session.flushStatements();
            session.commit();
        }

        System.out.println("push finished");

        long before = System.currentTimeMillis();
        List<SearchProcessQueryResponse> searchProcessQueryResponses = processQueries.searchProcesses(
                new SearchProcessRequestDto(
                        null,
                        null,
                        null,
                        "Process",
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
                Locale.FRENCH,
                "frezeth"
        );
        System.out.println(System.currentTimeMillis() - before);
        Assertions.assertEquals(1, searchProcessQueryResponses.size());
    }

}
