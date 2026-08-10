#!/usr/bin/env python3
"""Dump deterministic class/member access flags directly from a DEX or DEX-containing JAR."""

from __future__ import annotations

import argparse
import json
import struct
import zipfile
from pathlib import Path


def uleb(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        byte = data[offset]
        offset += 1
        value |= (byte & 0x7F) << shift
        if byte & 0x80 == 0:
            return value, offset
        shift += 7
        if shift >= 35:
            raise ValueError("invalid ULEB128")


def dex_bytes(path: Path) -> bytes:
    content = path.read_bytes()
    if content.startswith(b"dex\n"):
        return content
    with zipfile.ZipFile(path) as archive:
        return archive.read("classes.dex")


class Dex:
    def __init__(self, data: bytes) -> None:
        if not data.startswith(b"dex\n"):
            raise ValueError("not a DEX file")
        self.data = data
        self.strings = self._strings()
        self.types = self._types()
        self.protos = self._protos()
        self.fields = self._fields()
        self.methods = self._methods()

    def pair(self, offset: int) -> tuple[int, int]:
        return struct.unpack_from("<II", self.data, offset)

    def _strings(self) -> list[str]:
        size, offset = self.pair(56)
        result = []
        for index in range(size):
            position = struct.unpack_from("<I", self.data, offset + index * 4)[0]
            _, position = uleb(self.data, position)
            end = self.data.index(0, position)
            result.append(self.data[position:end].decode("utf-8", errors="replace"))
        return result

    def _types(self) -> list[str]:
        size, offset = self.pair(64)
        return [self.strings[struct.unpack_from("<I", self.data, offset + i * 4)[0]]
                for i in range(size)]

    def _type_list(self, offset: int) -> tuple[str, ...]:
        if offset == 0:
            return ()
        size = struct.unpack_from("<I", self.data, offset)[0]
        return tuple(self.types[struct.unpack_from("<H", self.data, offset + 4 + i * 2)[0]]
                     for i in range(size))

    def _protos(self) -> list[tuple[tuple[str, ...], str]]:
        size, offset = self.pair(72)
        result = []
        for i in range(size):
            _, return_type, parameters = struct.unpack_from(
                    "<III", self.data, offset + i * 12)
            result.append((self._type_list(parameters), self.types[return_type]))
        return result

    def _fields(self) -> list[tuple[str, str, str]]:
        size, offset = self.pair(80)
        result = []
        for i in range(size):
            class_index, type_index, name_index = struct.unpack_from(
                    "<HHI", self.data, offset + i * 8)
            result.append((self.types[class_index], self.strings[name_index],
                           self.types[type_index]))
        return result

    def _methods(self) -> list[tuple[str, str, tuple[str, ...], str]]:
        size, offset = self.pair(88)
        result = []
        for i in range(size):
            class_index, proto_index, name_index = struct.unpack_from(
                    "<HHI", self.data, offset + i * 8)
            parameters, returns = self.protos[proto_index]
            result.append((self.types[class_index], self.strings[name_index],
                           parameters, returns))
        return result

    def class_records(self) -> list[tuple[str, int, str, tuple[str, ...], int]]:
        size, offset = self.pair(96)
        result = []
        for i in range(size):
            values = struct.unpack_from("<8I", self.data, offset + i * 32)
            class_index, access, superclass, interfaces, _, _, class_data, _ = values
            super_name = "" if superclass == 0xFFFFFFFF else self.types[superclass]
            result.append((self.types[class_index], access, super_name,
                           self._type_list(interfaces), class_data))
        return result

    def members(self, class_data: int) -> tuple[list[tuple[int, int]], list[tuple[int, int]]]:
        if class_data == 0:
            return [], []
        counts = []
        position = class_data
        for _ in range(4):
            count, position = uleb(self.data, position)
            counts.append(count)
        fields: list[tuple[int, int]] = []
        methods: list[tuple[int, int]] = []
        for count in counts[:2]:
            field_index = 0
            for _ in range(count):
                difference, position = uleb(self.data, position)
                access, position = uleb(self.data, position)
                field_index += difference
                fields.append((field_index, access))
        for count in counts[2:]:
            method_index = 0
            for _ in range(count):
                difference, position = uleb(self.data, position)
                access, position = uleb(self.data, position)
                _, position = uleb(self.data, position)
                method_index += difference
                methods.append((method_index, access))
        return fields, methods


def selected(descriptor: str, roots: tuple[str, ...]) -> bool:
    return any(descriptor == root or descriptor.startswith(root[:-1] + "$") for root in roots)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("artifact", type=Path)
    parser.add_argument("--class", dest="classes", action="append", default=[],
                        help="DEX descriptor; nested classes are included")
    parser.add_argument("--mapping", type=Path,
                        help="Read root classes from r2-mapping.json")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()

    roots = list(args.classes)
    if args.mapping is not None:
        mapping = json.loads(args.mapping.read_text(encoding="utf-8"))
        roots.extend(mapping["r2_dex_access_baseline"]["root_classes"])
    roots = list(dict.fromkeys(roots))
    if not roots:
        parser.error("provide --class or --mapping")

    dex = Dex(dex_bytes(args.artifact))
    lines = ["# Generated directly from classes.dex; access values are DEX flags.\n"]
    records = sorted((record for record in dex.class_records()
                      if selected(record[0], tuple(roots))), key=lambda value: value[0])
    found_roots = {root: False for root in roots}
    for descriptor, access, superclass, interfaces, class_data in records:
        for root in found_roots:
            if selected(descriptor, (root,)):
                found_roots[root] = True
        suffix = " interfaces=" + ",".join(interfaces) if interfaces else ""
        lines.append(f"class {descriptor} access=0x{access:x} super={superclass}{suffix}\n")
        fields, methods = dex.members(class_data)
        for field_index, field_access in sorted(fields, key=lambda value: dex.fields[value[0]][1:]):
            owner, name, field_type = dex.fields[field_index]
            if owner == descriptor:
                lines.append(f"  field {name}:{field_type} access=0x{field_access:x}\n")
        for method_index, method_access in sorted(
                methods, key=lambda value: (dex.methods[value[0]][1], dex.methods[value[0]][2],
                                            dex.methods[value[0]][3])):
            owner, name, parameters, returns = dex.methods[method_index]
            if owner == descriptor:
                lines.append(f"  method {name}({''.join(parameters)}){returns} "
                             f"access=0x{method_access:x}\n")
    missing = [root for root, found in found_roots.items() if not found]
    if missing:
        raise SystemExit("classes missing from DEX: " + ", ".join(missing))
    text = "".join(lines)
    if args.output:
        args.output.write_text(text, encoding="utf-8")
    else:
        print(text, end="")


if __name__ == "__main__":
    main()
