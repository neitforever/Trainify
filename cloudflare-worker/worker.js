const ROUTES = {
  "/recognize-equipment": "gemini-2.5-flash",
  "/generate-exercise": "gemini-3-flash-preview",
  "/generate-template": "gemini-3.5-flash",
  "/translate": "gemini-3.1-flash-lite",
  "/suggest-exercise-selection": "gemini-3.1-flash-lite",
  "/analyze-exercise-technique": "gemini-3.1-flash-lite"
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
  const rankedResult = await searchAndRankTechniqueVideos({
    exerciseName,
    lang,
    maxResults,
    env,
    channelConfig
  });

  return jsonResponse({
    ...rankedResult,
    lang,
    preferredChannel: channelConfig.channelName,
    preferredHandle: channelConfig.channelHandle,
    channelId: channelConfig.channelId || null,
    fallbackUsed: rankedResult.videos.some((video) => video.source === "youtube_fallback")
  });
}

async function searchAndRankTechniqueVideos({ exerciseName, lang, maxResults, env, channelConfig }) {
  const plans = buildTechniqueSearchPlans(exerciseName, lang, channelConfig);
  const found = new Map();
  const queries = [];
  const youtubeErrors = [];

  for (const plan of plans) {
    const result = await searchTechniqueVideos({
      query: plan.query,
      maxResults: 8,
      env,
      channelConfig,
      source: plan.source,
      channelId: plan.channelId || ""
    });

    queries.push(result.query);
    if (result.youtubeError) youtubeErrors.push(result.youtubeError);

    for (const video of result.videos) {
      const previous = found.get(video.videoId);
      if (!previous || techniqueSourcePriority(video.source) > techniqueSourcePriority(previous.source)) {
        found.set(video.videoId, video);
      }
    }

    if (found.size >= 14) break;
  }

  const candidates = Array.from(found.values());
  const detailedCandidates = await attachTechniqueVideoDetails(candidates, env);
  const rankedVideos = rankTechniqueVideos(detailedCandidates, exerciseName, lang)
    .filter((video) => video.score >= 25)
    .slice(0, maxResults)
    .map(({ score, description, durationSeconds, viewCount, ...video }) => ({
      ...video,
      relevanceScore: Math.round(score),
      durationSeconds,
      viewCount
    }));

  return {
    query: queries[0] || "",
    queries,
    videos: rankedVideos,
    youtubeError: youtubeErrors[0] || undefined
  };
}

async function searchTechniqueVideos({ query, maxResults, env, channelConfig, source, channelId }) {
  const searchUrl = new URL("https://www.googleapis.com/youtube/v3/search");
  searchUrl.searchParams.set("part", "snippet");
  searchUrl.searchParams.set("type", "video");
  searchUrl.searchParams.set("q", query);
  searchUrl.searchParams.set("maxResults", String(maxResults));
  searchUrl.searchParams.set("videoEmbeddable", "true");
  searchUrl.searchParams.set("videoSyndicated", "true");
  searchUrl.searchParams.set("videoDuration", "short");
  searchUrl.searchParams.set("safeSearch", "strict");
  searchUrl.searchParams.set("order", "relevance");

  if (channelId) {
    searchUrl.searchParams.set("channelId", channelId);
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
        title: decodeHtmlEntities(snippet.title || ""),
        description: decodeHtmlEntities(snippet.description || ""),
        channelTitle: decodeHtmlEntities(snippet.channelTitle || ""),
        thumbnailUrl,
        youtubeUrl: `https://www.youtube.com/watch?v=${videoId}`,
        source
      };
    })
    .filter((video) => /^[a-zA-Z0-9_-]{11}$/.test(video.videoId));

  if (source === "youtube_preferred_channel" && !channelId) {
    videos = videos.filter((video) => {
      const normalizedTitle = normalizeChannelName(video.channelTitle);
      return normalizedTitle.includes(normalizeChannelName(channelConfig.channelName)) || normalizedTitle.includes(normalizeChannelName(channelConfig.channelHandle));
    });
  }

  return { query, videos };
}

async function attachTechniqueVideoDetails(videos, env) {
  if (videos.length === 0) return [];

  const detailsUrl = new URL("https://www.googleapis.com/youtube/v3/videos");
  detailsUrl.searchParams.set("part", "snippet,contentDetails,statistics,status");
  detailsUrl.searchParams.set("id", videos.map((video) => video.videoId).join(","));
  detailsUrl.searchParams.set("key", env.YOUTUBE_API_KEY);

  const response = await fetch(detailsUrl.toString());
  if (!response.ok) return videos;

  const body = await response.json();
  const detailsById = new Map((body.items || []).map((item) => [item.id, item]));

  return videos.map((video) => {
    const item = detailsById.get(video.videoId);
    if (!item) return video;

    const snippet = item.snippet || {};
    const statistics = item.statistics || {};
    const status = item.status || {};

    return {
      ...video,
      title: decodeHtmlEntities(snippet.title || video.title),
      description: decodeHtmlEntities(snippet.description || video.description || ""),
      channelTitle: decodeHtmlEntities(snippet.channelTitle || video.channelTitle),
      durationSeconds: parseIsoDurationToSeconds(item.contentDetails?.duration || ""),
      viewCount: Number(statistics.viewCount || 0),
      embeddable: status.embeddable !== false
    };
  }).filter((video) => video.embeddable !== false);
}

