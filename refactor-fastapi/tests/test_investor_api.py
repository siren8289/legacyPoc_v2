from __future__ import annotations

from fastapi.testclient import TestClient

from app.common.search_index import InvestorSearchIndex


def test_search_index_narrows_first_char_bucket() -> None:
    index = InvestorSearchIndex()
    index.register(2001, "김민수")
    index.register(2002, "이서연")
    index.register(2003, "박준호")
    assert index.candidate_ids("김") == [2001]
    assert index.candidate_ids("") is None


def test_list_and_name_search(memory_client: TestClient) -> None:
    all_rows = memory_client.get("/api/investors").json()["data"]
    assert [row["investorId"] for row in all_rows] == [2001, 2002, 2003]
    kim = memory_client.get("/api/investors", params={"name": "김"}).json()["data"]
    assert [row["investorName"] for row in kim] == ["김민수"]


def test_investor_page_projection(memory_client: TestClient) -> None:
    body = memory_client.get("/api/investors", params={"page": 0, "size": 2}).json()["data"]
    assert body["totalCount"] == 3
    assert set(body["items"][0].keys()) == {"investorId", "investorName", "investorGrade", "totalAmount"}


def test_create_investor(memory_client: TestClient) -> None:
    created = memory_client.post(
        "/api/investors",
        json={"investorName": "최수아", "investorGrade": "GOLD", "totalAmount": 10},
    )
    assert created.json() == {"result": "OK", "investorId": 2004}
    detail = memory_client.get("/api/investors/2004")
    assert detail.json()["data"]["investorName"] == "최수아"
