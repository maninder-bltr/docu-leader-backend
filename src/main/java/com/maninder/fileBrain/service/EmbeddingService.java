package com.maninder.fileBrain.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public float[] generateEmbedding(String text) {
        float[] embedding = embeddingModel.embed(text);
        float[] floatArray = new float[embedding.length];
        System.arraycopy(embedding, 0, floatArray, 0, embedding.length);
        return floatArray;
    }
}
