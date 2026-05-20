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

    if (request.method !== "POST") {
      return jsonResponse({ error: "Method not allowed" }, 405);
    }

    const model = ROUTES[url.pathname];
    if (!model) {
      return jsonResponse({ error: "Not found", allowed_paths: Object.keys(ROUTES) }, 404);
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

function jsonResponse(payload, status = 200) {
  return new Response(JSON.stringify(payload), {
    status,
    headers: { "Content-Type": "application/json", ...corsHeaders() }
  });
}

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type"
  };
}
