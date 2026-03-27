package com.example.demo.repository;

import com.example.demo.document.EsLostFound;
import org.springframework.data.elasticsearch.annotations.Highlight;
import org.springframework.data.elasticsearch.annotations.HighlightField;
import org.springframework.data.elasticsearch.annotations.HighlightParameters;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EsLostFoundRepository extends ElasticsearchRepository<EsLostFound, Long> {
    
    // 配置高亮字段，并自定义高亮标签 (这里前端拿到后可以直接用 CSS 渲染成红色)
    @Highlight(
        fields = {
            @HighlightField(name = "itemName"),
            @HighlightField(name = "description")
        },
        parameters = @HighlightParameters(preTags = "<span style='color:red; font-weight:bold;'>", postTags = "</span>")
    )
    SearchHits<EsLostFound> findByItemNameOrDescription(String itemName, String description);
}