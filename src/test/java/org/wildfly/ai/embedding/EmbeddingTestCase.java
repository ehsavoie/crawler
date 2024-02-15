/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai.embedding;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.content.Content;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.Query;
import dev.langchain4j.store.embedding.EmbeddingStore;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.wildfly.ai.document.loader.WildFlyHtmlContent;
import org.wildfly.ai.document.parser.HtmlDocumentParser;

/**
 *
 * podman run -d -v ollama:/home/ehsavoie/tmp/ollama -p 11434:11434 --name
 * ollama langchain4j/ollama-mistral:latest
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
public class EmbeddingTestCase {

    /**
     * Test of parsePage method, of class HtmlDocumentParser.
     */
    @Test
    public void testEmbedPageFromDocs() {
        WildFlyHtmlContent content = new WildFlyHtmlContent(new File("target").toPath().resolve("test-classes").resolve("admin_guide.html"), "en", "https://docs.wildfly.org/31/Admin_Guide.html", "https://docs.wildfly.org/31/");
        String cssSelector = ".sect3";
        String parentSelector = "h2";
        HtmlDocumentParser instance = new HtmlDocumentParser();
        List<TextSegment> result = instance.parsePage(content, cssSelector, parentSelector);
        assertEquals(190, result.size());
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();

        /*      EmbeddingModel embeddingModel = new OllamaEmbeddingModel.OllamaEmbeddingModelBuilder()
//                .baseUrl("http://ollama-mchomaredhatcom.apps.ai-hackathon.qic7.p1.openshiftapps.com")
                .baseUrl("http://localhost:11434")
                .modelName("mistral")
                .maxRetries(1)
                .timeout(Duration.ofSeconds(30))
                .build();*/
        EmbeddingStore<TextSegment> store = EmbeddingStoreFactory.createEmbeddingStore(result, embeddingModel);
        ContentRetriever contentRetriever = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(store)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();

        //String ollamaUrl = "http://localhost:11434";
        String ollamaUrl = "http://ollama-mchomaredhatcom.apps.ai-hackathon.qic7.p1.openshiftapps.com";

        ChatLanguageModel model = OllamaChatModel.builder()
                .baseUrl(ollamaUrl)
                .modelName("mistral:7b")
                .timeout(Duration.ofSeconds(90))
                .build();

        String prompt = "How do I set up a ConnectionFactory to a remote broker ?";
        List<Content> ragContents = contentRetriever.retrieve(Query.from(prompt));

        List<ChatMessage> messages = new ArrayList<>();
        messages.add(UserMessage.from(prompt));
        
        SystemMessage systemMessage = SystemMessage.systemMessage("You are a wildfly expert who understands well how to administrate the wildfly server and its components");
        String completePrompt = "Objective: answer the user question delimited by  ---\n"
                + "\n"
                + "---\n"
                + "%s\n"
                + "---"
                + "\n Here is a few data to help you:\n"
                + "";
        StringBuilder messageBuilder = new StringBuilder(String.format(completePrompt, prompt));
        for (Content ragContent : ragContents) {
            messageBuilder.append(ragContent.textSegment().text());
        }
        System.out.println("Answer " + model.generate(systemMessage, UserMessage.from(messageBuilder.toString())).content().text());
        //        AiServices.builder(CustomerSupportAgent.class)
        //                .chatLanguageModel(chatModel)
        //                .contentRetriever(contentRetriever)
        //                .chatMemory(chatMemory)
        //                .build();
    }

}