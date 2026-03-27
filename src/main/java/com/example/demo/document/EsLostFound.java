package com.example.demo.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Document(indexName = "lost_found") // 对应 ES 里面的索引名称
public class EsLostFound {
    
    @Id
    private Long id; // 与 MySQL 的主键保持一致

    @Field(type = FieldType.Integer)
    private Integer type;

    // 对物品名称和描述进行分词 (ik分词器是中文最常用的分词器，如果没装可以先用 standard)
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String itemName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String description;

    @Field(type = FieldType.Keyword) // 不分词，精确匹配
    private String location;

    @Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createTime;
}