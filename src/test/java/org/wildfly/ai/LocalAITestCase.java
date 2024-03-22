/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.localai.LocalAiChatModel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Testing against: 
 * docker run -ti -p 8080:8080localai/localai:v2.9.0-ffmpeg-core mixtral-instruct
 *
 * @author Emmanuel Hugonnet (c) 2024 Red Hat, Inc.
 */
@Disabled
public class LocalAITestCase {

    @Test
    public void testHelloWorld() {
        ChatLanguageModel model = LocalAiChatModel.builder()
                .baseUrl("http://localhost:8080")
                .modelName("mixtral-instruct")
                .temperature(0.9)
                .build();

        String answer = model.generate("How are you?");
        System.out.println(answer);
    }
}
