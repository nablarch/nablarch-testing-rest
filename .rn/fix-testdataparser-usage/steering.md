# Goal

`RestTestSupport` の `isExisting()` メソッドが、シートの存在確認に Apache POI で Excel を直接開く実装になっており、SystemRepository に登録された `testDataParser`（例: `YamlTestDataParser`）が差し替えられても使われない問題を修正する。
`getSheet()` による Excel 直接読み込みをなくし、`TestDataParser#isResourceExisting()` 経由に統一する。

# Acceptance criteria

- `RestTestSupport` の `isExisting()` において、シートの存在確認が `TestDataParser#isResourceExisting()` を通じて行われる
- Apache POI (`WorkbookFactory`) を使った `getSheet()` メソッドが `isExisting()` から除去される
- SystemRepository に `YamlTestDataParser` を登録した場合、Excel ファイルではなく YAML ファイルが読み込まれる（Excel が存在しても Excel は読まれない）
- 既存の Excel ベースの動作（`BasicTestDataParser` / `PoiXlsReader` を使う場合）が引き続き動作する
- 変更前後で振る舞いが変わらないことが自動テストで保証されている

# Assumptions

- `getSheet()` はシートの存在確認以外には使われていない（実際のデータ読み込みは `dbSupport.setUpDb()` 経由）
- `dbSupport.setUpDb()` はファイルがあるがシートがない場合、空リストを返して自然にスキップする（`DbAccessTestSupport:184` の `if (allTables.isEmpty()) return;`）
- よって `getSheet()` によるシート単位確認は不要であり、`isResourceExisting()` によるファイル単位確認で十分
- `YamlTestDataParser` は `isResourceExisting()` を正しく実装している

# Rules

- commit and push every change; one completion marker per task
- **TDD で進める**: テストを先に書いて RED を確認してから実装する
- Java のコーディング規約は既存コードのスタイルに従う
- テストは既存のテストクラス `RestTestSupportTest` に追加・修正する

# Tasks

### #1: 後方互換を保証するテストを追加して RED を確認する

**Purpose**: 変更後も後方互換が壊れないことを保証するテストを先に書き、現状では通らない（RED）ことを確認する。

**Prerequisites**: none

**Steps**:

- [ ] 「ファイルはあるがシートがない → `setUpDb` がスキップされる」ケースのテストを `RestTestSupportTest` に追加する
- [ ] 「`testDataParser` を `YamlTestDataParser` 相当の mock に差し替えると `isResourceExisting()` が呼ばれる」ケースのテストを追加する
- [ ] `mvn test` を実行し、追加したテストが RED（失敗）であることを確認する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-1.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupportTest` に「ファイルありシートなし → スキップ」のテストケースが存在する
- `RestTestSupportTest` に「`testDataParser` 差し替えで `isResourceExisting()` が呼ばれる」テストケースが存在する
- 追加したテストが `mvn test` で失敗する（RED）

### #2: `getSheet()` を除去して `isResourceExisting()` 経由に統一し GREEN にする

**Purpose**: `RestTestSupport#isExisting()` から `getSheet()` を除去し、`testDataParser.isResourceExisting()` のみでシートの存在を判断するよう実装を変更して、#1 のテストを GREEN にする。

**Prerequisites**: #1

**Steps**:

- [ ] `isExisting()` 内の `getSheet(path, sheetName) != null` を除去し、`getPathOf()` の結果が null でなければ存在するとみなすよう変更する
- [ ] `getSheet()` メソッド自体を削除する
- [ ] Apache POI (`WorkbookFactory`, `Workbook`, `Sheet`) の import が不要になった場合は削除する
- [ ] `mvn test` を実行し、全テスト（#1 で追加したテストを含む）が GREEN であることを確認する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-2.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupport.java` に `getSheet()` メソッドが存在しない
- `isExisting()` 内で `WorkbookFactory` または Apache POI の呼び出しがない
- `mvn test` が全てグリーン

# Decisions

## D-1: `getSheet()` によるシート単位確認は不要
- **Issue**: `isExisting()` でファイル単位確認（`isResourceExisting`）に加えてシート単位確認（`getSheet`）を行う必要があるか
- **Conclusion**: 不要。ファイル単位確認で十分。
- **Rationale**: シートが存在しない場合でも `dbSupport.setUpDb()` は空リストを返して自然にスキップするため、`RestTestSupport` 側でシート単位確認をする必要がない。
- **Evidence**: `DbAccessTestSupport.java:184` の `if (allTables.isEmpty()) return;`
- **Sources**: コードリーディング（2026-06-24）

# State

- **Status**: not suspended
- **Date**: 2026-06-24
- **Last completed**: none
- **Next**: #1 後方互換を保証するテストを追加して RED を確認する
- **Notes**: `getSheet()` はシート確認専用。`dbSupport.setUpDb()` は既に `TestDataParser` 経由で正しく動作している。TDD で進める。
