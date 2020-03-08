// ---
// Copyright 2020 netty-agents team
// All rights reserved
// ---
package nettyagents;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface Constants {

	Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
	int DEFAULT_REQUEST_TIMEOUT_SEC = 10;
	int DEFAULT_CHANNEL_WAIT_SEC = 10;
	String DEFAULT_SERVER_HOST = "127.0.0.1";
	int DEFAULT_SERVER_PORT = 4444;
}
