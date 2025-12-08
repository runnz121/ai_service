from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from typing import Optional, List, Dict, Any
import httpx
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="FastMCP Server", version="1.0.0")

SEARCH_API_URL = "http://search-api:8081"


class SearchRequest(BaseModel):
    query: str
    category: Optional[str] = None
    index_type: Optional[str] = None


class MCPResponse(BaseModel):
    success: bool
    data: Optional[Any] = None
    error: Optional[str] = None
    source: Optional[str] = None


def determine_best_index(query: str, category: Optional[str] = None) -> tuple[str, str]:
    """
    쿼리를 분석하여 가장 적합한 인덱스와 엔드포인트를 결정합니다.
    """
    query_lower = query.lower()

    # 키워드 기반으로 인덱스 결정
    if any(keyword in query_lower for keyword in ['product', 'item', 'buy', 'purchase', 'price']):
        return "products", "/api/products/search"

    # 카테고리가 명시된 경우
    if category:
        return "products", "/api/products/search"

    # 기본값: products 인덱스
    return "products", "/api/products/search"


async def call_search_api(endpoint: str, payload: dict) -> dict:
    """
    search-api를 호출합니다.
    """
    url = f"{SEARCH_API_URL}{endpoint}"

    try:
        async with httpx.AsyncClient(timeout=30.0) as client:
            logger.info(f"Calling search-api: {url} with payload: {payload}")
            response = await client.post(url, json=payload)
            response.raise_for_status()
            return response.json()
    except httpx.HTTPError as e:
        logger.error(f"Error calling search-api: {str(e)}")
        raise HTTPException(status_code=500, detail=f"Search API error: {str(e)}")


@app.get("/health")
async def health_check():
    """
    헬스 체크 엔드포인트
    """
    return {"status": "healthy", "service": "fastmcp-server"}


@app.post("/mcp/search", response_model=MCPResponse)
async def mcp_search(request: SearchRequest):
    """
    MCP 검색 엔드포인트
    api-service로부터 요청을 받아 적절한 인덱스를 선택하고 search-api를 호출합니다.
    """
    try:
        logger.info(f"Received search request: {request}")

        # 가장 적합한 인덱스 및 엔드포인트 결정
        index_name, endpoint = determine_best_index(request.query, request.category)

        logger.info(f"Selected index: {index_name}, endpoint: {endpoint}")

        # search-api 호출을 위한 payload 구성
        search_payload = {
            "keyword": request.query,
            "page": 0,
            "size": 10
        }

        if request.category:
            search_payload["category"] = request.category

        # search-api 호출
        search_result = await call_search_api(endpoint, search_payload)

        return MCPResponse(
            success=True,
            data=search_result,
            source=f"elasticsearch:{index_name}",
            error=None
        )

    except HTTPException as e:
        logger.error(f"HTTP error in mcp_search: {str(e)}")
        return MCPResponse(
            success=False,
            data=None,
            source=None,
            error=str(e.detail)
        )
    except Exception as e:
        logger.error(f"Unexpected error in mcp_search: {str(e)}")
        return MCPResponse(
            success=False,
            data=None,
            source=None,
            error=str(e)
        )


@app.post("/mcp/tools/list")
async def list_tools():
    """
    사용 가능한 도구 목록을 반환합니다.
    """
    return {
        "tools": [
            {
                "name": "search_products",
                "description": "Search for products in Elasticsearch",
                "parameters": {
                    "query": "string (required) - search query",
                    "category": "string (optional) - product category filter"
                }
            }
        ]
    }


@app.get("/")
async def root():
    """
    루트 엔드포인트
    """
    return {
        "service": "FastMCP Server",
        "version": "1.0.0",
        "status": "running",
        "endpoints": {
            "health": "/health",
            "search": "/mcp/search",
            "tools": "/mcp/tools/list"
        }
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8082, log_level="info")
