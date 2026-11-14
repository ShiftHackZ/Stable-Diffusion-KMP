#!/usr/bin/env python3
"""Statically validate the entire OpenAPI document; this does not test a server.

The document uses JSON, so validation has no dependencies.
Schemas, properties, media bodies and operations must be documented and typed.
Local references and schema compositions are traversed. Consumed DTOs must stay
linked to their operations, preventing orphaned component definitions from hiding
missing request or response contracts.
"""

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
CONTRACT = ROOT / "network/openai-compatible.openapi.json"


def validate(document):
    """Raise ValueError for an incomplete schema or operation anywhere in the document."""
    errors = []

    def require(condition, location, reason):
        if not condition:
            errors.append(f"{location}: {reason}")

    def resolve(reference, location):
        if not isinstance(reference, str) or not reference.startswith("#/"):
            errors.append(f"{location}: only local references are allowed")
            return {}
        target = document
        try:
            for part in reference[2:].split("/"):
                target = target[part.replace("~1", "/").replace("~0", "~")]
        except (KeyError, TypeError):
            errors.append(f"{location}: unresolved reference {reference}")
            return {}
        require(isinstance(target, dict), location, "reference must resolve to an object")
        return target if isinstance(target, dict) else {}

    def dereference(value, location):
        seen = set()
        while isinstance(value, dict) and "$ref" in value:
            reference = value["$ref"]
            if reference in seen:
                errors.append(f"{location}: circular reference without a concrete definition")
                return {}
            seen.add(reference)
            value = resolve(reference, location)
        return value if isinstance(value, dict) else {}

    def walk(value, location, in_schema=False, active_refs=frozenset()):
        if isinstance(value, list):
            for index, child in enumerate(value):
                walk(child, f"{location}/{index}", in_schema, active_refs)
            return
        if not isinstance(value, dict):
            if in_schema:
                errors.append(f"{location}: an explicit schema object is required")
            return
        if "$ref" in value:
            reference = value["$ref"]
            target = resolve(reference, location)
            if isinstance(reference, str) and reference not in active_refs:
                walk(target, reference, in_schema, active_refs | {reference})
            return
        if in_schema:
            require(bool(value.get("description", "").strip()), location, "description is required")
            compositions = {key for key in ("allOf", "anyOf", "oneOf") if key in value}
            require(bool(value.get("type")) or bool(compositions), location, "explicit type or typed composition is required")
            for key in compositions:
                require(isinstance(value[key], list) and bool(value[key]), location, f"{key} must contain schemas")
            types = value.get("type", [])
            types = [types] if isinstance(types, str) else types
            if "object" in types:
                require(bool(value.get("properties")), location, "object properties must be enumerated")
                require(value.get("additionalProperties") is False, location, "free-form object is prohibited")
                require(set(value.get("required", [])) <= set(value.get("properties", {})), location, "unknown required property")
            if "array" in types:
                require("items" in value, location, "array items are required")
        for key, child in value.items():
            child_location = f"{location}/{key}"
            if key in {"schemas", "properties", "dependentSchemas", "patternProperties"}:
                for name, definition in child.items():
                    walk(definition, f"{child_location}/{name}", True, active_refs)
            elif key == "content":
                require(isinstance(child, dict) and bool(child), child_location, "media content is required")
                if isinstance(child, dict):
                    for media_type, media in child.items():
                        require(isinstance(media, dict) and "schema" in media, f"{child_location}/{media_type}", "every media type requires a schema")
                walk(child, child_location, active_refs=active_refs)
            else:
                walk(child, child_location, key in {"schema", "items", "requestUrl", "allOf", "anyOf", "oneOf", "not", "if", "then", "else", "contains", "prefixItems"}, active_refs)

    def linked_schemas(value, location, seen=frozenset()):
        """Find referenced schema definitions reached from a concrete operation media body."""
        if isinstance(value, list):
            return set().union(*(linked_schemas(child, location, seen) for child in value))
        if not isinstance(value, dict):
            return set()
        links = set()
        if "$ref" in value:
            reference = value["$ref"]
            if isinstance(reference, str) and reference not in seen:
                links.add(reference)
                links.update(linked_schemas(resolve(reference, location), location, seen | {reference}))
        for key, child in value.items():
            if key != "$ref":
                links.update(linked_schemas(child, location, seen))
        return links

    require(document.get("openapi", "").startswith("3.1."), "#", "OpenAPI 3.1 is required")
    walk(document, "#")
    for route, item in document.get("paths", {}).items():
        for method, operation in item.items():
            if method not in {"get", "post", "put", "delete", "patch", "head", "options"}:
                continue
            location = f"#/paths/{route}/{method}"
            require(bool(operation.get("description")), location, "operation description required")
            require(bool(operation.get("operationId")), location, "operationId required")
            require(bool(operation.get("responses")), location, "responses required")
            for status, response in operation.get("responses", {}).items():
                response = dereference(response, location + "/" + status)
                require(bool(response.get("description")), location + "/" + status, "response description required")
            if "requestBody" in operation:
                body = dereference(operation["requestBody"], location + "/requestBody")
                require(bool(body.get("description")) and bool(body.get("content")), location, "request body description/content required")
            for parameter in operation.get("parameters", []):
                parameter = dereference(parameter, location + "/parameters")
                require(bool(parameter.get("description")) and "schema" in parameter, location, "parameter description/schema required")
    for route in ("/models", "/images/generations"):
        require(route in document.get("paths", {}), "#/paths", f"missing client route {route}")
    generation = document.get("paths", {}).get("/images/generations", {}).get("post", {})
    request = dereference(generation.get("requestBody", {}), "generation request")
    success = dereference(generation.get("responses", {}).get("200", {}), "generation success")
    for label, body, expected in (("request", request, "GenerationRequest"), ("success", success, "GenerationResponse")):
        media = body.get("content", {}).get("application/json", {})
        require("schema" in media, label, "generation JSON body schema required")
        links = linked_schemas(media.get("schema", {}), label)
        require(f"#/components/schemas/{expected}" in links, label, f"operation must use {expected}")
        if label == "success":
            require("#/components/schemas/Image" in links, label, "operation must use the image DTO schema")
    require(request.get("required") is True, "generation request", "request body must be required")
    definitions = document.get("components", {}).get("schemas", {})
    dto_files = {
        "GenerationRequest": ("request/OpenAiCompatibleRequest.kt", "OpenAiCompatibleRequest"),
        "GenerationResponse": ("response/OpenAiCompatibleResponse.kt", "OpenAiCompatibleResponse"),
        "Image": ("response/OpenAiCompatibleResponse.kt", "OpenAiCompatibleImage"),
    }
    source_root = ROOT / "network/src/commonMain/kotlin/com/shifthackz/aisdv1/network"
    for schema_name, (filename, class_name) in dto_files.items():
        source = (source_root / filename).read_text()
        declaration = re.search(r"data class " + class_name + r"\((.*?)\)\s*(?:$|/)", source, re.S)
        require(declaration is not None, filename, "serializable DTO declaration must be inspectable")
        if declaration:
            fields = re.findall(r'(?:@SerialName\("([^"\n]+)"\)\s*)?val\s+(\w+)\s*:', declaration.group(1))
            actual = {serialized or name for serialized, name in fields}
            documented = set(definitions.get(schema_name, {}).get("properties", {}))
            require(actual == documented, schema_name, f"DTO fields {sorted(actual)} differ from documented fields {sorted(documented)}")
    if errors:
        raise ValueError("\n".join(errors))


if __name__ == "__main__":
    validate(json.loads(CONTRACT.read_text()))
    print("OpenAI-compatible OpenAPI completeness check passed (static documentation check).")
