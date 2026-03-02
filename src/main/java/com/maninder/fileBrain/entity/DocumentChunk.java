package com.maninder.fileBrain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "document_chunks")
@Getter
@Setter
public class DocumentChunk extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "vector")
    @JdbcTypeCode(SqlTypes.VECTOR)  // ← Critical: tells Hibernate this is a vector type
    @Array(length = 1536)           // ← Set to your embedding dimension
    private float[] embedding;

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;
}
