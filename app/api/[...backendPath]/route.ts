import { NextResponse } from "next/server";

import { backendApiBaseUrl } from "@/lib/backend-api";

type RouteContext = {
  params: Promise<{
    backendPath: string[];
  }>;
};

type ProxyRequestInit = RequestInit & {
  duplex?: "half";
};

const hopByHopHeaders = new Set([
  "connection",
  "content-encoding",
  "content-length",
  "host",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
]);

export async function GET(request: Request, context: RouteContext) {
  return proxyBackend(request, context);
}

export async function POST(request: Request, context: RouteContext) {
  return proxyBackend(request, context);
}

export async function PUT(request: Request, context: RouteContext) {
  return proxyBackend(request, context);
}

export async function PATCH(request: Request, context: RouteContext) {
  return proxyBackend(request, context);
}

export async function DELETE(request: Request, context: RouteContext) {
  return proxyBackend(request, context);
}

async function proxyBackend(request: Request, context: RouteContext) {
  const targetUrl = await buildTargetUrl(request, context);

  if (!targetUrl.ok) {
    return targetUrl.response;
  }

  try {
    const body = request.method === "GET" || request.method === "HEAD"
      ? undefined
      : request.body;
    const init: ProxyRequestInit = {
      body,
      cache: "no-store",
      duplex: body ? "half" : undefined,
      headers: proxiedRequestHeaders(request.headers),
      method: request.method,
      redirect: "manual",
    };
    const response = await fetch(targetUrl.url, init);

    return new Response(response.body, {
      headers: proxiedResponseHeaders(response.headers),
      status: response.status,
      statusText: response.statusText,
    });
  } catch (error) {
    const detail = error instanceof Error ? error.message : "Request failed";

    return NextResponse.json(
      {
        success: false,
        message:
          `Backend API unavailable. Check API_BASE_URL/BACKEND_API_BASE_URL and make sure Spring Boot is running. ${detail}`,
        errorCode: "BACKEND_UNREACHABLE",
      },
      { status: 502 },
    );
  }
}

async function buildTargetUrl(request: Request, context: RouteContext) {
  const backendBaseUrl = backendApiBaseUrl();

  if (!backendBaseUrl) {
    return {
      ok: false as const,
      response: NextResponse.json(
        {
          success: false,
          message:
            "Backend API URL is not configured. Set API_BASE_URL or BACKEND_API_BASE_URL to your Spring Boot backend URL.",
          errorCode: "BACKEND_URL_MISSING",
        },
        { status: 500 },
      ),
    };
  }

  const frontendOrigin = new URL(request.url).origin;
  const backendOrigin = new URL(backendBaseUrl).origin;

  if (frontendOrigin === backendOrigin) {
    return {
      ok: false as const,
      response: NextResponse.json(
        {
          success: false,
          message:
            "Backend API URL points to the frontend origin. Set API_BASE_URL or BACKEND_API_BASE_URL to the Spring Boot backend URL.",
          errorCode: "BACKEND_URL_LOOP",
        },
        { status: 500 },
      ),
    };
  }

  const { backendPath } = await context.params;
  const encodedPath = backendPath.map(encodeURIComponent).join("/");
  const { search } = new URL(request.url);

  return {
    ok: true as const,
    url: new URL(`/api/${encodedPath}${search}`, `${backendBaseUrl}/`),
  };
}

function proxiedRequestHeaders(headers: Headers) {
  const nextHeaders = new Headers(headers);

  hopByHopHeaders.forEach((header) => nextHeaders.delete(header));
  nextHeaders.delete("access-control-request-headers");
  nextHeaders.delete("access-control-request-method");
  nextHeaders.delete("origin");

  return nextHeaders;
}

function proxiedResponseHeaders(headers: Headers) {
  const nextHeaders = new Headers(headers);

  hopByHopHeaders.forEach((header) => nextHeaders.delete(header));

  return nextHeaders;
}