function rankTechniqueVideos(videos, exerciseName, lang) {
  const exerciseTokens = tokenizeTechniqueText(exerciseName);
  const normalizedExercise = normalizeTechniqueText(exerciseName);
  const normalizedLang = (lang || "en").toLowerCase();
  const techniqueWords = normalizedLang === "en"
    ? ["exercise", "technique", "tutorial", "proper", "form", "how", "perform"]
    : ["техника", "выполнение", "упражнение", "как", "правильно", "делать"];
  const negativeWords = [
    "workout", "routine", "program", "challenge", "motivation", "compilation", "full body",
    "тренировка", "программа", "комплекс", "мотивация", "подборка", "челлендж"
  ];

  return videos.map((video, index) => {
    const title = normalizeTechniqueText(video.title);
    const description = normalizeTechniqueText(video.description || "");
    const combined = `${title} ${description}`;
    const titleTokens = tokenizeTechniqueText(video.title);
    const combinedTokens = tokenizeTechniqueText(combined);

    const titleOverlap = tokenOverlapRatio(exerciseTokens, titleTokens);
    const combinedOverlap = tokenOverlapRatio(exerciseTokens, combinedTokens);
    let score = 0;

    score += titleOverlap * 55;
    score += combinedOverlap * 20;

    if (title.includes(normalizedExercise)) score += 35;
    if (combined.includes(normalizedExercise)) score += 12;

    for (const word of techniqueWords) {
      if (combined.includes(word)) score += 3;
    }

    for (const word of negativeWords) {
      if (combined.includes(word)) score -= 14;
    }

    const duration = Number(video.durationSeconds || 0);
    if (duration > 0) {
      if (duration <= 20) score -= 6;
      else if (duration <= 90) score += 14;
      else if (duration <= 240) score += 9;
      else if (duration <= 600) score -= 7;
      else score -= 22;
    }

    if (video.source === "youtube_preferred_channel") score += 8;
    score += Math.max(0, 8 - index * 0.8);
    score += Math.min(5, Math.log10(Number(video.viewCount || 0) + 1));

    return { ...video, score };
  }).sort((a, b) => b.score - a.score);
}

function buildTechniqueSearchPlans(exerciseName, lang, channelConfig) {
  const normalized = exerciseName.replace(/[_-]+/g, " ").replace(/\s+/g, " ").trim();
  const simplified = simplifyExerciseName(normalized);
  const normalizedLang = (lang || "en").toLowerCase();
  const isEnglish = normalizedLang === "en";
  const exactSuffix = isEnglish ? "exercise technique" : "техника выполнения упражнение";
  const formSuffix = isEnglish ? "proper form tutorial" : "как правильно делать упражнение";
  const plans = [];

  plans.push({ query: `"${normalized}" ${exactSuffix}`, source: "youtube_preferred_channel", channelId: channelConfig.channelId || "" });
  if (simplified !== normalized) {
    plans.push({ query: `"${simplified}" ${exactSuffix}`, source: "youtube_preferred_channel", channelId: channelConfig.channelId || "" });
  }
  plans.push({ query: `${normalized} ${formSuffix}`, source: "youtube_fallback", channelId: "" });
  plans.push({ query: `"${normalized}" ${exactSuffix}`, source: "youtube_fallback", channelId: "" });
  if (simplified !== normalized) {
    plans.push({ query: `${simplified} ${formSuffix}`, source: "youtube_fallback", channelId: "" });
  }

  if (!channelConfig.channelId) {
    return plans.map((plan) => plan.source === "youtube_preferred_channel"
      ? { ...plan, query: `${plan.query} ${channelConfig.channelName}` }
      : plan
    );
  }

  return plans;
}

function simplifyExerciseName(value) {
  return String(value || "")
    .replace(/\b(single|double|alternating|standing|seated|lying|machine|bodyweight|weighted)\b/gi, "")
    .replace(/\b(одной|двумя|попеременно|стоя|сидя|лежа|лёжа|тренажере|тренажёре)\b/gi, "")
    .replace(/\s+/g, " ")
    .trim();
}

function normalizeTechniqueText(value) {
  return String(value || "")
    .toLowerCase()
    .replace(/&quot;/g, " ")
    .replace(/&#39;/g, " ")
    .replace(/[^a-z0-9а-яё\s]/gi, " ")
    .replace(/\s+/g, " ")
    .trim();
}

function tokenizeTechniqueText(value) {
  const stopWords = new Set([
    "the", "and", "with", "for", "how", "to", "a", "an", "of", "in", "on", "by",
    "и", "с", "со", "для", "как", "на", "в", "во", "по", "из", "от", "к", "ко", "у"
  ]);
  return normalizeTechniqueText(value)
    .split(" ")
    .filter((token) => token.length >= 3 && !stopWords.has(token));
}

function tokenOverlapRatio(sourceTokens, targetTokens) {
  if (!sourceTokens.length || !targetTokens.length) return 0;
  const target = new Set(targetTokens);
  const matched = sourceTokens.filter((token) => target.has(token)).length;
  return matched / sourceTokens.length;
}

function parseIsoDurationToSeconds(duration) {
  const match = String(duration || "").match(/^PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?$/);
  if (!match) return 0;
  return Number(match[1] || 0) * 3600 + Number(match[2] || 0) * 60 + Number(match[3] || 0);
}

function techniqueSourcePriority(source) {
  return source === "youtube_preferred_channel" ? 2 : 1;
}

function decodeHtmlEntities(value) {
  return String(value || "")
    .replace(/&amp;/g, "&")
    .replace(/&quot;/g, '"')
    .replace(/&#39;/g, "'")
    .replace(/&lt;/g, "<")
    .replace(/&gt;/g, ">");
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
