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

### #1: リグレッションテストを追加して変更前 GREEN を確認する

**Purpose**: 実装変更前に後方互換を保証するリグレッションテストを追加し、現状（変更前）で GREEN であることを確認する。変更後も GREEN をキープすることで後方互換を保証する。

**Prerequisites**: none

**Steps**:

- [ ] 「ファイルはあるがシートがない → `setUpDb` がスキップされ例外が出ない」ケースのテストを `RestTestSupportTest` に追加する
- [ ] `mvn test` を実行し、追加したテストが現状（変更前）で GREEN であることを確認する（GREEN でない場合は D-1 の仮定が崩れているため設計を見直す）
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-1.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupportTest` に「ファイルありシートなし → スキップ」のテストケースが存在する
- 追加したテストが変更前の状態で `mvn test` GREEN である

### #2: `testDataParser` 差し替えの新テストを追加して RED を確認する

**Purpose**: TDD で `testDataParser` 差し替えが効くことを確認するテストを先に書き、現状（変更前）では RED であることを確認する。

**Prerequisites**: #1

**Steps**:

- [ ] 「`testDataParser` を mock に差し替えると `isResourceExisting()` が呼ばれる」テストを `RestTestSupportTest` に追加する
- [ ] `mvn test` を実行し、追加したテストが現状（変更前）で RED であることを確認する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-2.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupportTest` に「`testDataParser` 差し替えで `isResourceExisting()` が呼ばれる」テストケースが存在する
- 追加したテストが変更前の状態で `mvn test` RED である

### #3: `getSheet()` を除去して `isResourceExisting()` 経由に統一し全テスト GREEN にする

**Purpose**: `RestTestSupport#isExisting()` から `getSheet()` を除去し、`testDataParser.isResourceExisting()` のみでシートの存在を判断するよう実装を変更する。#1（リグレッション）と #2（新テスト）が両方 GREEN になることで完了。

**Prerequisites**: #2

**Steps**:

- [ ] `isExisting()` 内の `getSheet(path, sheetName) != null` を除去し、`getPathOf()` の結果が null でなければ存在するとみなすよう変更する
- [ ] `getSheet()` メソッド自体を削除する
- [ ] Apache POI (`WorkbookFactory`, `Workbook`, `Sheet`) の不要になった import を削除する
- [ ] `mvn test` を実行し、全テスト（#1 リグレッション・#2 新テスト・既存テスト）が GREEN であることを確認する
- [ ] self-check（各 Completion criteria を OK/NG で確認し `checks/task-3.md` に記録）
- [ ] QA expert review（subagent）
- [ ] language expert review（subagent）
- [ ] software-engineering expert review（subagent）
- [ ] user review

**Completion criteria**:

- `RestTestSupport.java` に `getSheet()` メソッドが存在しない
- `isExisting()` 内で `WorkbookFactory` または Apache POI の呼び出しがない
- `mvn test` が全て GREEN（#1 リグレッション・#2 新テスト・既存テストを含む）

# Decisions

## D-1: `getSheet()` によるシート単位確認は不要
- **Issue**: `isExisting()` でファイル単位確認（`isResourceExisting`）に加えてシート単位確認（`getSheet`）を行う必要があるか
- **Conclusion**: 不要。ファイル単位確認で十分。
- **Rationale**: シートが存在しない場合でも `dbSupport.setUpDb()` は空リストを返して自然にスキップするため、`RestTestSupport` 側でシート単位確認をする必要がない。
- **Evidence**: `DbAccessTestSupport.java:184` の `if (allTables.isEmpty()) return;`
- **Sources**: コードリーディング（2026-06-24）

# State

<!-- updated by rn:up / rn:pause — do not edit manually -->

