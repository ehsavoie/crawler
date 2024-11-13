/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.embedding;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.embedding.neo4j.Neo4jEmbeddingStore;
import dev.langchain4j.store.embedding.weaviate.WeaviateEmbeddingStore;
import java.nio.file.Path;
import java.util.List;

/**
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class EmbeddingStoreFactory {
    
    public static EmbeddingStore<TextSegment> createEmbeddingStore(List<Document> documents, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        return embeddingStore;
    }
    /**
     * rm -Rf /home/ehugonne/.local/share/containers/storage/volumes/weaviate/_data/*
     * podman run --rm -p 8090:8080 -p 50051:50051 -v /home/ehugonne/\.local/share/containers/storage/volumes/weaviate/_data:/data --name=weaviate cr.weaviate.io/semitechnologies/weaviate:1.24.
     * @param documents
     * @param embeddingModel
     * @param metadata
     * @return 
     */
    public static EmbeddingStore<TextSegment> createWeaviateEmbeddingStore(List<Document> documents, EmbeddingModel embeddingModel, List<String> metadata) {
        EmbeddingStore<TextSegment> embeddingStore = WeaviateEmbeddingStore.builder()
                .scheme("http")
                .host("localhost")
                .port(8090)
                .objectClass("Simple")
                .avoidDups(true)
                .consistencyLevel("ALL")
                .metadataKeys(metadata)
                .build();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        return embeddingStore;
    }
    public static EmbeddingStore<TextSegment> createNeo4jEmbeddingStore(List<Document> documents, EmbeddingModel embeddingModel) {
        EmbeddingStore<TextSegment> embeddingStore = Neo4jEmbeddingStore.builder()
                .withBasicAuth("neo4j://localhost:7687", "neo4j", "neo4jpassword")
                .dimension(384)
                .build();
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 100))
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();
        ingestor.ingest(documents);
        return embeddingStore;
    }
    
    public static EmbeddingStore<TextSegment> loadEmbeddingStore(Path filePath) {
        return InMemoryEmbeddingStore.fromFile(filePath);
    }
}
