/*
 * Copyright 2018-2021 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.gchq.palisade.client.shell.shell;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;

import uk.gov.gchq.palisade.client.internal.impl.Configuration;

import java.net.URI;
import java.util.Optional;

public class CustomPromptProvider implements PromptProvider {
    private static final AttributedString USER_DELIM = new AttributedString("@");
    private static final AttributedString TOKEN_DELIM = new AttributedString("#");
    private static final AttributedString PROMPT = new AttributedString("> ");
    private static final AttributedString NO_ONE = new AttributedString("no-one", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
    private static final AttributedString DISCONNECTED = new AttributedString("disconnected", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));

    @Override
    public AttributedString getPrompt() {
        AttributedString userInfo = Optional.ofNullable(ClientShell.getSession())
                .map(session -> session.getConfiguration().<String>get(Configuration.USER_ID))
                .map(user -> new AttributedString(user, AttributedStyle.DEFAULT.foreground(AttributedStyle.BLUE)))
                .orElse(NO_ONE);

        AttributedString hostInfo = Optional.ofNullable(ClientShell.getSession())
                .map(session -> session.getConfiguration().<URI>get(Configuration.SPEC_URI).getHost())
                .map(host -> new AttributedString(host, AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN)))
                .orElse(DISCONNECTED);

        AttributedString delimitedTokenInfo = Optional.ofNullable(ClientShell.getSelectedToken())
                .map(token -> new AttributedString(token, AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)))
                .map(attrStr -> AttributedString.join(TOKEN_DELIM, attrStr))
                .orElse(AttributedString.EMPTY);

        return AttributedString.join(AttributedString.EMPTY,
                userInfo, USER_DELIM, hostInfo, delimitedTokenInfo, PROMPT);
    }
}
