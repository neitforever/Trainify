const ROUTES = {
  "/recognize-equipment": "gemini-2.5-flash",
  "/generate-exercise": "gemma-4-31b-it",
  "/generate-template": "gemma-4-31b-it"
};

export default {
  async fetch(request, env) {
    const url = new URL(request.url);

    if (request.method === "OPTIONS") {
      return new Response(null, { status: 204, headers: corsHeaders() });
    }

    if (url.pathname === "/exercise-technique-videos") {
      if (request.method !== "GET") {
        return jsonResponse({ error: "Method not allowed" }, 405);
      }
      return handleExerciseTechniqueVideos(url, env);
    }

    if (request.method !== "POST") {
      return jsonResponse({ error: "Method not allowed" }, 405);
    }

    const model = ROUTES[url.pathname];
    if (!model) {
      return jsonResponse({ error: "Not found", allowed_paths: [...Object.keys(ROUTES), "/exercise-technique-videos"] }, 404);
    }

    if (!env.GEMINI_API_KEY) {
      return jsonResponse({ error: "GEMINI_API_KEY secret is not configured" }, 500);
    }

    let requestBody;
    try {
      requestBody = await request.text();
      if (!requestBody || requestBody.trim().length === 0) {
        return jsonResponse({ error: "Request body is empty" }, 400);
      }
      JSON.parse(requestBody);
    } catch (error) {
      return jsonResponse({ error: "Invalid JSON request body", details: String(error?.message || error) }, 400);
    }

    const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/${model}:generateContent`;

    let geminiResponse;
    try {
      geminiResponse = await fetch(geminiUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY
        },
        body: requestBody
      });
    } catch (error) {
      return jsonResponse({ error: "Failed to call Gemini API", details: String(error?.message || error) }, 502);
    }

    const responseBody = await geminiResponse.text();
    return new Response(responseBody, {
      status: geminiResponse.status,
      headers: {
        "Content-Type": geminiResponse.headers.get("Content-Type") || "application/json",
        ...corsHeaders()
      }
    });
  }
};

async function handleExerciseTechniqueVideos(url, env) {
  if (!env.YOUTUBE_API_KEY) {
    return jsonResponse({ error: "YOUTUBE_API_KEY secret is not configured" }, 500);
  }

  const exerciseName = (url.searchParams.get("exerciseName") || "").trim();
  const lang = (url.searchParams.get("lang") || "en").trim();
  const maxResults = clampNumber(Number(url.searchParams.get("maxResults") || 5), 1, 10);

  if (!exerciseName) {
    return jsonResponse({ error: "exerciseName query parameter is required" }, 400);
  }

  const channelConfig = await buildTechniqueChannelConfig(lang, env);
  const preferredResult = await searchTechniqueVideos({
    exerciseName,
    lang,
    maxResults,
    env,
    channelConfig,
    usePreferredChannel: true
  });

  if (preferredResult.videos.length > 0) {
    return jsonResponse({
      ...preferredResult,
      lang,
      preferredChannel: channelConfig.channelName,
      preferredHandle: channelConfig.channelHandle,
      channelId: channelConfig.channelId || null,
      fallbackUsed: false
    });
  }

  const fallbackResult = await searchTechniqueVideos({
    exerciseName,
    lang,
    maxResults,
    env,
    channelConfig,
    usePreferredChannel: false
  });

  return jsonResponse({
    ...fallbackResult,
    lang,
    preferredChannel: channelConfig.channelName,
    preferredHandle: channelConfig.channelHandle,
    channelId: channelConfig.channelId || null,
    fallbackUsed: true
  });
}

async function searchTechniqueVideos({ exerciseName, lang, maxResults, env, channelConfig, usePreferredChannel }) {
  const query = buildTechniqueQuery(exerciseName, lang, channelConfig, usePreferredChannel);
  const searchUrl = new URL("https://www.googleapis.com/youtube/v3/search");
  searchUrl.searchParams.set("part", "snippet");
  searchUrl.searchParams.set("type", "video");
  searchUrl.searchParams.set("q", query);
  searchUrl.searchParams.set("maxResults", String(maxResults));
  searchUrl.searchParams.set("videoEmbeddable", "true");
  searchUrl.searchParams.set("videoSyndicated", "true");
  searchUrl.searchParams.set("videoDuration", "short");
  searchUrl.searchParams.set("safeSearch", "strict");

  if (usePreferredChannel && channelConfig.channelId) {
    searchUrl.searchParams.set("channelId", channelConfig.channelId);
  }

  searchUrl.searchParams.set("key", env.YOUTUBE_API_KEY);

  const searchResponse = await fetch(searchUrl.toString());
  const searchBody = await searchResponse.json();
  if (!searchResponse.ok) {
    return { query, videos: [], youtubeError: searchBody };
  }

  let videos = (searchBody.items || [])
    .map((item) => {
      const videoId = item?.id?.videoId || "";
      const snippet = item?.snippet || {};
      const thumbnailUrl = snippet?.thumbnails?.high?.url || snippet?.thumbnails?.medium?.url || snippet?.thumbnails?.default?.url || "";
      return {
        videoId,
        title: snippet.title || "",
        channelTitle: snippet.channelTitle || "",
        thumbnailUrl,
        youtubeUrl: `https://www.youtube.com/shorts/${videoId}`,
        source: usePreferredChannel ? "youtube_preferred_channel" : "youtube_fallback"
      };
    })
    .filter((video) => /^[a-zA-Z0-9_-]{11}$/.test(video.videoId));

  if (usePreferredChannel && !channelConfig.channelId) {
    videos = videos.filter((video) => {
      const normalizedTitle = normalizeChannelName(video.channelTitle);
      return normalizedTitle.includes(normalizeChannelName(channelConfig.channelName)) || normalizedTitle.includes(normalizeChannelName(channelConfig.channelHandle));
    });
  }

  return { query, videos };
}

