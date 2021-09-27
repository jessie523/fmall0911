package com.my.fmall.list;

import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = FmallListServiceApplication.class)
public class FmallListServiceApplicationTests {

    @Autowired
    JestClient jestClient;

    /**
     * 1 定义dsl语句
     * 2 定义执行动作
     * 3 执行动作
     * 4 获取执行之后的结果集
     * @throws IOException
     */
    @Test
  public  void testEs() throws IOException {
        String query="{\n" +
                "  \"query\": {\n" +
                "    \"match\": {\n" +
                "      \"actorList.name\": \"zhang yi\"\n" +
                "    }\n" +
                "  }\n" +
                "}";
        Search search = new Search.Builder(query).addIndex("movie_index").addType("movie").build();

        SearchResult result = jestClient.execute(search);

        List<SearchResult.Hit<HashMap, Void>> hits = result.getHits(HashMap.class);

        for (SearchResult.Hit<HashMap, Void> hit : hits) {
            HashMap source = hit.source;
            System.err.println("source = " + source);

        }
    }

}
