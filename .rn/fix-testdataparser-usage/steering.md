# Goal

`RestTestSupport` の `isExisting()` メソッドが、シートの存在確認に Apache POI で Excel を直接開く実装になっており、SystemRepository に登録された `testDataParser`（例: `YamlTestDataParser`）が差し替えられても使われない問題を修正する。
`getSheet()` による Excel 直接読み込みをなくし、`TestDataParser#isResourceExisting()` 経由に統一する。

# Acceptance criteria

- `RestTestSupport` の `isExisting()` において、シートの存在確認が `TestDataParser#isResourceExisting()` を通じて行われる
- Apache POI (`WorkbookFactory`) を使った `getSheet()` メソッドが `isExisting()` から除去される
- SystemRepository に `YamlTestDataParser` を登録した場合、Excel ファイルではなく YAML ファイルが読み込まれる（Excel が存在しても Excel は読まれない）
- 既存の Excel ベースのテスト（`ExcelTestDataParser` を使う場合）が引き続き動作する
- 変更に対応するユニットテストが存在する

# Assumptions

- `TestDataParser#isResourceExisting(basePath, resourceName)` はリソースの存在確認に加えて、シート（またはそれに相当するキー）の存在確認も担う実装がパーサー側にある、あるいはリソース単位の確認で十分
- `getSheet()` はシートの存在確認以外には使われていない（読み込みは `dbSupport.setUpDb()` 経由）
- `YamlTestDataParser` は `isResourceExisting()` を正しく実装している

# Rules

- commit and push every change; one completion marker per task
- Java のコーディング規約は既存コードのスタイルに従う
- テストは既存のテストクラス `RestTestSupportTest` に追加・修正する

# Tasks

### #1: `getSheet()` の除去と `isExisting()` の `TestDataParser` 経由への統一

**Purpose**: `RestTestSupport#isExisting()` が `getSheet()` で Excel を直接開く代わりに、`TestDataParser#isResourceExisting()` を使ってシートの存在を確認するよう修正する。

**Prerequisites**: none

**Steps**:

- [ ] `RestTestSupport#getSheet()` の呼び出し箇所（`isExisting()` 内）を `getTestDataParser().isResourceExisting(path, sheetResourceName)` に置き換える
- [ ] `getSheet()` メソッド自体を削除する（他に呼び出し元がないことを確認）
- [ ] `isExisting()` のロジックが `testDataExists` フラグを適切に扱えているか確認・調整する
- [ ] `RestTestSupportTest` に `YamlTestDataParser` を mock/stub して差し替えが効くことを確認するテストを追加する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-1.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupport.java` に `getSheet()` メソッドが存在しない
- `isExisting()` 内で `WorkbookFactory` または Apache POI の呼び出しがない
- `RestTestSupportTest` に `testDataParser` を差し替えても動作するテストケースが存在する
- `mvn test` がグリーン（既存テストが壊れていない）

### #2: `isExisting()` のシート単位確認が必要か検討・対応

**Purpose**: `TestDataParser#isResourceExisting()` がリソース（ファイル）単位の確認であるため、シート単位の確認が必要な場合に `TestDataParser` インターフェースへのメソッド追加が必要かどうかを判断・実装する。

**Prerequisites**: #1

**Steps**:

- [ ] `YamlTestDataParser` および `ExcelTestDataParser` の `isResourceExisting()` の実装を確認し、シート単位の確認が必要かを判断する
- [ ] 必要であれば `TestDataParser` インターフェースに `isSheetExisting()` を追加し各実装クラスに実装する
- [ ] 不要であれば決定事項として `Decisions` に記録する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-2.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- シート単位の確認が必要か不要かの判断が `Decisions` に記録されている
- 必要な場合: `TestDataParser` に `isSheetExisting()` が追加され、`YamlTestDataParser` / `ExcelTestDataParser` が実装している
- 不要な場合: #1 の変更で正しく動作することがテストで確認されている

# Decisions

# State

- **Status**: not suspended
- **Date**: 2026-06-24
- **Last completed**: none
- **Next**: #1 `getSheet()` の除去と `isExisting()` の `TestDataParser` 経由への統一
- **Notes**: `RestTestSupport#isExisting()` の問題が起点。`getSheet()` はシート確認専用でデータ読み込みには使われていない。`dbSupport.setUpDb()` は既に `TestDataParser` 経由で正しく動作している。