async function buildTechniqueChannelConfig(lang, env) {
  const normalizedLang = (lang || "en").toLowerCase();
  const isEnglish = normalizedLang === "en";
  const channelName = isEnglish ? "DeltaBolic" : "anti_trainer";
  const channelHandle = isEnglish ? "@DeltaBolic" : "@anti_trainer";
  const explicitChannelId = isEnglish ? env.DELTABOLIC_CHANNEL_ID : env.ANTITRAINER_CHANNEL_ID;

  return {
    channelName,
    channelHandle,
    channelId: explicitChannelId || await resolveChannelIdByHandle(channelHandle, env)
  };
}

async function resolveChannelIdByHandle(handle, env) {
  const normalizedHandle = String(handle || "").trim();
  if (!normalizedHandle || !env.YOUTUBE_API_KEY) return "";

  const cache = caches.default;
  const cacheKey = new Request(`https://trainify.local/youtube-channel-id/${encodeURIComponent(normalizedHandle)}`);
  const cached = await cache.match(cacheKey);
  if (cached) {
    const cachedBody = await cached.json();
    if (cachedBody?.channelId) return cachedBody.channelId;
  }

  let channelId = "";
  for (const handleCandidate of [normalizedHandle, normalizedHandle.replace(/^@/, "")]) {
    const channelUrl = new URL("https://www.googleapis.com/youtube/v3/channels");
    channelUrl.searchParams.set("part", "id,snippet");
    channelUrl.searchParams.set("forHandle", handleCandidate);
    channelUrl.searchParams.set("key", env.YOUTUBE_API_KEY);

    const response = await fetch(channelUrl.toString());
    if (!response.ok) continue;

    const body = await response.json();
    channelId = body?.items?.[0]?.id || "";
    if (channelId) break;
  }

  if (!channelId) return "";

  await cache.put(
    cacheKey,
    new Response(JSON.stringify({ channelId }), {
      headers: {
        "Content-Type": "application/json",
        "Cache-Control": "public, max-age=604800"
      }
    })
  );

  return channelId;
}

function buildTechniqueQuery(exerciseName, lang, channelConfig, usePreferredChannel = true) {
  const normalized = exerciseName.replace(/[_-]+/g, " ").trim();
  const normalizedLang = (lang || "en").toLowerCase();

  if (!usePreferredChannel) {
    if (normalizedLang === "en") return `${normalized} exercise technique shorts`;
    return `${normalized} техника выполнения упражнение shorts`;
  }

  if (channelConfig.channelId) {
    if (normalizedLang === "en") return `${normalized} exercise technique shorts`;
    return `${normalized} техника выполнения упражнение shorts`;
  }

  if (normalizedLang === "en") {
    return `${normalized} exercise technique shorts ${channelConfig.channelName}`;
  }

  return `${normalized} техника выполнения упражнение shorts ${channelConfig.channelName}`;
}

function normalizeChannelName(value) {
  return String(value || "")
    .toLowerCase()
    .replace(/[^a-z0-9а-яё]/gi, "");
}

function clampNumber(value, min, max) {
  if (!Number.isFinite(value)) return min;
  return Math.min(Math.max(value, min), max);
}

function jsonResponse(payload, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "Content-Type": "application/json", ...corsHeaders() }
  });
}

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type"
  };
}
