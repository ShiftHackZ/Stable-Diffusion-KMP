#!/usr/bin/env python3
"""Static whole-document and consumed DTO parity gate; does not contact an account/server."""

import json
import re
import sys
from pathlib import Path

sys.dont_write_bytecode = True
from test_openai_compatible_contract import validate

ROOT = Path(__file__).resolve().parents[1]
FEATURE = ROOT / "feature/openai-oauth/src/commonMain/kotlin/com/shifthackz/aisdv1/feature/openaioauth"
CONTRACT = ROOT / "network/openai-oauth.openapi.json"


def test_contract():
    document = json.loads(CONTRACT.read_text())
    validate(document, compatible_client=False)
    expected = {
        "/api/accounts/deviceauth/usercode": ("DeviceCodeRequest", "DeviceCodeResponse"),
        "/api/accounts/deviceauth/token": ("DevicePollRequest", "DevicePollResponse"),
        "/oauth/token": ("TokenExchangeRequest", "TokenResponse"),
        "/backend-api/codex/images/generations": ("ImageRequest", "ImageResponse"),
    }
    for route, (request, response) in expected.items():
        operation = document["paths"][route]["post"]
        request_body = json.dumps(operation["requestBody"])
        success_body = json.dumps(operation["responses"]["200"])
        assert "#/components/schemas/" + request in request_body, route
        assert "#/components/schemas/" + response in success_body, route
    token_body = json.dumps(document["paths"]["/oauth/token"]["post"]["requestBody"])
    assert "#/components/schemas/TokenRefreshRequest" in token_body
    assert document["paths"]["/backend-api/codex/images/generations"]["post"]["security"] == [{"chatGptBearer": []}]
    definitions = document["components"]["schemas"]
    for name in (
        "DeviceCodeRequest", "DeviceCodeResponse", "DevicePollRequest", "DevicePollResponse",
        "TokenResponse", "ImageRequest", "ImageResponse", "ImageData",
    ):
        source = (FEATURE / "model" / f"{name}.kt").read_text()
        declaration = re.search(r"internal class " + name + r"\((.*?)\)(?=\s*(?:\n|$))", source, re.S)
        assert declaration is not None, name
        fields = re.findall(r'(?:@SerialName\("([^"\n]+)"\)\s*)?val\s+(\w+)\s*:', declaration.group(1))
        assert {wire or field for wire, field in fields} == set(definitions[name]["properties"]), name
    api = (FEATURE / "api/OAuthApi.kt").read_text()
    for method, schema in (("exchange", "TokenExchangeRequest"), ("refresh", "TokenRefreshRequest")):
        method_body = re.search(r"suspend fun " + method + r"\(.*?Parameters.build\s*\{(.*?)\}\)", api, re.S)
        assert method_body is not None, method
        assert set(re.findall(r'append\("([^"\n]+)"', method_body.group(1))) == set(definitions[schema]["properties"]), schema
    assert 'followRedirects = false' in (FEATURE / "di/OpenAiOAuthModule.kt").read_text()
    print("OpenAI OAuth contract completeness and DTO parity passed (static documentation gate).")


if __name__ == "__main__":
    test_contract()
